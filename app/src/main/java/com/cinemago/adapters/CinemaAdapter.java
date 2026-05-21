package com.cinemago.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.cinemago.R;
import com.cinemago.models.Cinema;
import java.util.List;

public class CinemaAdapter extends RecyclerView.Adapter<CinemaAdapter.CinemaViewHolder> {

    private final List<Cinema> cinemas;
    private final Context context;

    public CinemaAdapter(Context context, List<Cinema> cinemas) {
        this.context = context;
        this.cinemas = cinemas;
    }

    @NonNull
    @Override
    public CinemaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_cinema, parent, false);
        return new CinemaViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CinemaViewHolder holder, int position) {
        Cinema cinema = cinemas.get(position);
        holder.name.setText(cinema.getName());
        holder.address.setText(cinema.getAddress());
        holder.ratingBar.setRating(cinema.getRating());
        holder.tvRating.setText(String.valueOf(cinema.getRating()));

        holder.btnNavigate.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse(
                    "google.navigation:q=" + cinema.getLatitude() + "," + cinema.getLongitude());
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(context.getPackageManager()) != null)
                context.startActivity(mapIntent);
        });
    }

    @Override
    public int getItemCount() { return cinemas.size(); }

    static class CinemaViewHolder extends RecyclerView.ViewHolder {
        TextView name, address, tvRating;
        RatingBar ratingBar;
        ImageButton btnNavigate;

        CinemaViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_cinema_name);
            address = itemView.findViewById(R.id.tv_cinema_address);
            ratingBar = itemView.findViewById(R.id.rating_bar);
            tvRating = itemView.findViewById(R.id.tv_rating);
            btnNavigate = itemView.findViewById(R.id.btn_navigate);
        }
    }
}