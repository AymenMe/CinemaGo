package com.cinemago.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cinemago.R;
import com.cinemago.adapters.CinemaAdapter;
import com.cinemago.models.Cinema;
import com.cinemago.utils.Constants;
import com.cinemago.utils.LocationHelper;
import com.cinemago.utils.PermissionHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
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

        // Init map
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        // Init Places
        if (!Places.isInitialized())
            Places.initialize(requireContext(), Constants.GOOGLE_MAPS_API_KEY);
        placesClient = Places.createClient(requireContext());

        locationHelper = new LocationHelper(requireContext());

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.setMapStyle(
                com.google.android.gms.maps.model.MapStyleOptions.loadRawResourceStyle(
                        requireContext(), R.raw.map_style_dark));

        if (PermissionHelper.hasLocationPermission(requireActivity())) {
            loadUserLocationAndCinemas();
        } else {
            PermissionHelper.requestLocation(requireActivity());
        }
    }

    private void loadUserLocationAndCinemas() {
        progressBar.setVisibility(View.VISIBLE);
        locationHelper.getCurrentLocation(new LocationHelper.LocationCallback() {
            @Override
            public void onLocationReceived(android.location.Location location) {
                LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                // Move camera to user location
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
        double delta = 0.05; // ~5km radius
        RectangularBounds bounds = RectangularBounds.newInstance(
                new LatLng(center.latitude - delta, center.longitude - delta),
                new LatLng(center.latitude + delta, center.longitude + delta));

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
                    cinemaAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Cinema search failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}