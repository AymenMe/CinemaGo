package com.cinemago.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.cinemago.R;
import com.cinemago.models.FavoriteMovie;
import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavViewHolder> {

    public interface OnRemoveListener {
        void onRemove(FavoriteMovie movie, int position);
    }

    private final List<FavoriteMovie> favorites;
    private final Context context;
    private final OnRemoveListener listener;

    public FavoriteAdapter(Context context, List<FavoriteMovie> favorites, OnRemoveListener listener) {
        this.context = context;
        this.favorites = favorites;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FavViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_favorite, parent, false);
        return new FavViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FavViewHolder holder, int position) {
        FavoriteMovie movie = favorites.get(position);
        holder.title.setText(movie.getTitle());
        holder.rating.setText(String.format("★ %.1f", movie.getRating()));

        Glide.with(context).load(movie.getPosterUrl())
                .placeholder(R.drawable.ic_movie_placeholder)
                .centerCrop().into(holder.poster);

        holder.btnRemove.setOnClickListener(v -> listener.onRemove(movie, position));
    }

    @Override
    public int getItemCount() { return favorites.size(); }

    public void removeItem(int position) {
        favorites.remove(position);
        notifyItemRemoved(position);
    }

    static class FavViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title, rating;
        ImageButton btnRemove;

        FavViewHolder(View v) {
            super(v);
            poster = v.findViewById(R.id.iv_poster);
            title = v.findViewById(R.id.tv_title);
            rating = v.findViewById(R.id.tv_rating);
            btnRemove = v.findViewById(R.id.btn_remove);
        }
    }
}