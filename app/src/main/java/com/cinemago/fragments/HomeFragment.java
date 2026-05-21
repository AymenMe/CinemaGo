package com.cinemago.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cinemago.R;
import com.cinemago.activities.CameraActivity;
import com.cinemago.activities.MovieDetailActivity;
import com.cinemago.adapters.MovieAdapter;
import com.cinemago.api.RetrofitClient;
import com.cinemago.models.Movie;
import com.cinemago.models.MovieResponse;
import com.cinemago.utils.Constants;
import java.util.ArrayList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private MovieAdapter adapter;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        progressBar = view.findViewById(R.id.progress_bar);
        RecyclerView recyclerView = view.findViewById(R.id.rv_movies);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        adapter = new MovieAdapter(getContext(), new ArrayList<>(), movie -> {
            Intent intent = new Intent(getActivity(), MovieDetailActivity.class);
            intent.putExtra(Constants.EXTRA_MOVIE_ID, movie.getId());
            intent.putExtra(Constants.EXTRA_MOVIE_TITLE, movie.getTitle());
            intent.putExtra(Constants.EXTRA_MOVIE_POSTER, movie.getPosterPath());
            intent.putExtra(Constants.EXTRA_MOVIE_OVERVIEW, movie.getOverview());
            intent.putExtra(Constants.EXTRA_MOVIE_RATING, movie.getVoteAverage());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // Camera FAB
        view.findViewById(R.id.fab_camera).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), CameraActivity.class)));

        loadTrendingMovies();
        return view;
    }

    private void loadTrendingMovies() {
        progressBar.setVisibility(View.VISIBLE);

        RetrofitClient.getInstance().getApiService()
                .getTrendingMovies(Constants.TMDB_API_KEY)
                .enqueue(new Callback<MovieResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MovieResponse> call,
                                           @NonNull Response<MovieResponse> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.updateMovies(response.body().getResults());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Failed to load movies", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}