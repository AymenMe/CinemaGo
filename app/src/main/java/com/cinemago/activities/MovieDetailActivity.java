package com.cinemago.activities;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.cinemago.R;
import com.cinemago.firebase.FirebaseManager;
import com.cinemago.models.FavoriteMovie;
import com.cinemago.utils.Constants;

public class MovieDetailActivity extends AppCompatActivity {

    private boolean isFavorite = false;
    private ImageButton btnFavorite;
    private String movieId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        // Get data from intent
        movieId = String.valueOf(getIntent().getIntExtra(Constants.EXTRA_MOVIE_ID, 0));
        String title    = getIntent().getStringExtra(Constants.EXTRA_MOVIE_TITLE);
        String poster   = getIntent().getStringExtra(Constants.EXTRA_MOVIE_POSTER);
        String overview = getIntent().getStringExtra(Constants.EXTRA_MOVIE_OVERVIEW);
        double rating   = getIntent().getDoubleExtra(Constants.EXTRA_MOVIE_RATING, 0.0);

        // Bind views
        TextView tvTitle    = findViewById(R.id.tv_title);
        TextView tvOverview = findViewById(R.id.tv_overview);
        TextView tvRating   = findViewById(R.id.tv_rating);
        RatingBar ratingBar = findViewById(R.id.rating_bar);
        ImageView ivPoster  = findViewById(R.id.iv_poster);
        btnFavorite = findViewById(R.id.btn_favorite);
        ImageButton btnBack = findViewById(R.id.btn_back);

        tvTitle.setText(title);
        tvOverview.setText(overview);
        tvRating.setText(String.format("%.1f / 10", rating));
        ratingBar.setRating((float) (rating / 2));

        Glide.with(this).load(Constants.TMDB_IMAGE_BASE + poster)
                .centerCrop().into(ivPoster);

        btnBack.setOnClickListener(v -> finish());

        // Check favorite status
        FirebaseManager.getInstance().isFavorite(movieId, exists -> {
            isFavorite = exists;
            updateFavoriteIcon();
        });

        btnFavorite.setOnClickListener(v -> toggleFavorite(title, poster, rating));
    }

    private void toggleFavorite(String title, String poster, double rating) {
        if (isFavorite) {
            FirebaseManager.getInstance().removeFavorite(movieId, new FirebaseManager.SimpleCallback() {
                @Override
                public void onSuccess() {
                    isFavorite = false;
                    updateFavoriteIcon();
                    Toast.makeText(MovieDetailActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onError(String error) {
                    Toast.makeText(MovieDetailActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            String uid = FirebaseManager.getInstance().getCurrentUser().getUid();
            FavoriteMovie fav = new FavoriteMovie(movieId, title,
                    Constants.TMDB_IMAGE_BASE + poster, rating, uid);

            FirebaseManager.getInstance().addFavorite(fav, new FirebaseManager.SimpleCallback() {
                @Override
                public void onSuccess() {
                    isFavorite = true;
                    updateFavoriteIcon();
                    Toast.makeText(MovieDetailActivity.this, "Added to favorites!", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onError(String error) {
                    Toast.makeText(MovieDetailActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateFavoriteIcon() {
        btnFavorite.setImageResource(
                isFavorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
    }
}