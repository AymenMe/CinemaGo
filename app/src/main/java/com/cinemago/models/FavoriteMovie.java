package com.cinemago.models;

public class FavoriteMovie {
    private String movieId;
    private String title;
    private String posterUrl;
    private double rating;
    private String userId;

    public FavoriteMovie() {}

    public FavoriteMovie(String movieId, String title, String posterUrl,
                         double rating, String userId) {
        this.movieId = movieId;
        this.title = title;
        this.posterUrl = posterUrl;
        this.rating = rating;
        this.userId = userId;
    }

    public String getMovieId() { return movieId; }
    public String getTitle() { return title; }
    public String getPosterUrl() { return posterUrl; }
    public double getRating() { return rating; }
    public String getUserId() { return userId; }
}