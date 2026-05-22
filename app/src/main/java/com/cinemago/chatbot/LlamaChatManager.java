package com.cinemago.chatbot;

import android.util.Log;
import com.cinemago.api.RetrofitClient;
import com.cinemago.models.LlamaChatRequest;
import com.cinemago.models.LlamaChatResponse;
import com.cinemago.utils.Constants;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LlamaChatManager {

    public interface ChatCallback {
        void onResponse(String response);
        void onError(String error);
    }

    private static final String SYSTEM_PROMPT = 
            "You are CineBot, a movie expert. Help users with movie questions. Keep it brief.";

    public void sendMessage(String userMessage, ChatCallback callback) {
        List<LlamaChatRequest.Message> messages = new ArrayList<>();
        messages.add(new LlamaChatRequest.Message("system", SYSTEM_PROMPT));
        messages.add(new LlamaChatRequest.Message("user", userMessage));

        LlamaChatRequest request = new LlamaChatRequest(Constants.LLAMA_MODEL, messages, false);

        RetrofitClient.getInstance().getLlamaApiService().chat(request).enqueue(new Callback<LlamaChatResponse>() {
            @Override
            public void onResponse(Call<LlamaChatResponse> call, Response<LlamaChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResponse(response.body().getMessage().getContent());
                } else {
                    String error = "Server error: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            error += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {}
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<LlamaChatResponse> call, Throwable t) {
                Log.e("Llama", "Connection Failed", t);
                callback.onError("Connection Failed: " + t.getMessage());
            }
        });
    }
}