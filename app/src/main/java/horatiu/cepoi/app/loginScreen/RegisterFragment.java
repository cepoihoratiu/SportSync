package horatiu.cepoi.app.loginScreen;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;

import horatiu.cepoi.app.R;
import horatiu.cepoi.app.mainScreen.HomepageFragment;
import horatiu.cepoi.app.mainScreen.MainFragment;
import horatiu.cepoi.app.repositories.UserRepository;

public class RegisterFragment extends Fragment {
    private EditText passwordEditText, emailEditText;
    private Button registerButton, backToLoginButton;
    private UserRepository userRepository;

    public RegisterFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        emailEditText = view.findViewById(R.id.email_register);
        passwordEditText = view.findViewById(R.id.password_register);
        registerButton = view.findViewById(R.id.register_button);
        backToLoginButton = view.findViewById(R.id.back_to_login_button);
        userRepository = new UserRepository();

        registerButton.setOnClickListener(v -> registerUser());
        backToLoginButton.setOnClickListener(v -> goToLogin());

        return view;
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        TextInputLayout emailLayout = requireView().findViewById(R.id.email_input_layout);
        TextInputLayout passwordLayout = requireView().findViewById(R.id.password_input_layout);

        emailLayout.setError(null);
        passwordLayout.setError(null);

        if (email.isEmpty()) {
            emailLayout.setError("Email is required");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Invalid email format");
            return;
        }

        if (password.isEmpty()) {
            passwordLayout.setError("Password is required");
            return;
        }

        if (password.length() < 6) {
            passwordLayout.setError("Password must be at least 6 characters");
            return;
        }

        userRepository.registerUserWithEmail(email, password, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(Object result) {
                String userId = (String) result;
                Toast.makeText(getActivity(), "Registration successful", Toast.LENGTH_SHORT).show();
                HomepageFragment homepageFragment = HomepageFragment.newInstance(userId);
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new MainFragment(homepageFragment, userId))
                        .commit();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getActivity(), "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void goToLogin() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new LoginFragment())
                .commit();
    }
}
