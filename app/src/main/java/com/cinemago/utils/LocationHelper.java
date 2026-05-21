package com.cinemago.utils;

import android.content.Context;
import android.location.Location;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

public class LocationHelper {

    public interface LocationCallback {
        void onLocationReceived(Location location);
        void onLocationFailed(String error);
    }

    private final FusedLocationProviderClient fusedClient;

    public LocationHelper(Context context) {
        fusedClient = LocationServices.getFusedLocationProviderClient(context);
    }

    @SuppressWarnings("MissingPermission")
    public void getCurrentLocation(LocationCallback callback) {
        CancellationTokenSource cts = new CancellationTokenSource();
        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.getToken())
                .addOnSuccessListener(location -> {
                    if (location != null) callback.onLocationReceived(location);
                    else callback.onLocationFailed("Location unavailable");
                })
                .addOnFailureListener(e -> callback.onLocationFailed(e.getMessage()));
    }
}