package com.cinemago.chatbot;

import com.cinemago.utils.Constants;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GeminiChatManager {

    public interface ChatCallback {
        void onResponse(String response);
        void onError(String error);
    }

    private final GenerativeModelFutures model;
    private final Executor executor = Executors.newSingleThreadExecutor();

    private static final String SYSTEM_CONTEXT =
            "You are CineBot, a friendly movie expert assistant for the CinemaGo app. " +
                    "Help users discover movies, get recommendations, learn about actors and directors, " +
                    "and find cinema showtimes. Keep responses concise and engaging. " +
                    "Always relate your answers to movies and cinema.";

    public GeminiChatManager() {
        GenerativeModel gm = new GenerativeModel(
                "gemini-1.5-flash",
                Constants.GEMINI_API_KEY
        );
        model = GenerativeModelFutures.from(gm);
    }

    public void sendMessage(String userMessage, ChatCallback callback) {
        String fullPrompt = SYSTEM_CONTEXT + "\n\nUser: " + userMessage;

        Content content = new Content.Builder()
                .addText(fullPrompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String text = result.getText();
                callback.onResponse(text != null ? text : "I couldn't generate a response.");
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onError("Error: " + t.getMessage());
            }
        }, executor);
    }
}