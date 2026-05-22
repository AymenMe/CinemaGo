package com.cinemago.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cinemago.R;
import com.cinemago.adapters.ChatAdapter;
import com.cinemago.chatbot.LlamaChatManager;
import com.cinemago.models.ChatMessage;
import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    private ChatAdapter adapter;
    private List<ChatMessage> messages;
    private EditText etMessage;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private LlamaChatManager chatManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        messages = new ArrayList<>();
        chatManager = new LlamaChatManager();

        recyclerView = view.findViewById(R.id.rv_chat);
        etMessage = view.findViewById(R.id.et_message);
        progressBar = view.findViewById(R.id.progress_bar);
        ImageButton btnSend = view.findViewById(R.id.btn_send);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new ChatAdapter(getContext(), messages);
        recyclerView.setAdapter(adapter);

        if (messages.isEmpty()) {
            addAiMessage("👋 Hi! I'm CineBot (powered by Llama 3.2). Ask me anything about movies!");
        }

        btnSend.setOnClickListener(v -> sendMessage());

        return view;
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        etMessage.setText("");
        addUserMessage(text);
        progressBar.setVisibility(View.VISIBLE);

        chatManager.sendMessage(text, new LlamaChatManager.ChatCallback() {
            @Override
            public void onResponse(String response) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        addAiMessage(response);
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Llama Error: " + error, Toast.LENGTH_LONG).show();
                        addAiMessage("Sorry, I can't talk right now. Is Ollama running?");
                    });
                }
            }
        });
    }

    private void addUserMessage(String text) {
        messages.add(new ChatMessage(text, true));
        adapter.notifyItemInserted(messages.size() - 1);
        recyclerView.scrollToPosition(messages.size() - 1);
    }

    private void addAiMessage(String text) {
        messages.add(new ChatMessage(text, false));
        adapter.notifyItemInserted(messages.size() - 1);
        recyclerView.scrollToPosition(messages.size() - 1);
    }
}