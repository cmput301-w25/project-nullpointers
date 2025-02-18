package com.hamidat.nullpointersapp;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.List;
import android.Manifest;
import java.util.Random;

/**
 * Handles map display and mood event visualization.
 * <p>
 * Manages Google Maps integration, location permissions, and
 * mood event clustering functionality.
 * </p>
 */
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    /** Request code for location permission requests. */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private GoogleMap mMap;
    private LatLng currentLocation;
    private ClusterManager<MoodClusterItem> clusterManager;
    private List<MoodClusterItem> allDummyItems = new ArrayList<>();

    /**
     * Initializes the activity layout and components.
     *
     * @param savedInstanceState Bundle containing previous state if available
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        if (checkLocationPermission()) {
            getLastLocation();
        } else {
            requestLocationPermission();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        SwitchMaterial showNearbySwitch = findViewById(R.id.showNearbySwitch);
        showNearbySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentLocation != null) {
                filterAndDisplayMoodEvents(isChecked, currentLocation);
            }
        });
    }

    /**
     * Checks if location permissions are granted.
     *
     * @return true if either FINE or COARSE location permission is granted
     */
    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /** Requests location permissions from the user. */
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    /**
     * Handles permission request results.
     *
     * @param requestCode  The request code from permission request
     * @param permissions  The requested permissions array
     * @param grantResults The grant results array
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
        } else {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
        }
    }

    /** Retrieves the user's last known location and initializes dummy data. */
    private void getLastLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (checkLocationPermission()) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    allDummyItems = generateDummyData(currentLocation);
                    setupMap();
                }
            });
        }
    }

    /**
     * Generates dummy mood events within 10km of current location.
     *
     * @param currentLocation Base location for generating dummy events
     * @return List of generated MoodClusterItems
     */
    private List<MoodClusterItem> generateDummyData(LatLng currentLocation) {
        List<MoodClusterItem> dummyData = new ArrayList<>();
        Random random = new Random();
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int numParticipants = 20;

        for (int i = 0; i < numParticipants; i++) {
            char letter = letters.charAt(i % letters.length());
            double distance = random.nextDouble() * 10000;
            double angle = random.nextDouble() * 360;
            LatLng eventLocation = SphericalUtil.computeOffset(currentLocation, distance, angle);
            dummyData.add(new MoodClusterItem(eventLocation, String.valueOf(letter)));
        }

        // Add user's own mood event
        dummyData.add(new MoodClusterItem(currentLocation, "ME"));
        return dummyData;
    }

    /**
     * Filters and displays mood events based on proximity.
     *
     * @param showNearby       Whether to filter to 5km radius
     * @param currentLocation  Base location for distance calculations
     */
    private void filterAndDisplayMoodEvents(boolean showNearby, LatLng currentLocation) {
        clusterManager.clearItems();
        List<MoodClusterItem> filteredItems = new ArrayList<>();

        for (MoodClusterItem item : allDummyItems) {
            double distance = SphericalUtil.computeDistanceBetween(currentLocation, item.getPosition());
            if (!showNearby || distance <= 5000) {
                filteredItems.add(item);
            }
        }

        clusterManager.addItems(filteredItems);
        clusterManager.cluster();
    }

    /**
     * Called when map is ready to be used.
     *
     * @param googleMap The GoogleMap object representing the map
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (currentLocation != null) {
            setupMap();
        }
    }

    /** Configures map settings and initializes clustering. */
    private void setupMap() {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(currentLocation)
                .zoom(15)
                .tilt(0)
                .build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        clusterManager = new ClusterManager<>(this, mMap);
        clusterManager.setRenderer(new MoodClusterRenderer(this, mMap, clusterManager));
        mMap.setOnCameraIdleListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);

        clusterManager.setOnClusterItemClickListener(item -> {
            Toast.makeText(this, "Location: " + item.getPosition().toString(), Toast.LENGTH_SHORT).show();
            return true;
        });

        filterAndDisplayMoodEvents(false, currentLocation);
    }

}