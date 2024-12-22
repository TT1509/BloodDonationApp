package com.example.blooddonationapp.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Button focusLocationButton;

    // List to hold markers for donation sites
    private final List<Marker> donationSiteMarkers = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    @Nullable
    @Override
    public android.view.View onCreateView(@NonNull android.view.LayoutInflater inflater, @Nullable android.view.ViewGroup container, @Nullable Bundle savedInstanceState) {
        android.view.View rootView = inflater.inflate(R.layout.fragment_maps, container, false);

        // Set up the SupportMapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        focusLocationButton = rootView.findViewById(R.id.focusLocationButton);
        focusLocationButton.setOnClickListener(v -> focusOnCurrentLocation());

        return rootView;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);

            // Get the user's current location
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            moveToUserLocation(location);
                        } else {
                            LatLng defaultLocation = new LatLng(-34, 151);
                            mMap.addMarker(new MarkerOptions().position(defaultLocation).title("Default Location"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));
                        }
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                    });

            // Load donation site markers
            loadDonationSites();
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    private void moveToUserLocation(@NonNull Location location) {
        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        mMap.clear();
        addDonationSiteMarkers();

        mMap.addMarker(new MarkerOptions().position(userLatLng).title("Your Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15));
    }

    private void focusOnCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            moveToUserLocation(location);
                        } else {
                            LatLng defaultLocation = new LatLng(-34, 151);
                            mMap.addMarker(new MarkerOptions().position(defaultLocation).title("Default Location"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));
                        }
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                    });
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    private void loadDonationSites() {
        // Initialize Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query the 'donation_sites' collection
        db.collection("donation_sites")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Extract latitude, longitude, and name
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
                        // Log or handle query failure
                        task.getException().printStackTrace();
                    }
                });
    }

    private void addDonationSiteMarkers() {
        for (Marker marker : donationSiteMarkers) {
            LatLng position = marker.getPosition();
            String title = marker.getTitle();

            mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(title));
        }
    }
}
