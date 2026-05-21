package com.cinemago.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.cinemago.R;
import com.cinemago.models.Movie;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    public interface OnMovieClickListener {
        void onMovieClick(Movie movie);
    }

    private List<Movie> movies;
    private final Context context;
    private final OnMovieClickListener listener;

    public MovieAdapter(Context context, List<Movie> movies, OnMovieClickListener listener) {
        this.context = context;
        this.movies = movies;
        this.listener = listener;
    }

    public void updateMovies(List<Movie> newMovies) {
        this.movies = newMovies;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movies.get(position);
        holder.bind(movie);
    }

    @Override
    public int getItemCount() { return movies != null ? movies.size() : 0; }

    class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title, rating;
        RatingBar ratingBar;

        MovieViewHolder(View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.iv_poster);
            title = itemView.findViewById(R.id.tv_title);
            rating = itemView.findViewById(R.id.tv_rating);
            ratingBar = itemView.findViewById(R.id.rating_bar);
        }

        void bind(Movie movie) {
            title.setText(movie.getTitle());
            double r = movie.getVoteAverage();
            rating.setText(String.format("%.1f", r));
            ratingBar.setRating((float) (r / 2));

            Glide.with(context)
                    .load(movie.getFullPosterUrl())
                    .placeholder(R.drawable.ic_movie_placeholder)
                    .centerCrop()
                    .into(poster);

            itemView.setOnClickListener(v -> listener.onMovieClick(movie));
        }
    }
}