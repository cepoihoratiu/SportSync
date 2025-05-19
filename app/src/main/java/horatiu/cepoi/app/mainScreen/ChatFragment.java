package horatiu.cepoi.app.mainScreen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import horatiu.cepoi.app.R;
import horatiu.cepoi.app.data.ChatAdapter;
import horatiu.cepoi.app.data.models.ChatMessage;
import horatiu.cepoi.app.data.ChatViewModel;

import java.util.ArrayList;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerViewChat;
    private TextInputEditText editTextMessage;
    private Button buttonSend;
    private ChatAdapter chatAdapter;
    private ChatViewModel chatViewModel;

    public ChatFragment() {
        // Constructor public gol necesar
    }

    public static ChatFragment newInstance(String userId) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        fragment.setArguments(args);
        return fragment;
    }

    private String userId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerViewChat = view.findViewById(R.id.recyclerViewChat);
        editTextMessage = view.findViewById(R.id.editTextMessage);
        buttonSend = view.findViewById(R.id.buttonSend);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inițializați ViewModel
        // Asigurați-vă că aveți dependența androidx.lifecycle:lifecycle-viewmodel-ktx (chiar și pentru Java)
        // sau folosiți ViewModelProviders.of(this) pentru versiuni mai vechi.
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        setupRecyclerView();

        chatViewModel.getMessages().observe(getViewLifecycleOwner(), messages -> {
            // Trimite o copie a listei pentru DiffUtil pentru a evita modificarea listei originale
            chatAdapter.submitList(new ArrayList<>(messages));
            if (messages != null && !messages.isEmpty()) {
                recyclerViewChat.smoothScrollToPosition(messages.size() - 1);
            }
        });

        buttonSend.setOnClickListener(v -> {
            String messageText = "";
            if (editTextMessage.getText() != null) {
                messageText = editTextMessage.getText().toString().trim();
            }

            if (!messageText.isEmpty()) {
                chatViewModel.sendMessage(messageText);
                if (editTextMessage.getText() != null) {
                    editTextMessage.getText().clear();
                }
            } else {
                Toast.makeText(getContext(), "Mesajul nu poate fi gol", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter();
        recyclerViewChat.setAdapter(chatAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        recyclerViewChat.setLayoutManager(layoutManager);
    }
}