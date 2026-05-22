package com.cinemago.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cinemago.R;
import com.cinemago.adapters.CinemaAdapter;
import com.cinemago.models.Cinema;
import com.cinemago.utils.Constants;
import com.cinemago.utils.LocationHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.CircularBounds;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.net.SearchNearbyRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private LocationHelper locationHelper;
    private PlacesClient placesClient;
    private CinemaAdapter cinemaAdapter;
    private final List<Cinema> cinemaList = new ArrayList<>();
    private ProgressBar progressBar;

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if (fineLocationGranted != null && fineLocationGranted) {
                    loadUserLocationAndCinemas();
                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                    loadUserLocationAndCinemas();
                } else {
                    Toast.makeText(getContext(), "Location permission is required for maps", Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        progressBar = view.findViewById(R.id.progress_bar);
        RecyclerView rv = view.findViewById(R.id.rv_cinemas);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        cinemaAdapter = new CinemaAdapter(getContext(), cinemaList);
        rv.setAdapter(cinemaAdapter);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        if (!Places.isInitialized())
            Places.initialize(requireContext(), Constants.GOOGLE_MAPS_API_KEY);
        placesClient = Places.createClient(requireContext());

        locationHelper = new LocationHelper(requireContext());

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        try {
            googleMap.setMapStyle(
                    com.google.android.gms.maps.model.MapStyleOptions.loadRawResourceStyle(
                            requireContext(), R.raw.map_style_dark));
        } catch (Exception e) {
            // Style might be missing or invalid
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            loadUserLocationAndCinemas();
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void loadUserLocationAndCinemas() {
        progressBar.setVisibility(View.VISIBLE);
        locationHelper.getCurrentLocation(new LocationHelper.LocationCallback() {
            @Override
            public void onLocationReceived(android.location.Location location) {
                if (googleMap == null) return;
                LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 13f));
                googleMap.addMarker(new MarkerOptions()
                        .position(userLatLng)
                        .title("You are here")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                searchNearbyCinemas(userLatLng);
            }

            @Override
            public void onLocationFailed(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Location error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchNearbyCinemas(LatLng center) {
        // Updated to use CircularBounds as required by the new Places SDK SearchNearbyRequest
        CircularBounds bounds = CircularBounds.newInstance(center, 5000.0); // 5km radius

        List<Place.Field> fields = Arrays.asList(
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG,
                Place.Field.RATING,
                Place.Field.ID);

        SearchNearbyRequest request = SearchNearbyRequest.builder(bounds, fields)
                .setIncludedTypes(Arrays.asList("movie_theater"))
                .setMaxResultCount(20)
                .build();

        placesClient.searchNearby(request)
                .addOnSuccessListener(response -> {
                    progressBar.setVisibility(View.GONE);
                    cinemaList.clear();
                    if (googleMap != null) {
                        for (Place place : response.getPlaces()) {
                            LatLng latLng = place.getLatLng();
                            if (latLng == null) continue;

                            Cinema cinema = new Cinema(
                                    place.getName(),
                                    place.getAddress() != null ? place.getAddress() : "",
                                    latLng.latitude, latLng.longitude,
                                    place.getRating() != null ? place.getRating().floatValue() : 0f,
                                    place.getId()
                            );
                            cinemaList.add(cinema);

                            googleMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(place.getName())
                                    .icon(BitmapDescriptorFactory.defaultMarker(
                                            BitmapDescriptorFactory.HUE_RED)));
                        }
                    }
                    cinemaAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Cinema search failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}