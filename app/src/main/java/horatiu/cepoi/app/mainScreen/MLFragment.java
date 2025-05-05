package horatiu.cepoi.app.mainScreen;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.vision.classifier.ImageClassifier;
import org.tensorflow.lite.task.vision.classifier.Classifications;

import java.util.List;

import horatiu.cepoi.app.R;

public class MLFragment extends Fragment {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final String TAG = "MLFragment";

    private final String[] labels = {"Pushup", "Plank", "Squat", "Lateral raises"};

    private TextureView textureView;
    private TextView statusTextView;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest.Builder previewRequestBuilder;
    private ImageClassifier classifier;

    public MLFragment() {}

    public static MLFragment newInstance(String userId) {
        MLFragment fragment = new MLFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ml, container, false);
        textureView = view.findViewById(R.id.camera_preview);
        statusTextView = view.findViewById(R.id.statusTextView);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            setupCamera();
        }

        loadModel();
        return view;
    }

    private void setupCamera() {
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                openFrontCamera();
            }

            @Override public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {}

            @Override public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                return true;
            }

            @Override public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
                classifyFrame();
            }
        });
    }

    private void loadModel() {
        try {
            classifier = ImageClassifier.createFromFile(requireContext(), "model.tflite");
            Log.d(TAG, "✅ Model loaded");
        } catch (Exception e) {
            String error = "❌ Failed to load model: " + e.getMessage();
            Log.e(TAG, error, e);
            statusTextView.setText(error);
        }
    }

    private int lastShownBucket = -1;
    private String lastShownLabel = "";

    private void classifyFrame() {
        if (classifier == null || textureView == null || !textureView.isAvailable()) return;

        Bitmap bitmap = textureView.getBitmap();
        if (bitmap == null) return;

        TensorImage tensorImage = TensorImage.fromBitmap(bitmap);

        try {
            List<Classifications> results = classifier.classify(tensorImage);

            if (results.isEmpty()) return;

            Category topCategory = results.get(0).getCategories().get(0);
            int labelIndex;

            try {
                labelIndex = Integer.parseInt(topCategory.getLabel());
            } catch (NumberFormatException e) {
                labelIndex = 0; // fallback
            }

            String labelName = labelIndex < labels.length ? labels[labelIndex] : "Unknown";

            // Round to nearest 10 (e.g., 68 -> 70)
            int percentage = Math.round(topCategory.getScore() * 100);
            int bucket = Math.round(percentage / 10.0f) * 10;

            if (labelName.equals(lastShownLabel) && bucket == lastShownBucket) return;

            lastShownLabel = labelName;
            lastShownBucket = bucket;

            String correctness;
            if (bucket >= 90) {
                correctness = "✅ Perfect form";
            } else if (bucket >= 50 && bucket < 90) {
                correctness = "⚠️ Needs improvement";
            } else {
                correctness = "❌ Incorrect form";
            }

            String display = labelName + "\n" + correctness;

            requireActivity().runOnUiThread(() -> statusTextView.setText(display));

        } catch (Exception e) {
            Log.e(TAG, "Classification error: " + e.getMessage(), e);
        }
    }
    private void openFrontCamera() {
        CameraManager manager = (CameraManager) requireActivity().getSystemService(Activity.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) return;

                    manager.openCamera(cameraId, stateCallback, null);
                    break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to open camera", e);
        }
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            startPreview();
        }

        @Override public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            cameraDevice = null;
        }

        @Override public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            cameraDevice = null;
        }
    };

    private void startPreview() {
        if (cameraDevice == null || !textureView.isAvailable()) return;

        SurfaceTexture texture = textureView.getSurfaceTexture();
        texture.setDefaultBufferSize(textureView.getWidth(), textureView.getHeight());
        Surface surface = new Surface(texture);

        try {
            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(
                    java.util.Collections.singletonList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override public void onConfigured(@NonNull CameraCaptureSession session) {
                            cameraCaptureSession = session;
                            try {
                                cameraCaptureSession.setRepeatingRequest(
                                        previewRequestBuilder.build(), null, null);
                            } catch (CameraAccessException e) {
                                Log.e(TAG, "Preview error", e);
                            }
                        }

                        @Override public void onConfigureFailed(@NonNull CameraCaptureSession session) {}
                    },
                    null
            );
        } catch (CameraAccessException e) {
            Log.e(TAG, "Camera preview error", e);
        }
    }
}
