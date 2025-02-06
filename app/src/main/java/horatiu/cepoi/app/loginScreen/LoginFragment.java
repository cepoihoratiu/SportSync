package horatiu.cepoi.app.loginScreen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import horatiu.cepoi.app.R;
import horatiu.cepoi.app.repositories.UserRepository;

public class LoginFragment extends Fragment {
    private EditText usernameEditText, passwordEditText;
    private Button loginButton, registerButton;
    private UserRepository userRepository;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        usernameEditText = view.findViewById(R.id.emailLogin_editText); // Use username field
        passwordEditText = view.findViewById(R.id.password_editText);
        loginButton = view.findViewById(R.id.login_button);
        registerButton = view.findViewById(R.id.registerFromLogin_button);
        userRepository = new UserRepository();

        loginButton.setOnClickListener(v -> loginUser());
        registerButton.setOnClickListener(v -> registerUser());

        return view;
    }

    private void loginUser() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        userRepository.loginUser(username, password, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(Object result) {
                Toast.makeText(getActivity(), result.toString(), Toast.LENGTH_SHORT).show();
                // Navigate to next screen or update UI
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getActivity(), "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void registerUser() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        userRepository.registerUser(username, password, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(Object result) {
                Toast.makeText(getActivity(), result.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getActivity(), "Registration failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
