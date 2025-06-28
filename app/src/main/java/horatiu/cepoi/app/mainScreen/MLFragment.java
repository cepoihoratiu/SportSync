package horatiu.cepoi.app.mainScreen;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.vision.classifier.ImageClassifier;
import org.tensorflow.lite.task.vision.classifier.Classifications;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import horatiu.cepoi.app.R;

public class MLFragment extends Fragment {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final String TAG = "MLFragment";

    private String[] labels;
    private boolean firstPickMade = false;
    private boolean isPaused = true;

    private TextureView textureView;
    private TextView statusTextView;
    private Spinner exerciseSpinner;
    private TextView instructionText;
    private Button pauseButton;
    private String selectedExercise = "Pushup";

    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest.Builder previewRequestBuilder;
    private ImageClassifier classifier;
    private Handler handler = new Handler();
    private Runnable classifyRunnable;

    private final List<Integer> recentBuckets = new ArrayList<>();
    private static final int MAX_BUCKET_HISTORY = 10; // 1 sec at 10 Hz

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
        loadLabelsFromAssets(requireContext());

        textureView = view.findViewById(R.id.camera_preview);
        statusTextView = view.findViewById(R.id.statusTextView);
        exerciseSpinner = view.findViewById(R.id.exercise_spinner);
        pauseButton = view.findViewById(R.id.pause_button);
        instructionText = view.findViewById(R.id.instructionText);
        instructionText.setText("📣 Pick an exercise to start scanning!");

        pauseButton.setOnClickListener(v -> {
            isPaused = !isPaused;
            pauseButton.setText(isPaused ? "Resume" : "Pause");
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exerciseSpinner.setAdapter(adapter);
        exerciseSpinner.setSelection(0);
        exerciseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedExercise = labels[position];
                statusTextView.setText("Analyzing: " + selectedExercise);

                if (!firstPickMade) {
                    firstPickMade = true;
                    new android.app.AlertDialog.Builder(requireContext())
                            .setTitle("📷 Camera Instructions")
                            .setMessage("Make sure your body is fully visible in the frame.\n" +
                                    "Use a simple, clean background.\n" +
                                    "Good lighting helps improve accuracy.\n\n" +
                                    "Position yourself clearly before starting.")
                            .setPositiveButton("OK", null)
                            .show();
                }
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            setupCamera();
        }

        loadModel();

        classifyRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isPaused) classifyFrame();
                handler.postDelayed(this, 500); // every 0.5 sec
            }
        };
        handler.postDelayed(classifyRunnable, 500);

        return view;
    }

    private void loadLabelsFromAssets(Context context) {
        List<String> labelList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(context.getAssets().open("labels.txt")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split(" ", 2);
                if (parts.length == 2) {
                    labelList.add(parts[1].trim());
                }
            }
            labels = labelList.toArray(new String[0]);
        } catch (IOException e) {
            e.printStackTrace();
            labels = new String[]{};
        }
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

    private void classifyFrame() {
        if (classifier == null || textureView == null || !textureView.isAvailable()) return;

        Bitmap bitmap = textureView.getBitmap();
        if (bitmap == null) return;

        TensorImage tensorImage = TensorImage.fromBitmap(bitmap);

        try {
            List<Classifications> results = classifier.classify(tensorImage);
            if (results.isEmpty()) return;

            int selectedIndex = -1;
            for (int i = 0; i < labels.length; i++) {
                if (labels[i].equals(selectedExercise)) {
                    selectedIndex = i;
                    break;
                }
            }
            if (selectedIndex == -1) return;

            Category matchedCategory = null;
            for (Category category : results.get(0).getCategories()) {
                int labelIndex;
                try {
                    labelIndex = Integer.parseInt(category.getLabel());
                } catch (NumberFormatException e) {
                    continue;
                }
                if (labelIndex == selectedIndex) {
                    matchedCategory = category;
                    break;
                }
            }
            if (matchedCategory == null) return;

            int percentage = Math.round(matchedCategory.getScore() * 100);
            int bucket = Math.round(percentage / 10.0f) * 10;

            // Add to history
            recentBuckets.add(bucket);
            if (recentBuckets.size() > MAX_BUCKET_HISTORY) {
                recentBuckets.remove(0);
            }

            // Evaluate last sec
            int perfect = 0, good = 0, bad = 0;
            for (int b : recentBuckets) {
                if (b >= 90) perfect++;
                else if (b >= 50) good++;
                else bad++;
            }

            String correctness;
            if (perfect >= good && perfect >= bad) {
                correctness = "✅ Perfect form";
            } else if (good >= bad) {
                correctness = "⚠️ Needs improvement";
            } else {
                correctness = "❌ Incorrect form";
            }

            String display = selectedExercise + "\n" + correctness;
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

    private void setupCamera() {
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                openFrontCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
                adjustAspectRatio(width, height);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {}
        });
    }

    private void adjustAspectRatio(int textureViewWidth, int textureViewHeight) {
        int cameraWidth = 640;
        int cameraHeight = 480;
        float cameraAspectRatio = (float) cameraWidth / cameraHeight;

        float textureViewAspectRatio = (float) textureViewWidth / textureViewHeight;

        if (cameraAspectRatio > textureViewAspectRatio) {
            int newHeight = (int) (textureViewWidth / cameraAspectRatio);
            textureView.setLayoutParams(new FrameLayout.LayoutParams(textureViewWidth, newHeight));
        } else {
            int newWidth = (int) (textureViewHeight * cameraAspectRatio);
            textureView.setLayoutParams(new FrameLayout.LayoutParams(newWidth, textureViewHeight));
        }
    }

}
