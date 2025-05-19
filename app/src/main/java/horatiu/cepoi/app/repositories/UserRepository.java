package horatiu.cepoi.app.repositories;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import horatiu.cepoi.app.data.models.User;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final CollectionReference usersCollection;

    public UserRepository() {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        this.usersCollection = db.collection("users");
    }

    public void registerUserWithEmail(String email, String password, final UserCallback callback) {

        usersCollection.whereEqualTo("email", email).get().addOnCompleteListener(emailTask -> {
            if (emailTask.isSuccessful() && !emailTask.getResult().isEmpty()) {
                callback.onFailure(new Exception("Email already in use"));
            } else {
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener(authResult -> {
                            saveUserToFirestore(email, password, callback);
                        })
                        .addOnFailureListener(callback::onFailure);
            }
        });
    }

    private void saveUserToFirestore(String email, String password, final UserCallback callback) {
        String hashedPassword = hashPassword(password);

        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("password", hashedPassword);
        user.put("name", null);
        user.put("surname", null);
        user.put("phoneNumber", null);

        usersCollection.add(user)
                .addOnSuccessListener(docRef -> callback.onSuccess(docRef.getId()))
                .addOnFailureListener(callback::onFailure);
    }

    public void loginUser(String email, String password, final UserCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String userEmail = auth.getCurrentUser().getEmail();

                    usersCollection.whereEqualTo("email", userEmail)
                            .get()
                            .addOnSuccessListener(query -> {
                                if (!query.isEmpty()) {
                                    String userId = query.getDocuments().get(0).getId();
                                    callback.onSuccess(userId);
                                } else {
                                    callback.onFailure(new Exception("User not found in Firestore"));
                                }
                            })
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void logoutUser() {
        auth.signOut();
    }

    public void getUserById(String userId, UserCallback callback) {
        usersCollection.document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        callback.onSuccess(documentSnapshot.getData());
                    } else {
                        callback.onFailure(new Exception("User not found"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void updateUser(String userId, Map<String, Object> updates, UserCallback callback) {
        usersCollection.document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess("User updated"))
                .addOnFailureListener(callback::onFailure);
    }

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
            return password;
        }
    }

    public void getAllUsers(Consumer<List<User>> callback) {
        FirebaseFirestore.getInstance().collection("users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<User> users = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        User user = doc.toObject(User.class);
                        user.setId(doc.getId());
                        users.add(user);
                    }
                    callback.accept(users);
                });
    }

    public void updateUser(String userId, String name, String surname, Runnable onSuccess) {
        FirebaseFirestore.getInstance().collection("users").document(userId)
                .update("name", name, "surname", surname)
                .addOnSuccessListener(aVoid -> onSuccess.run());
    }

    public void deleteUser(String userId, Runnable onSuccess) {
        FirebaseFirestore.getInstance().collection("users").document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> onSuccess.run());
    }

    public interface UserCallback {
        void onSuccess(Object result);
        void onFailure(Exception e);
    }
}
