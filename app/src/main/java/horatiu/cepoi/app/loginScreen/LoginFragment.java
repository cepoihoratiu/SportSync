package horatiu.cepoi.app.loginScreen;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

import horatiu.cepoi.app.R;
import horatiu.cepoi.app.mainScreen.HomepageFragment;
import horatiu.cepoi.app.mainScreen.MainFragment;
import horatiu.cepoi.app.repositories.UserRepository;

public class LoginFragment extends Fragment {
    private EditText usernameEditText, passwordEditText;
    private Button loginButton, registerButton;
    private TextView forgotPasswordTextView;
    private UserRepository userRepository;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        usernameEditText = view.findViewById(R.id.emailLogin_editText);
        passwordEditText = view.findViewById(R.id.password_editText);
        loginButton = view.findViewById(R.id.login_button);
        registerButton = view.findViewById(R.id.registerFromLogin_button);
        forgotPasswordTextView = view.findViewById(R.id.forgotPassword_textView);
        userRepository = new UserRepository();

        loginButton.setOnClickListener(v -> loginUser());
        registerButton.setOnClickListener(v -> goToRegister());
        forgotPasswordTextView.setOnClickListener(v -> showForgotPasswordDialog());

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
                String userId = (String) result;

                Toast.makeText(getActivity(), "Login successful", Toast.LENGTH_SHORT).show();
                HomepageFragment homepageFragment = HomepageFragment.newInstance(userId);

                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new MainFragment(homepageFragment, userId))
                        .commit();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getActivity(), "Login failed", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void goToRegister() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new RegisterFragment())
                .commit();
    }

    private void showForgotPasswordDialog() {
        Context context = getContext();
        if (context == null) return;

        final EditText emailInput = new EditText(context);
        emailInput.setHint("Enter your email");
        emailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailInput.setPadding(48, 32, 48, 32);

        new MaterialAlertDialogBuilder(context)
                .setTitle("Reset Password")
                .setMessage("We'll send you a link to reset your password.")
                .setView(emailInput)
                .setPositiveButton("Send", (dialog, which) -> {
                    String email = emailInput.getText().toString().trim();
                    if (!email.isEmpty()) {
                        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(context, "Reset email sent", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(context, "Failed to send reset email", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(context, "Email field is empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
