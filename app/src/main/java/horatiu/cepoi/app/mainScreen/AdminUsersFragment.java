package horatiu.cepoi.app.mainScreen;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import horatiu.cepoi.app.R;
import horatiu.cepoi.app.data.UserAdapter;
import horatiu.cepoi.app.data.models.User;
import horatiu.cepoi.app.repositories.UserRepository;

public class AdminUsersFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> userList = new ArrayList<>();
    private UserRepository userRepository;
    private String currentUserId;

    public AdminUsersFragment(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_users, container, false);

        if (!currentUserId.equals("3VDP7Aoj2cT5DNHSu4t6")) {
            Toast.makeText(getContext(), "Access denied", Toast.LENGTH_SHORT).show();
            return view;
        }

        recyclerView = view.findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UserAdapter(userList, user -> showEditDialog(user), user -> deleteUser(user));
        recyclerView.setAdapter(adapter);

        userRepository = new UserRepository();
        loadUsers();

        return view;
    }

    private void loadUsers() {
        userRepository.getAllUsers(users -> {
            userList.clear();
            userList.addAll(users);
            adapter.notifyDataSetChanged();
        });
    }

    private void showEditDialog(User user) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_user, null);
        EditText editName = dialogView.findViewById(R.id.editName);
        EditText editSurname = dialogView.findViewById(R.id.editSurname);

        editName.setText(user.getName());
        editSurname.setText(user.getSurname());

        new AlertDialog.Builder(getContext())
                .setTitle("Edit user")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = editName.getText().toString();
                    String newSurname = editSurname.getText().toString();
                    user.setName(newName);
                    user.setSurname(newSurname);
                    userRepository.updateUser(user.getId(), newName, newSurname, this::loadUsers);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUser(User user) {
        new AlertDialog.Builder(getContext())
                .setTitle("Confirm deletion")
                .setMessage("You are about to delete the user " + user.getName() + " " + user.getSurname() + ". Are you sure?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    userRepository.deleteUser(user.getId(), this::loadUsers);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
