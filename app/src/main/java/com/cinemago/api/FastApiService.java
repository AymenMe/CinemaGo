package com.cinemago.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

public interface FastApiService {
    // Example: Check if server is up
    @GET("/")
    Call<ResponseBody> getStatus();
    
    // Add your FastAPI endpoints here, e.g.:
    // @GET("recommendations/{user_id}")
    // Call<List<Movie>> getRecommendations(@Path("user_id") String userId);
}