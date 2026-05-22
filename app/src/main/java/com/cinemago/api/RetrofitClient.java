package com.cinemago.api;

import com.cinemago.utils.Constants;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {

    private static RetrofitClient instance;
    private final TmdbApiService apiService;
    private final LlamaApiService llamaApiService;
    private final FastApiService fastApiService;

    private RetrofitClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        // TMDB Retrofit
        Retrofit tmdbRetrofit = new Retrofit.Builder()
                .baseUrl(Constants.TMDB_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        // Llama Retrofit
        Retrofit llamaRetrofit = new Retrofit.Builder()
                .baseUrl(Constants.LLAMA_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        // FastAPI Retrofit
        Retrofit fastApiRetrofit = new Retrofit.Builder()
                .baseUrl(Constants.FASTAPI_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        apiService = tmdbRetrofit.create(TmdbApiService.class);
        llamaApiService = llamaRetrofit.create(LlamaApiService.class);
        fastApiService = fastApiRetrofit.create(FastApiService.class);
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public TmdbApiService getApiService() {
        return apiService;
    }

    public LlamaApiService getLlamaApiService() {
        return llamaApiService;
    }

    public FastApiService getFastApiService() {
        return fastApiService;
    }
}