package com.cinemago.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cinemago.R;
import com.cinemago.adapters.ChatAdapter;
import com.cinemago.chatbot.GeminiChatManager;
import com.cinemago.models.ChatMessage;
import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    private ChatAdapter adapter;
    private List<ChatMessage> messages;
    private EditText etMessage;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private GeminiChatManager chatManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        messages = new ArrayList<>();
        chatManager = new GeminiChatManager();

        recyclerView = view.findViewById(R.id.rv_chat);
        etMessage = view.findViewById(R.id.et_message);
        progressBar = view.findViewById(R.id.progress_bar);
        ImageButton btnSend = view.findViewById(R.id.btn_send);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new ChatAdapter(getContext(), messages);
        recyclerView.setAdapter(adapter);

        // Welcome message
        addAiMessage("👋 Hi! I'm CineBot. Ask me anything about movies — recommendations, " +
                "plots, actors, directors, or what's trending!");

        btnSend.setOnClickListener(v -> sendMessage());

        return view;
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        etMessage.setText("");
        addUserMessage(text);
        progressBar.setVisibility(View.VISIBLE);

        chatManager.sendMessage(text, new GeminiChatManager.ChatCallback() {
            @Override
            public void onResponse(String response) {
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    addAiMessage(response);
                });
            }

            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    addAiMessage("Sorry, I couldn't process that. Please try again.");
                });
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