package horatiu.cepoi.app.mainScreen;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;

import horatiu.cepoi.app.R;
import horatiu.cepoi.app.loginScreen.LoginFragment;
import horatiu.cepoi.app.repositories.UserRepository;

public class ProfileFragment extends Fragment {

    private String userId;
    private TextInputEditText nameEditText, surnameEditText, emailEditText, phoneEditText;
    private final UserRepository userRepository = new UserRepository();

    public ProfileFragment() {}

    public static ProfileFragment newInstance(String userId) {
        ProfileFragment fragment = new ProfileFragment();
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
            Log.d("ProfileFragment", "User ID: " + userId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        nameEditText = view.findViewById(R.id.nameEditText);
        surnameEditText = view.findViewById(R.id.surnameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        phoneEditText = view.findViewById(R.id.phoneEditText);
        Button saveButton = view.findViewById(R.id.saveProfileButton);

        if (userId != null) {
            userRepository.getUserById(userId, new UserRepository.UserCallback() {
                @Override
                public void onSuccess(Object result) {
                    Map<String, Object> user = (Map<String, Object>) result;
                    nameEditText.setText((String) user.get("name"));
                    surnameEditText.setText((String) user.get("surname"));
                    emailEditText.setText((String) user.get("email"));
                    phoneEditText.setText((String) user.get("phone"));
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getActivity(), "Error loading profile", Toast.LENGTH_SHORT).show();
                }
            });
        }

        saveButton.setOnClickListener(v -> {
            Map<String, Object> updates = new HashMap<>();
            updates.put("name", nameEditText.getText().toString().trim());
            updates.put("surname", surnameEditText.getText().toString().trim());
            updates.put("email", emailEditText.getText().toString().trim());
            updates.put("phone", phoneEditText.getText().toString().trim());

            userRepository.updateUser(userId, updates, new UserRepository.UserCallback() {
                @Override
                public void onSuccess(Object result) {
                    Toast.makeText(getActivity(), "Profile updated", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getActivity(), "Update failed", Toast.LENGTH_SHORT).show();
                }
            });
        });

        Button logoutButton = view.findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment())
                    .commit();
        });


        return view;
    }
}
