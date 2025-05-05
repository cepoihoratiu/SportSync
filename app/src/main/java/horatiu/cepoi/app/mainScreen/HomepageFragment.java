package horatiu.cepoi.app.mainScreen;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import horatiu.cepoi.app.R;

public class HomepageFragment extends Fragment {

    private String userId;

    public HomepageFragment() {}

    public static HomepageFragment newInstance(String userId) {
        HomepageFragment fragment = new HomepageFragment();
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
            Log.d("Homepage", "Logged in user ID: " + userId);
            System.out.println(userId + " homepage");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_homepage, container, false);

        TextView homepageText = view.findViewById(R.id.homepage_text);
        homepageText.setText("Welcome, " + (userId != null ? userId : "user") + "!");

        return view;
    }
}
