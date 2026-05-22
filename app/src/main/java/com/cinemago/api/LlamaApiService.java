package com.cinemago.api;

import com.cinemago.models.LlamaChatRequest;
import com.cinemago.models.LlamaChatResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface LlamaApiService {
    @POST("api/chat")
    Call<LlamaChatResponse> chat(@Body LlamaChatRequest request);
}