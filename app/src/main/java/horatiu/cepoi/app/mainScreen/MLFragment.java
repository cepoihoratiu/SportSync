package horatiu.cepoi.app.mainScreen;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import horatiu.cepoi.app.R;

public class MLFragment extends Fragment {
    private String userId;

    public MLFragment() {}

    public static MLFragment newInstance(String userId) {
        MLFragment fragment = new MLFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
            Log.d("MLFragment", "Logged in user ID: " + userId);
            System.out.println(userId + " ml");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // ✅ Inflate your layout
        return inflater.inflate(R.layout.fragment_ml, container, false);
    }
}
