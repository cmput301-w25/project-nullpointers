package com.hamidat.nullpointersapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.os.Bundle;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.*;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private LatLng currentLocation;
    private ClusterManager<MoodClusterItem> clusterManager;
    private List<MoodClusterItem> allDummyItems = new ArrayList<>();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkLocationPermission()) {
            getLastLocation();
        } else {
            requestLocationPermission();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        SwitchMaterial showNearbySwitch = findViewById(R.id.showNearbySwitch);
        showNearbySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentLocation != null) {
                filterAndDisplayMoodEvents(isChecked, currentLocation);
            }
        });
    }

    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
        } else {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
        }
    }

    private void getLastLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (checkLocationPermission()) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                    allDummyItems = generateDummyData(currentLocation);

                    MoodClusterItem userEvent = new MoodClusterItem(currentLocation, "ME");
                    allDummyItems.add(userEvent);

                    setupMap();
                }
            });
        }
    }

    private List<MoodClusterItem> generateDummyData(LatLng currentLocation) {
        List<MoodClusterItem> dummyData = new ArrayList<>();
        Random random = new Random();
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int numParticipants = 20;

        for (int i = 0; i < numParticipants; i++) {
            char letter = letters.charAt(i % letters.length());
            double distance = random.nextDouble() * 10000; // 0-10 km
            double angle = random.nextDouble() * 360;
            LatLng eventLocation = SphericalUtil.computeOffset(currentLocation, distance, angle);
            dummyData.add(new MoodClusterItem(eventLocation, String.valueOf(letter)));
        }
        return dummyData;
    }

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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (currentLocation != null) {
            setupMap();
        }
    }

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



