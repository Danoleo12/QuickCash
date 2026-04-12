package com.example.development_01.core.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.development_01.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore db;
    private static final String TAG = "MapsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        db = FirebaseFirestore.getInstance();

        // Set up Back Button
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Safety check for Google Play Services
        if (checkGooglePlayServices()) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }
        }
    }

    private boolean checkGooglePlayServices() {
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        int result = availability.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (availability.isUserResolvableError(result)) {
                availability.getErrorDialog(this, result, 9000).show();
            } else {
                Toast.makeText(this, "Google Play Services required.", Toast.LENGTH_LONG).show();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Fetch jobs and place markers
        displayJobsOnMap();
    }

    private void displayJobsOnMap() {
        db.collection("jobs").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) {
                Log.d(TAG, "No jobs found in Firestore collection 'jobs'");
                return;
            }

            // Run geocoding in a background thread to keep UI smooth
            new Thread(() -> {
                Geocoder geocoder = new Geocoder(this);
                List<LatLng> capturedLocations = new ArrayList<>();

                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    String title = doc.getString("title");
                    String locationName = doc.getString("location");
                    String description = doc.getString("description");
                    Long pay = doc.getLong("pay");

                    // Only process jobs that have a location string
                    if (locationName != null && !locationName.trim().isEmpty()) {
                        try {
                            List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                Address address = addresses.get(0);
                                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                                capturedLocations.add(latLng);

                                // Add marker to map on UI thread
                                runOnUiThread(() -> {
                                    if (mMap != null) {
                                        String details = "Pay: $" + (pay != null ? pay : "N/A");
                                        if (description != null && !description.isEmpty()) {
                                            details += " | " + description;
                                        }

                                        mMap.addMarker(new MarkerOptions()
                                                .position(latLng)
                                                .title(title != null ? title : "Available Job")
                                                .snippet(details));
                                    }
                                });
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Geocoder failed for: " + locationName, e);
                        }
                    }
                }

                // Auto-adjust camera to fit all markers found
                if (!capturedLocations.isEmpty()) {
                    runOnUiThread(() -> {
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (LatLng loc : capturedLocations) {
                            builder.include(loc);
                        }
                        LatLngBounds bounds = builder.build();
                        int padding = 150; // padding around the markers in pixels
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
                    });
                }
            }).start();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error fetching from Firestore", e);
            Toast.makeText(this, "Failed to load job data.", Toast.LENGTH_SHORT).show();
        });
    }
}