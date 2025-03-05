package com.hamidat.nullpointersapp.mainFragments;

import com.google.android.gms.maps.model.LatLngBounds;
import com.hamidat.nullpointersapp.MainActivity;
import com.hamidat.nullpointersapp.R;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.clustering.ClusterManager;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.models.moodHistory;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;
import com.hamidat.nullpointersapp.utils.mapUtils.AppEventBus;
import com.hamidat.nullpointersapp.utils.mapUtils.EmotionAdapter;
import com.hamidat.nullpointersapp.utils.mapUtils.MoodClusterItem;
import com.hamidat.nullpointersapp.utils.mapUtils.MoodClusterRenderer;
import com.hamidat.nullpointersapp.utils.networkUtils.NetworkMonitor;

import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A Fragment that displays a map with mood markers and filters.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private LatLng currentLocation;
    private ClusterManager<MoodClusterItem> clusterManager;
    private List<MoodClusterItem> allDummyItems = new ArrayList<>();
    private RecyclerView emotionListView;
    private SwitchMaterial showNearbySwitch;
    private SwitchMaterial showLast7DaysSwitch;
    private boolean isLast7DaysFilter;
    private View infoWindow;
    private boolean isInfoWindowVisible = false;
    private View emotionListContainer;
    private Set<String> selectedMoods = new HashSet<>();
    private Switch allSwitch;
    private EmotionAdapter adapter;
    private boolean isEventRegistered = false;


    // Executors for filtering and geocoding
    private final ExecutorService filterExecutor = Executors.newCachedThreadPool();
    private final ExecutorService geocodeExecutor = Executors.newFixedThreadPool(2);

    // Cache for geocoding results (key is "lat,lng")
    private final Map<String, String> geocodeCache = new HashMap<>();

    // Date filter: only events on the selected day are shown.
    // If no date is selected, only today's events are shown.
    private Date selectedDate = null;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // Handler and Runnable for debouncing filtering tasks.
    private final Handler filterHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingFilterRunnable;
    private NetworkMonitor networkMonitor;
    private FirestoreHelper firestoreHelper;
    private String currentUserId;
    private boolean isFirstLoad = true;

    @Override
    public void onResume() {
        super.onResume();
        // Check for location permissions each time the fragment resumes
        if (checkLocationPermission()) {
            if (currentLocation == null) {
                getLastLocation();
            }
        } else {
            requestLocationPermission();
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        if (!isEventRegistered) {
            AppEventBus.getInstance().register(this);
            isEventRegistered = true;
        }
    }

    @Subscribe
    public void onMoodAddedEvent(AppEventBus.MoodAddedEvent event) {
        fetchMoodData();
    }


    @Override
    public void onStop() {
        super.onStop();
        if (isEventRegistered) {
            AppEventBus.getInstance().unregister(this);
            isEventRegistered = false;
        }
    }

    /**
     * Inflates the fragment layout.
     *
     * @param inflater LayoutInflater instance
     * @param container Parent container
     * @param savedInstanceState Saved instance state
     * @return The root view of the fragment
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment (you may need to create fragment_map.xml based on activity_map.xml)
        return inflater.inflate(R.layout.fragment_map, container, false);

    }

    /**
     * Initializes UI components and permissions.
     *
     * @param view The root view of the fragment
     * @param savedInstanceState Saved instance state
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {



        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            this.firestoreHelper = mainActivity.getFirestoreHelper();
            this.currentUserId = mainActivity.getCurrentUserId();
        }


        showNearbySwitch = view.findViewById(R.id.showNearbySwitch);
        // Bind the new Last 7 Days switch (ensure it's added to your XML layout)
        showLast7DaysSwitch = view.findViewById(R.id.showLast7DaysSwitch);
        showLast7DaysSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isLast7DaysFilter = isChecked;
            // Clear any manually selected date if switching to last 7 days
            selectedDate = null;
            filterAndDisplayMoodEventsAsync(showNearbySwitch.isChecked(), currentLocation);
        });





        networkMonitor = new NetworkMonitor(requireContext());
        networkMonitor.startMonitoring();

        // Initialize UI components.
        showNearbySwitch = view.findViewById(R.id.showNearbySwitch);
        FloatingActionButton fab = view.findViewById(R.id.fab_filter);
        emotionListContainer = view.findViewById(R.id.emotion_list_container);
        // Ensure the filter list is hidden by default
        emotionListContainer.setVisibility(View.GONE);
        emotionListView = emotionListContainer.findViewById(R.id.emotion_list);

        // Header click now shows the calendar dialog.
        LinearLayout headerContainer = emotionListContainer.findViewById(R.id.header_container);
        headerContainer.setOnClickListener(v -> showCalendarDialog());

        // Close button listener.
        ImageButton closeButton = emotionListContainer.findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> emotionListContainer.setVisibility(View.GONE));

        // Info Window setup.
        ViewGroup rootView = (ViewGroup) view.findViewById(android.R.id.content);
        // If rootView is null, fallback to the fragment's view
        if (rootView == null) {
            rootView = (ViewGroup) view;
        }
        infoWindow = LayoutInflater.from(getContext()).inflate(R.layout.info_window, rootView, false);
        infoWindow.setVisibility(View.GONE);
        rootView.addView(infoWindow);

        // FAB click toggles the emotion list container.
        fab.setOnClickListener(v -> {
            if (emotionListContainer.getVisibility() == View.VISIBLE) {
                emotionListContainer.setVisibility(View.GONE);
            } else {
                if (isInfoWindowVisible) {
                    infoWindow.setVisibility(View.GONE);
                    isInfoWindowVisible = false;
                }
                emotionListContainer.setVisibility(View.VISIBLE);
            }
        });

        // Check location permissions.
        if (checkLocationPermission()) {
            getLastLocation();
        } else {
            requestLocationPermission();
        }

        // Map setup.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Nearby switch listener.
        showNearbySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentLocation != null) {
                filterAndDisplayMoodEventsAsync(isChecked, currentLocation);
            }
        });

        setupEmotionList();
    }

    /**
     * Sets up emotion filter list with checkboxes and switch.
     */
    private void setupEmotionList() {
        List<String> emotions = Arrays.asList("Happy", "Sad", "Angry", "Chill");
        allSwitch = emotionListContainer.findViewById(R.id.all_switch);
        Button doneButton = emotionListContainer.findViewById(R.id.done_button);

        adapter = new EmotionAdapter(emotions, allSwitch);

        // All switch listener.
        allSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (isChecked) {
                    adapter.updateCheckboxesState(true);
                    selectedMoods.clear();
                    selectedMoods.addAll(adapter.getSelectedEmotions());
                } else {
                    adapter.clearSelections();
                    selectedMoods.clear();
                }
                filterAndDisplayMoodEventsAsync(showNearbySwitch.isChecked(), currentLocation);
            }
        });

        // Done button listener.
        doneButton.setOnClickListener(v -> {
            selectedMoods = adapter.getSelectedEmotions();
            filterAndDisplayMoodEventsAsync(showNearbySwitch.isChecked(), currentLocation);
            emotionListContainer.setVisibility(View.GONE);
        });

        emotionListView.setLayoutManager(new LinearLayoutManager(getContext()));
        emotionListView.setAdapter(adapter);
    }

    /**
     * Checks if location permissions are granted.
     *
     * @return true if permissions are granted, false otherwise
     */
    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Requests fine location permission from user.
     */
    private void requestLocationPermission() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    /**
     * Handles permission request results.
     *
     * @param requestCode  Request code identifier
     * @param permissions  Requested permissions
     * @param grantResults Permission grant results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
        } else {
            Toast.makeText(getContext(), "Location permission required", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Fetches device's last known location.
     */

    private void getLastLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        if (checkLocationPermission()) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    fetchMoodData(); // Changed from generateDummyData()
                    setupMap();
                }
            });
        }
    }


    private void fetchMoodData() {
        if (currentUserId == null || firestoreHelper == null) return;

        // Fetch the current user's document to get the "following" field.
        firestoreHelper.getUser(currentUserId, new FirestoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                if (result instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> userData = (Map<String, Object>) result;
                    ArrayList<String> followingIds = (ArrayList<String>) userData.get("following");
                    if (followingIds == null) {
                        followingIds = new ArrayList<>();
                    }
                    // Include the current user's ID as well.
                    if (!followingIds.contains(currentUserId)) {
                        followingIds.add(currentUserId);
                    }
                    // Now fetch mood events for all these user IDs.
                    firestoreHelper.firebaseToMoodHistory(followingIds, new FirestoreHelper.FirestoreCallback() {
                        @Override
                        public void onSuccess(Object result) {
                            moodHistory history = (moodHistory) result;
                            updateMapData(history);
                        }
                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getContext(), "Failed to load moods: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }





//    private void updateMapData(moodHistory history) {
//        allDummyItems.clear();
//        for (Mood mood : history.getMoodArray()) {
//            // Skip invalid coordinates
//            if (mood.getLatitude() == 0.0 && mood.getLongitude() == 0.0) continue;
//
//            try {
//                LatLng position = new LatLng(mood.getLatitude(), mood.getLongitude());
//                String dateString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//                        .format(mood.getTimestamp().toDate());
//
//                allDummyItems.add(new MoodClusterItem(
//                        position,
//                        mood.getMood(),
//                        dateString,
//                        mood.getMoodDescription(),
//                        mood.getSocialSituation()
//                ));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        new Handler(Looper.getMainLooper()).post(() -> {
//            if (clusterManager != null) {
//                clusterManager.clearItems();
//                clusterManager.addItems(allDummyItems);
//                clusterManager.cluster();
//
//                // Zoom to show all markers on first load
//                if (isFirstLoad && !allDummyItems.isEmpty()) {
//                    isFirstLoad = false;
//                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
//                    for (MoodClusterItem item : allDummyItems) {
//                        builder.include(item.getPosition());
//                    }
//                    try {
//                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//    }

    private void updateMapData(moodHistory history) {
        allDummyItems.clear();
        List<LatLng> usedPositions = new ArrayList<>();
        for (Mood mood : history.getMoodArray()) {
            // Skip invalid coordinates
            if (mood.getLatitude() == 0.0 && mood.getLongitude() == 0.0) continue;

            double lat = mood.getLatitude();
            double lng = mood.getLongitude();
            LatLng originalPos = new LatLng(lat, lng);

            // Check for duplicate position
            boolean duplicate = false;
            for (LatLng pos : usedPositions) {
                if (Math.abs(pos.latitude - originalPos.latitude) < 1e-6 &&
                        Math.abs(pos.longitude - originalPos.longitude) < 1e-6) {
                    duplicate = true;
                    break;
                }
            }
            if (duplicate) {
                // Add small jitter to differentiate markers
                double jitterLat = (Math.random() - 0.5) * 0.0002;
                double jitterLng = (Math.random() - 0.5) * 0.0002;
                lat += jitterLat;
                lng += jitterLng;
            }
            LatLng position = new LatLng(lat, lng);
            usedPositions.add(position);

            // Safely format the date; use a default string if the timestamp is null
            String dateString = "Unknown Date";
            if (mood.getTimestamp() != null) {
                dateString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(mood.getTimestamp().toDate());
            }

            allDummyItems.add(new MoodClusterItem(
                    position,
                    mood.getMood(),
                    dateString,
                    mood.getMoodDescription(),
                    mood.getSocialSituation(),
                    mood.getUserId() // new parameter
            ));

        }
        new Handler(Looper.getMainLooper()).post(() -> {
            if (clusterManager != null) {
                clusterManager.clearItems();
                clusterManager.addItems(allDummyItems);
                clusterManager.cluster();

                // Zoom to show all markers on first load
                if (isFirstLoad && !allDummyItems.isEmpty()) {
                    isFirstLoad = false;
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (MoodClusterItem item : allDummyItems) {
                        builder.include(item.getPosition());
                    }
                    try {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }



    /**
     * Filters and displays events based on selected criteria (async).
     *
     * @param showNearby      True to show events within 5km
     * @param currentLocation Reference location for proximity checks
     */
    // Debounced asynchronous filtering:
    // Only events whose date (formatted as yyyy-MM-dd) exactly matches the selected day are shown.
    // If no date is selected, only today's events are shown.
    private void filterAndDisplayMoodEventsAsync(boolean showNearby, LatLng currentLocation) {
        // Cancel any pending filter task.
        if (pendingFilterRunnable != null) {
            filterHandler.removeCallbacks(pendingFilterRunnable);
        }
        pendingFilterRunnable = () -> {
            filterExecutor.execute(() -> {
                List<MoodClusterItem> filteredItems = new ArrayList<>();
                for (MoodClusterItem item : allDummyItems) {
                    double distance = SphericalUtil.computeDistanceBetween(currentLocation, item.getPosition());
                    boolean proximityMatch = !showNearby || distance <= 5000;
                    boolean emotionMatch = allSwitch.isChecked() || selectedMoods.contains(item.getEmotion());
                    boolean dateMatch = true;
                    try {
                        Date itemDate = dateFormat.parse(item.getDate());
                        if (isLast7DaysFilter) {
                            Date today = new Date();
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(today);
                            cal.add(Calendar.DAY_OF_YEAR, -7);
                            Date sevenDaysAgo = cal.getTime();
                            // Check if the item's date is within the last 7 days (inclusive)
                            dateMatch = (itemDate.equals(sevenDaysAgo) || itemDate.after(sevenDaysAgo)) &&
                                    (itemDate.equals(today) || itemDate.before(today));
                        } else if (selectedDate != null) {
                            dateMatch = dateFormat.format(itemDate).equals(dateFormat.format(selectedDate));
                        } else {
                            Date today = new Date();
                            dateMatch = dateFormat.format(itemDate).equals(dateFormat.format(today));
                        }
                    } catch (Exception e) {
                        dateMatch = false;
                    }
                    if (emotionMatch && proximityMatch && dateMatch) {
                        filteredItems.add(item);
                    }
                }
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (clusterManager != null) {
                        clusterManager.clearItems();
                        clusterManager.addItems(filteredItems);
                        clusterManager.cluster();
                    }
                });
            });
        };
        // Debounce filtering
        filterHandler.postDelayed(pendingFilterRunnable, 300);
    }

    /**
     * Handles map readiness callback.
     *
     * @param googleMap Initialized GoogleMap instance
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (currentLocation != null) {
            setupMap();
        }
    }

    /**
     * Configures map settings and cluster manager.
     */
    private void setupMap() {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(currentLocation)
                .zoom(15)
                .tilt(0)
                .build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        clusterManager = new ClusterManager<>(requireContext(), mMap);
        mMap.setOnCameraIdleListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);
        clusterManager.setRenderer(new MoodClusterRenderer(requireContext(), mMap, clusterManager));
        clusterManager.setOnClusterItemClickListener(item -> {
            if (emotionListContainer.getVisibility() == View.VISIBLE) {
                emotionListContainer.setVisibility(View.GONE);
            }
            showInfoWindow(item);
            return true;
        });
        mMap.setOnMapClickListener(latLng -> {
            if (isInfoWindowVisible) {
                infoWindow.setVisibility(View.GONE);
                isInfoWindowVisible = false;
            }
        });
        mMap.setOnCameraMoveStartedListener(reason -> {
            if (isInfoWindowVisible) {
                infoWindow.setVisibility(View.GONE);
                isInfoWindowVisible = false;
            }
        });
    }

    /**
     * Displays an info window with event details at marker position.
     *
     * @param item MoodClusterItem to display
     */
    private void showInfoWindow(MoodClusterItem item) {
        if (isInfoWindowVisible) {
            infoWindow.setVisibility(View.GONE);
        }

        // Bind UI elements from the info window layout.
        TextView username = infoWindow.findViewById(R.id.username);
        TextView emotion = infoWindow.findViewById(R.id.emotion);
        TextView date = infoWindow.findViewById(R.id.date);
        TextView location = infoWindow.findViewById(R.id.location);
        TextView description = infoWindow.findViewById(R.id.description);
        TextView socialSituationView = infoWindow.findViewById(R.id.tvSocialSituation);

        // Fetch the mood event owner's username using the userId stored in the item.
        if (username != null) {
            username.setText("Username: Loading...");
            firestoreHelper.getUser(item.getUserId(), new FirestoreHelper.FirestoreCallback() {
                @Override
                public void onSuccess(Object result) {
                    if (result instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> userData = (Map<String, Object>) result;
                        String usernameStr = (String) userData.get("username");
                        username.setText("Username: " + usernameStr);
                    } else {
                        username.setText("Username: Unknown");
                    }
                }
                @Override
                public void onFailure(Exception e) {
                    username.setText("Username: Unavailable");
                }
            });
        }


        // Set other info window fields.
        if (emotion != null) {
            emotion.setText("Emotion: " + item.getEmotion());
        }
        if (socialSituationView != null) {
            socialSituationView.setText("Social Situation: " + item.getSocialSituation());
        }
        if (date != null) {
            date.setText("Date: " + item.getDate());
        }
        if (description != null) {
            description.setText("Description: " + item.getDescription());
        }
        if (location != null) {
            location.setText("Location: Loading...");
        }

        // Use cached geocoding if available; otherwise, fetch location details.
        String cacheKey = item.getPosition().latitude + "," + item.getPosition().longitude;
        if (geocodeCache.containsKey(cacheKey)) {
            String cachedLocation = geocodeCache.get(cacheKey);
            new Handler(Looper.getMainLooper()).post(() ->
                    location.setText("Location: " + cachedLocation));
        } else {
            geocodeExecutor.execute(() -> {
                try {
                    Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(
                            item.getPosition().latitude,
                            item.getPosition().longitude,
                            1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        String street = address.getThoroughfare();
                        final String result = (street != null ? street : "Nearby area");
                        geocodeCache.put(cacheKey, result);
                        new Handler(Looper.getMainLooper()).post(() ->
                                location.setText("Location: " + result));
                    } else {
                        new Handler(Looper.getMainLooper()).post(() ->
                                location.setText("Location: Unknown"));
                    }
                } catch (IOException | IllegalStateException e) {
                    new Handler(Looper.getMainLooper()).post(() ->
                            location.setText("Location: Unavailable"));
                } catch (Exception e) {
                    new Handler(Looper.getMainLooper()).post(() ->
                            location.setText("Location: Error"));
                }
            });
        }

        // Position the info window above the marker.
        Point screenPosition = mMap.getProjection().toScreenLocation(item.getPosition());
        infoWindow.setX(screenPosition.x - infoWindow.getWidth() / 2);
        infoWindow.setY(screenPosition.y - infoWindow.getHeight() - 100);
        infoWindow.setVisibility(View.VISIBLE);
        isInfoWindowVisible = true;
    }



    /**
     * Cleans up resources on fragment destruction.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        filterExecutor.shutdown();
        geocodeExecutor.shutdown();
        filterHandler.removeCallbacksAndMessages(null);
        if (networkMonitor != null) {
            networkMonitor.stopMonitoring();
        }
    }

    /**
     * Shows calendar dialog for date filtering.
     */
    // display calendar in a dialog
    // when a date is selected, it persists and used for filteringgggggggggggggggggggggg
    private void showCalendarDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View calendarDialogView = LayoutInflater.from(getContext()).inflate(R.layout.calendar_dialog, null);
        CalendarView dialogCalendarView = calendarDialogView.findViewById(R.id.dialog_calendar_view);
        dialogCalendarView.setMaxDate(System.currentTimeMillis());
        if (selectedDate != null) {
            dialogCalendarView.setDate(selectedDate.getTime(), false, true);
        }
        dialogCalendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth, 23, 59, 59);
            selectedDate = cal.getTime();
            // If a date is manually selected, disable the Last 7 Days filter.
            if (isLast7DaysFilter) {
                isLast7DaysFilter = false;
                showLast7DaysSwitch.setChecked(false);
            }
        });
        builder.setView(calendarDialogView);
        builder.setPositiveButton("Apply", (dialog, which) -> {
            filterAndDisplayMoodEventsAsync(showNearbySwitch.isChecked(), currentLocation);
        });
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
