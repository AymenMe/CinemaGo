package com.cinemago.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cinemago.R;
import com.cinemago.activities.LoginActivity;
import com.cinemago.adapters.FavoriteAdapter;
import com.cinemago.firebase.FirebaseManager;
import com.cinemago.models.FavoriteMovie;
import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private FavoriteAdapter adapter;
    private List<FavoriteMovie> favorites;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        TextView tvEmail  = view.findViewById(R.id.tv_email);
        Button btnLogout  = view.findViewById(R.id.btn_logout);
        RecyclerView rv   = view.findViewById(R.id.rv_favorites);

        favorites = new ArrayList<>();
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FavoriteAdapter(getContext(), favorites, (movie, position) -> {
            FirebaseManager.getInstance().removeFavorite(movie.getMovieId(),
                    new FirebaseManager.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            adapter.removeItem(position);
                            Toast.makeText(getContext(), "Removed from favorites",
                                    Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onError(String error) {
                            Toast.makeText(getContext(), "Error: " + error,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        rv.setAdapter(adapter);

        // Show email
        com.google.firebase.auth.FirebaseUser user =
                FirebaseManager.getInstance().getCurrentUser();
        if (user != null) tvEmail.setText(user.getEmail());

        // Logout
        btnLogout.setOnClickListener(v -> {
            FirebaseManager.getInstance().logout();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            requireActivity().finish();
        });

        loadFavorites();
        return view;
    }

    private void loadFavorites() {
        FirebaseManager.getInstance().getFavorites(new FirebaseManager.FavoritesCallback() {
            @Override
            public void onSuccess(List<FavoriteMovie> list) {
                favorites.clear();
                favorites.addAll(list);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Failed to load favorites", Toast.LENGTH_SHORT).show();
            }
        });
    }
}