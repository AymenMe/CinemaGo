package com.cinemago.utils;

public class Constants {
    public static final String TMDB_BASE_URL = "https://api.themoviedb.org/3/";
    public static final String TMDB_API_KEY = "5176ae704d64442b87cd9ddce2d79742";
    
    // Llama 3.2 Configuration (Ollama)
    public static final String LLAMA_BASE_URL = "http://10.0.2.2:11434/";
    public static final String LLAMA_MODEL = "llama3.2:3b";

    // FastAPI Configuration
    public static final String FASTAPI_BASE_URL = "http://10.0.2.2:8000/";

    public static final String GOOGLE_MAPS_API_KEY = "AIzaSyDRntDHTtIdwX2t49VAgvlzPaw2achc-RU";
    public static final String TMDB_IMAGE_BASE = "https://image.tmdb.org/t/p/w500";

    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_FAVORITES = "favorites";
    public static final String COLLECTION_PHOTOS = "userPhotos";

    public static final String EXTRA_MOVIE_ID = "movie_id";
    public static final String EXTRA_MOVIE_TITLE = "movie_title";
    public static final String EXTRA_MOVIE_POSTER = "movie_poster";
    public static final String EXTRA_MOVIE_OVERVIEW = "movie_overview";
    public static final String EXTRA_MOVIE_RATING = "movie_rating";
}