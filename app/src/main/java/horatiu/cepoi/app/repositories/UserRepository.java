package horatiu.cepoi.app.repositories;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final CollectionReference usersCollection;

    public UserRepository() {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        this.usersCollection = db.collection("users"); // Firestore "users" collection
    }

    // Register a new user (Check if username already exists)
    public void registerUser(String username, String password, final UserCallback callback) {
        usersCollection.whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        callback.onFailure(new Exception("Username already taken"));
                    } else {
                        saveUserToFirestore(username, password, callback);
                    }
                });
    }

    // Save user to Firestore with an auto-generated document ID
    private void saveUserToFirestore(String username, String password, final UserCallback callback) {
        String hashedPassword = hashPassword(password); // Securely hash the password

        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("password", hashedPassword); // Store hashed password

        usersCollection.add(user) // Firestore will generate a unique document ID
                .addOnSuccessListener(documentReference -> callback.onSuccess("User registered with ID: " + documentReference.getId()))
                .addOnFailureListener(callback::onFailure);
    }

    // Login User by checking username and hashed password in Firestore
    public void loginUser(String username, String password, final UserCallback callback) {
        String hashedPassword = hashPassword(password);

        usersCollection.whereEqualTo("username", username)
                .whereEqualTo("password", hashedPassword) // Matching hashed password
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String userId = queryDocumentSnapshots.getDocuments().get(0).getId(); // ✅ Real Firestore user ID
                        callback.onSuccess(userId); // Pass the actual ID
                    } else {
                        callback.onFailure(new Exception("Invalid username or password"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    // Logout User
    public void logoutUser() {
        auth.signOut();
    }

    // Password Hashing (SHA-256)
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Hashing error: " + e.getMessage());
            return password; // Fallback (Not recommended)
        }
    }

    // Callback interface
    public interface UserCallback {
        void onSuccess(Object result);
        void onFailure(Exception e);
    }
}
