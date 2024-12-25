package com.example.blooddonationapp.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.blooddonationapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Button focusLocationButton, findRouteButton;
    private final List<Marker> donationSiteMarkers = new ArrayList<>();
    private Marker selectedMarker;
    private Polyline currentRoute;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    @Nullable
    @Override
    public android.view.View onCreateView(@NonNull android.view.LayoutInflater inflater, @Nullable android.view.ViewGroup container, @Nullable Bundle savedInstanceState) {
        android.view.View rootView = inflater.inflate(R.layout.fragment_maps, container, false);

        // Initialize the buttons
        focusLocationButton = rootView.findViewById(R.id.focusLocationButton);
        findRouteButton = rootView.findViewById(R.id.findRouteButton);

        // Set click listeners
        focusLocationButton.setOnClickListener(v -> focusOnCurrentLocation());
        findRouteButton.setOnClickListener(v -> findRouteToSelectedMarker());

        // Set up the SupportMapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return rootView;
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMarkerClickListener(marker -> {
            // Highlight the selected marker
            if (selectedMarker != null) {
                selectedMarker.setAlpha(1.0f);
            }
            selectedMarker = marker;
            selectedMarker.setAlpha(0.6f);

            return false;
        });


        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                        }
                    })
                    .addOnFailureListener(e -> e.printStackTrace());
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        loadDonationSites();
    }

    private void findRouteToSelectedMarker() {
        if (selectedMarker != null && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            LatLng destination = selectedMarker.getPosition();

                            // Add logic to draw the route (e.g., using Directions API or Polyline)
                            drawRoute(userLocation, destination);
                        }
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                    });
        } else {
            // Handle case where no marker is selected or permissions are not granted
            Toast.makeText(requireContext(), "Please select a donation site marker first", Toast.LENGTH_SHORT).show();
        }
    }


    private void drawRoute(LatLng start, LatLng end) {
        // Clear existing route
        if (currentRoute != null) {
            currentRoute.remove();
        }

        // Draw new route
        PolylineOptions polylineOptions = new PolylineOptions()
                .add(start)
                .add(end)
                .width(8)
                .color(Color.BLUE);
        currentRoute = mMap.addPolyline(polylineOptions);
    }



    private void focusOnCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15));
                        }
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Failed to focus on current location", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void loadDonationSites() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("donation_sites")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Double latitude = document.getDouble("latitude");
                            Double longitude = document.getDouble("longitude");
                            String name = document.getString("name");

                            if (latitude != null && longitude != null) {
                                LatLng location = new LatLng(latitude, longitude);
                                Marker marker = mMap.addMarker(new MarkerOptions()
                                        .position(location)
                                        .title(name));
                                donationSiteMarkers.add(marker);
                            }
                        }
                    } else {
                        task.getException().printStackTrace();
                    }
                });
    }
}
