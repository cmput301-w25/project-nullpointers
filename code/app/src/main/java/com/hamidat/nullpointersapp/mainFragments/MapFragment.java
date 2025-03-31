/**
 * MapFragment.java
 *
 * A fragment that displays a Google Map with mood markers using clustering.
 * Supports filtering moods by date, emotion, and proximity. Also includes
 * an animated info window to display mood details and user profile information.
 *
 * <p><b>Outstanding issues:</b> None.</p>
 */


package com.hamidat.nullpointersapp.mainFragments;

import android.Manifest;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.clustering.ClusterManager;
import com.hamidat.nullpointersapp.MainActivity;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.models.moodHistory;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;
import com.hamidat.nullpointersapp.utils.mapUtils.AppEventBus;
import com.hamidat.nullpointersapp.utils.mapUtils.MoodClusterItem;
import com.hamidat.nullpointersapp.utils.mapUtils.MoodClusterRenderer;
import com.hamidat.nullpointersapp.utils.networkUtils.NetworkMonitor;

import org.greenrobot.eventbus.Subscribe;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Fragment that displays a Google Map with mood event markers and handles map interactions.
 * Implements OnMapReadyCallback to manage the map initialization.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    // Map and location
    private GoogleMap mMap;
    private LatLng currentLocation;
    private ClusterManager<MoodClusterItem> clusterManager;
    private boolean isFirstLoad = true;
    private Date fromDate = null;
    private Date toDate = null;

    // Mood data
    private List<MoodClusterItem> allDummyItems = new ArrayList<>();

    // Filter UI
    private Switch switchShowMoodHistory;
    private boolean showMoodHistory = false;

    private View filterPanelContainer;
    private Switch showNearbySwitch;
    private Switch showLast7DaysSwitch;
    private Button selectDateButton;
    private Button applyFiltersButton;
    private TextView selectedDateDisplay;

    // Mood checkboxes
    private CheckBox cbHappy, cbSad, cbAngry, cbChill, cbFear, cbDisgust, cbShame, cbSurprise, cbConfusion;
    private Switch allSwitch;
    private Set<String> selectedMoods = new HashSet<>();

    // Date filter
    private boolean isLast7DaysFilter = true;
    private Date selectedDate = null;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // Info window
    private View infoWindow;
    private boolean isInfoWindowVisible = false;
    private Button buttonFromDate;
    private Button buttonToDate;

    // Firestore / network
    private NetworkMonitor networkMonitor;
    public FirestoreHelper firestoreHelper;
    public String currentUserId;

    // Executors for filtering & geocoding
    private final ExecutorService filterExecutor = Executors.newCachedThreadPool();
    private final ExecutorService geocodeExecutor = Executors.newFixedThreadPool(2);

    // Geocoding cache
    private final Map<String, String> geocodeCache = new HashMap<>();

    // Handler & runnable for debouncing filter tasks
    private final Handler filterHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingFilterRunnable;

    // EventBus
    private boolean isEventRegistered = false;

    /**
     * Called when the fragment is resumed. Checks and requests location permissions
     * if necessary, and retrieves the last known location.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (checkLocationPermission()) {
            if (currentLocation == null) {
                getLastLocation();
            }
        } else {
            requestLocationPermission();
        }
    }

    /**
     * Called when the fragment is started. Registers the fragment to the EventBus.
     */
    @Override
    public void onStart() {
        super.onStart();
        if (!isEventRegistered) {
            AppEventBus.getInstance().register(this);
            isEventRegistered = true;
        }
    }

    /**
     * Called when the fragment is stopped. Unregisters the fragment from the EventBus.
     */
    @Override
    public void onStop() {
        super.onStop();
        if (isEventRegistered) {
            AppEventBus.getInstance().unregister(this);
            isEventRegistered = false;
        }
    }

    /**
     * Called when the fragment is destroyed. Shuts down executors, removes pending
     * callbacks, and stops network monitoring.
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
     * Subscribed to MoodAddedEvent from EventBus. Fetches mood data when a new mood is added.
     *
     * @param event The MoodAddedEvent object.
     */
    @Subscribe
    public void onMoodAddedEvent(AppEventBus.MoodAddedEvent event) {
        fetchMoodData();
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     * UI should be attached to. The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous
     * saved state as given here.
     * @return The View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    /**
     * Called immediately after onCreateView(LayoutInflater, ViewGroup, Bundle) has returned,
     * but before any saved state has been restored in to the view.
     *
     * @param view               The View returned by onCreateView(LayoutInflater, ViewGroup, Bundle).
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous
     * saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Access Firestore from MainActivity
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            this.firestoreHelper = mainActivity.getFirestoreHelper();
            this.currentUserId = mainActivity.getCurrentUserId();
        }

        // Info window
        infoWindow = LayoutInflater.from(getContext()).inflate(R.layout.info_window, (ViewGroup) view, false);
        ((ViewGroup) view).addView(infoWindow);
        infoWindow.setVisibility(View.GONE);

        // Filter panel (assumed to be included in fragment_map.xml)
        filterPanelContainer = view.findViewById(R.id.filter_panel_container);
        if (filterPanelContainer == null) {
            // If it's null, manually inflate the filter panel layout.
            LayoutInflater inflater = LayoutInflater.from(getContext());
            filterPanelContainer = (LinearLayout) inflater.inflate(R.layout.filter_panel, (ViewGroup) view, false);
            // Add the filter panel to the root view.
            ((ViewGroup) view).addView(filterPanelContainer);
        }


        filterPanelContainer.setVisibility(View.GONE);

        // Initialize controls inside filter panel
        showNearbySwitch    = filterPanelContainer.findViewById(R.id.showNearbySwitch);
        showLast7DaysSwitch = filterPanelContainer.findViewById(R.id.showLast7DaysSwitch);
        buttonFromDate = filterPanelContainer.findViewById(R.id.buttonFromDate);
        buttonToDate = filterPanelContainer.findViewById(R.id.buttonToDate);


        applyFiltersButton  = filterPanelContainer.findViewById(R.id.apply_filters_button);
        allSwitch           = filterPanelContainer.findViewById(R.id.all_switch);
        switchShowMoodHistory = filterPanelContainer.findViewById(R.id.switchShowMoodHistory);

        // Mood checkboxes
        cbHappy    = filterPanelContainer.findViewById(R.id.checkbox_happy);
        cbSad      = filterPanelContainer.findViewById(R.id.checkbox_sad);
        cbAngry    = filterPanelContainer.findViewById(R.id.checkbox_angry);
        //cbChill    = filterPanelContainer.findViewById(R.id.checkbox_chill);
        cbFear     = filterPanelContainer.findViewById(R.id.checkbox_fear);
        cbDisgust  = filterPanelContainer.findViewById(R.id.checkbox_disgust);
        cbShame    = filterPanelContainer.findViewById(R.id.checkbox_shame);
        cbSurprise = filterPanelContainer.findViewById(R.id.checkbox_surprise);
        cbConfusion= filterPanelContainer.findViewById(R.id.checkbox_confusion);

        // Setup mood checkboxes
        setupMoodCheckboxes();

        // Listen for Last 7 Days switch
        showLast7DaysSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            applyLast7DaysToggle(isChecked);
            // Trigger filtering if needed:
            filterAndDisplayMoodEventsAsync(showNearbySwitch.isChecked(), currentLocation);
        });


        if (buttonFromDate != null && buttonToDate != null) {
            buttonFromDate.setOnClickListener(v -> openDatePicker(true, buttonFromDate));
            buttonToDate.setOnClickListener(v -> openDatePicker(false, buttonToDate));
        } else {
            // If still null, log an error and disable date-range filtering to avoid a crash.
            Log.e("MapFragment", "Date range buttons not found. Check that filter_panel.xml includes buttonFromDate and buttonToDate with the correct IDs.");
        }

        // Select date button
        //selectDateButton.setOnClickListener(v -> showCalendarDialog());

        // "Show Mood History" switch listener is not used for UI change; its state is read in fetchMoodData().

        // Close button on the filter panel
        ImageButton closeButton = filterPanelContainer.findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> hideFilterPanel());

        // Apply filters button
        applyFiltersButton.setOnClickListener(v -> {
            filterAndDisplayMoodEventsAsync(showNearbySwitch.isChecked(), currentLocation);
            hideFilterPanel();
        });


        switchShowMoodHistory.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /**
             * Called when the checked state of a compound button has changed.
             *
             * @param buttonView The compound button view whose state has changed.
             * @param isChecked  The new checked state of buttonView.
             */
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showMoodHistory = isChecked;
                Log.d("MapFragment", "switchShowMoodHistory changed to: " + isChecked);
                // Optionally trigger filtering here if immediate response is desired
                fetchMoodData();

            }
        });

        // Floating Action Button to toggle filter panel
        FloatingActionButton fab = view.findViewById(R.id.fab_filter);
        fab.setOnClickListener(v -> {
            if (filterPanelContainer.getVisibility() == View.VISIBLE) {
                hideFilterPanel();
            } else {
                if (isInfoWindowVisible) {
                    slideDownInfoWindow();
                }
                showFilterPanel();
            }
        });

        // Network monitor
        networkMonitor = new NetworkMonitor(requireContext());
        networkMonitor.startMonitoring();

        // Request or check location permission
        if (checkLocationPermission()) {
            getLastLocation();
        } else {
            requestLocationPermission();
        }

        // Set up the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Default date display to "Last 7 Days" on initial load
        //selectedDateDisplay.setText("Last 7 Days");
    }


    /**
     * Applies the "Last 7 Days" toggle, enabling or disabling date range filtering
     * based on the toggle state.
     *
     * @param isEnabled True if the "Last 7 Days" toggle is enabled, false otherwise.
     */
    private void applyLast7DaysToggle(boolean isEnabled) {
        if (isEnabled) {
            // Calculate 7 days ago:
            Calendar cal = Calendar.getInstance();
            toDate = cal.getTime(); // today
            cal.add(Calendar.DAY_OF_YEAR, -7);
            fromDate = cal.getTime();

            // Update buttons text:
            buttonFromDate.setText("From: " + new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(fromDate));
            buttonToDate.setText("To: " + new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(toDate));

            // Disable the buttons.
            buttonFromDate.setEnabled(false);
            buttonToDate.setEnabled(false);
        } else {
            // Enable buttons for user selection.
            buttonFromDate.setEnabled(true);
            buttonToDate.setEnabled(true);
            // Optionally, set the button text to prompt user to select.
            buttonFromDate.setText("From: Select Date");
            buttonToDate.setText("To: Select Date");
            // Reset the custom dates.
            fromDate = null;
            toDate = null;
        }
    }

    /**
     * Opens a DatePickerDialog to allow the user to select a date for filtering.
     *
     * @param isFromDate True if the date is for the "from" date, false for "to" date.
     * @param dateButton The button to update with the selected date.
     */
    private void openDatePicker(boolean isFromDate, Button dateButton) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(year, month, dayOfMonth, 0, 0, 0);
                    if (isFromDate) {
                        fromDate = selectedCalendar.getTime();
                        dateButton.setText("From: " + new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(fromDate));
                    } else {
                        // Set to end of day for To Date.
                        selectedCalendar.set(Calendar.HOUR_OF_DAY, 23);
                        selectedCalendar.set(Calendar.MINUTE, 59);
                        selectedCalendar.set(Calendar.SECOND, 59);
                        selectedCalendar.set(Calendar.MILLISECOND, 999);
                        toDate = selectedCalendar.getTime();
                        dateButton.setText("To: " + new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(toDate));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    /**
     * Sets up the mood checkboxes and their listeners.
     */
    private void setupMoodCheckboxes() {
        cbHappy.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) selectedMoods.add("Happy");
            else selectedMoods.remove("Happy");
            updateAllSwitchState();
        });
        cbSad.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) selectedMoods.add("Sad");
            else selectedMoods.remove("Sad");
            updateAllSwitchState();
        });
        cbAngry.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) selectedMoods.add("Angry");
            else selectedMoods.remove("Angry");
            updateAllSwitchState();
        });
//        cbChill.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            if (isChecked) selectedMoods.add("Chill");
//            else selectedMoods.remove("Chill");
//            updateAllSwitchState();
//        });
        cbFear.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) selectedMoods.add("Afraid");
            else selectedMoods.remove("Afraid");
            updateAllSwitchState();
        });
        cbDisgust.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) selectedMoods.add("Disgusted");
            else selectedMoods.remove("Disgusted");
            updateAllSwitchState();
        });
        cbShame.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) selectedMoods.add("Shameful");
            else selectedMoods.remove("Shameful");
            updateAllSwitchState();
        });
        cbSurprise.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) selectedMoods.add("Surprised");
            else selectedMoods.remove("Surprised");
            updateAllSwitchState();
        });
        cbConfusion.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) selectedMoods.add("Confused");
            else selectedMoods.remove("Confused");
            updateAllSwitchState();
        });

        allSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                cbHappy.setChecked(isChecked);
                cbSad.setChecked(isChecked);
                cbAngry.setChecked(isChecked);
                //cbChill.setChecked(isChecked);
                cbFear.setChecked(isChecked);
                cbDisgust.setChecked(isChecked);
                cbShame.setChecked(isChecked);
                cbSurprise.setChecked(isChecked);
                cbConfusion.setChecked(isChecked);
            }
        });
    }

    /**
     * Updates the state of the "All" switch based on the selected moods.
     */
    private void updateAllSwitchState() {
        boolean allSelected = selectedMoods.size() == 9;
        if (allSwitch.isChecked() != allSelected) {
            allSwitch.setChecked(allSelected);
        }
    }

    /**
     * Shows the filter panel with an animation.
     */
    private void showFilterPanel() {
        filterPanelContainer.setVisibility(View.VISIBLE);
        filterPanelContainer.setClickable(true);
        filterPanelContainer.setOnTouchListener((v, event) -> true);


        filterPanelContainer.post(() -> {
            float panelHeight = filterPanelContainer.getHeight();
            filterPanelContainer.setTranslationY(-panelHeight);
            filterPanelContainer.animate()
                    .translationY(0)
                    .setDuration(300)
                    .start();
        });
    }

    /**
     * Hides the filter panel with an animation.
     */
    private void hideFilterPanel() {
        filterPanelContainer.post(() -> {
            float panelHeight = filterPanelContainer.getHeight();
            filterPanelContainer.animate()
                    .translationY(-panelHeight)
                    .setDuration(300)
                    .withEndAction(() -> {
                        filterPanelContainer.setOnTouchListener(null);
                        filterPanelContainer.setVisibility(View.GONE);
                    })
                    .start();
        });
    }

    /**
     * Checks if the app has location permissions.
     *
     * @return True if permissions are granted, false otherwise.
     */
    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Requests location permissions from the user.
     */
    private void requestLocationPermission() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    /**
     * Handles the result of the permission request.
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     * which is either {@link android.content.pm.PackageManager#PERMISSION_GRANTED}
     * or {@link android.content.pm.PackageManager#PERMISSION_DENIED}. Never null.
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
     * Retrieves the last known location or requests location updates if the last location is null.
     */
    private void getLastLocation() {
        FusedLocationProviderClient fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(requireActivity());
        if (checkLocationPermission()) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    fetchMoodData();
                    setupMap();
                } else {
                    LocationRequest locationRequest = LocationRequest.create();
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    locationRequest.setInterval(1000);
                    locationRequest.setFastestInterval(500);
                    locationRequest.setNumUpdates(1);
                    fusedLocationClient.requestLocationUpdates(locationRequest,
                            new LocationCallback() {
                                /**
                                 * Callback invoked when a location result is available.
                                 *
                                 * @param locationResult The available location result.
                                 */
                                @Override
                                public void onLocationResult(LocationResult locationResult) {
                                    if (locationResult != null && !locationResult.getLocations().isEmpty()) {
                                        Location freshLocation = locationResult.getLastLocation();
                                        currentLocation = new LatLng(freshLocation.getLatitude(),
                                                freshLocation.getLongitude());
                                        fetchMoodData();
                                        setupMap();
                                    }
                                }
                            },
                            Looper.getMainLooper());
                }
            });
        }
    }

    /**
     * Fetches mood data from Firestore based on the current user's settings.
     */
    private void fetchMoodData() {
        if (currentUserId == null || firestoreHelper == null) return;

        if (showMoodHistory) {
            Log.d("MapFragment", "Fetching personal mood history for " + currentUserId);
            firestoreHelper.firebaseToMoodHistory(currentUserId, new FirestoreHelper.FirestoreCallback() {
                /**
                 * Called when the Firestore operation succeeds.
                 *
                 * @param result The result of the operation.
                 */
                @Override
                public void onSuccess(Object result) {
                    moodHistory history = (moodHistory) result;
                    updateMapData(history);
                }

                /**
                 * Called when the Firestore operation fails.
                 *
                 * @param e The exception that occurred.
                 */
                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Failed to load moods: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            firestoreHelper.getUser(currentUserId, new FirestoreHelper.FirestoreCallback() {
                /**
                 * Called when the Firestore operation succeeds.
                 *
                 * @param result The result of the operation.
                 */
                @Override
                public void onSuccess(Object result) {
                    if (result instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> userData = (Map<String, Object>) result;
                        ArrayList<String> followingIds = (ArrayList<String>) userData.get("following");
                        if (followingIds == null) {
                            followingIds = new ArrayList<>();
                        }
                        // Ensure the current user is included.
                        if (!followingIds.contains(currentUserId)) {
                            followingIds.add(currentUserId);
                        }
                        Log.d("MapFragment", "Fetching moods for following: " + followingIds.toString());
                        firestoreHelper.firebaseToMoodHistory(followingIds, new FirestoreHelper.FirestoreCallback() {
                            /**
                             * Called when the Firestore operation succeeds.
                             *
                             * @param result The result of the operation.
                             */
                            @Override
                            public void onSuccess(Object result) {
                                moodHistory history = (moodHistory) result;
                                updateMapData(history);
                            }

                            /**
                             * Called when the Firestore operation fails.
                             *
                             * @param e The exception that occurred.
                             */
                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(getContext(), "Failed to load moods: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                /**
                 * Called when the Firestore operation fails.
                 *
                 * @param e The exception that occurred.
                 */
                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    /**
     * Updates the map data with the fetched mood history.
     *
     * @param history The mood history to update the map with.
     */
    private void updateMapData(moodHistory history) {
        allDummyItems.clear();
        List<LatLng> usedPositions = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -7);
        Date sevenDaysAgo = cal.getTime();
        for (Mood mood : history.getMoodArray()) {
            if (mood.getLatitude() == 0.0 && mood.getLongitude() == 0.0) continue;
            if (isLast7DaysFilter && mood.getTimestamp() != null) {
                Date moodDate = mood.getTimestamp().toDate();
                if (moodDate.before(sevenDaysAgo)) {
                    continue;
                }
            }
            String dateString = "Unknown Date";
            String timeString = "Unknown Time";
            if (mood.getTimestamp() != null) {
                Date dateObj = mood.getTimestamp().toDate();
                dateString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dateObj);
                timeString = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(dateObj);
            }
            double lat = mood.getLatitude();
            double lng = mood.getLongitude();
            LatLng originalPos = new LatLng(lat, lng);
            boolean duplicate = false;
            for (LatLng pos : usedPositions) {
                if (Math.abs(pos.latitude - originalPos.latitude) < 1e-6 &&
                        Math.abs(pos.longitude - originalPos.longitude) < 1e-6) {
                    duplicate = true;
                    break;
                }
            }
            if (duplicate) {
                double jitterLat = (Math.random() - 0.5) * 0.0002;
                double jitterLng = (Math.random() - 0.5) * 0.0002;
                lat += jitterLat;
                lng += jitterLng;
            }
            LatLng position = new LatLng(lat, lng);
            usedPositions.add(position);
            allDummyItems.add(new MoodClusterItem(
                    position,
                    mood.getMood(),
                    dateString,
                    timeString,
                    mood.getMoodDescription(),
                    mood.getSocialSituation(),
                    mood.getImageBase64(),
                    mood.getUserId()
            ));
        }
        new Handler(Looper.getMainLooper()).post(() -> {
            if (clusterManager != null) {
                clusterManager.clearItems();
                clusterManager.addItems(allDummyItems);
                clusterManager.cluster();
                if (isFirstLoad && !allDummyItems.isEmpty()) {
                    isFirstLoad = false;
                }
            }
        });
    }

    /**
     * Filters and displays mood events asynchronously based on the selected filters.
     *
     * @param showNearby      True if nearby moods should be shown, false otherwise.
     * @param currentLocation The current location of the user.
     */
    private void filterAndDisplayMoodEventsAsync(boolean showNearby, LatLng currentLocation) {
        if (pendingFilterRunnable != null) {
            filterHandler.removeCallbacks(pendingFilterRunnable);
        }
        pendingFilterRunnable = () -> {
            filterExecutor.execute(() -> {
                List<MoodClusterItem> filteredItems = new ArrayList<>();
                for (MoodClusterItem item : allDummyItems) {
                    double distance = (currentLocation == null)
                            ? Double.MAX_VALUE
                            : SphericalUtil.computeDistanceBetween(currentLocation, item.getPosition());
                    boolean proximityMatch = !showNearby || distance <= 5000;
                    boolean emotionMatch = selectedMoods.contains(item.getEmotion());
                    boolean dateMatch = true;
                    try {
                        Date itemDate = dateFormat.parse(item.getDate());
                        if (fromDate != null && toDate != null) {
                            // Use custom date range filtering.
                            if (itemDate.before(fromDate) || itemDate.after(toDate)) {
                                dateMatch = false;
                            }
                        } else if (isLast7DaysFilter) {
                            Date today = new Date();
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(today);
                            cal.add(Calendar.DAY_OF_YEAR, -7);
                            Date sevenDaysAgo = cal.getTime();
                            dateMatch = !itemDate.before(sevenDaysAgo) && !itemDate.after(today);
                        } else {
                            // If no date filter is set, default to matching.
                            dateMatch = true;
                        }
                    } catch (Exception e) {
                        dateMatch = false;
                    }
                    if (proximityMatch && emotionMatch && dateMatch) {
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
        filterHandler.postDelayed(pendingFilterRunnable, 300);
    }

    /**
     * Called when the map is ready to be used.
     *
     * @param googleMap The GoogleMap object.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (currentLocation != null) {
            setupMap();
        }
    }

    /**
     * Sets up the map with camera position, cluster manager, and listeners.
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
            if (filterPanelContainer.getVisibility() == View.VISIBLE) {
                hideFilterPanel();
            }
            showInfoWindow(item);
            return true;
        });
        mMap.setOnMapClickListener(latLng -> {
            if (isInfoWindowVisible) {
                slideDownInfoWindow();
            }
        });
        mMap.setOnCameraMoveStartedListener(reason -> {
            if (isInfoWindowVisible) {
                slideDownInfoWindow();
            }
        });
    }

    /**
     * Shows the info window for a mood cluster item.
     *
     * @param item The mood cluster item to show the info window for.
     */
    private void showInfoWindow(MoodClusterItem item) {
        TextView tvUsername = infoWindow.findViewById(R.id.tvUsername);
        TextView tvDate = infoWindow.findViewById(R.id.tvDate);
        TextView tvTime = infoWindow.findViewById(R.id.tvTime);
        TextView tvEmotion = infoWindow.findViewById(R.id.tvEmotion);
        TextView tvSocialSituation = infoWindow.findViewById(R.id.tvSocialSituation);
        TextView tvDescription = infoWindow.findViewById(R.id.tvDescription);
        TextView tvLocation = infoWindow.findViewById(R.id.tvLocation);
        ImageView ivImage = infoWindow.findViewById(R.id.ivImage);

        if (tvUsername != null) {
            tvUsername.setText("Loading...");
            firestoreHelper.getUser(item.getUserId(), new FirestoreHelper.FirestoreCallback() {
                /**
                 * Called when the Firestore operation succeeds.
                 *
                 * @param result The result of the operation.
                 */
                @Override
                public void onSuccess(Object result) {
                    if (result instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> userData = (Map<String, Object>) result;
                        String usernameStr = (String) userData.get("username");
                        tvUsername.setText(usernameStr);
                    } else {
                        tvUsername.setText("Unknown");
                    }
                }

                /**
                 * Called when the Firestore operation fails.
                 *
                 * @param e The exception that occurred.
                 */
                @Override
                public void onFailure(Exception e) {
                    tvUsername.setText("Unavailable");
                }
            });
        }

        if (tvDate != null) tvDate.setText(item.getDate());
        if (tvTime != null) tvTime.setText(item.getTime());

        if (tvEmotion != null) {
            tvEmotion.setText("is feeling: " + item.getEmotion());
        }

        if (tvSocialSituation != null) {
            tvSocialSituation.setText("Social Situation: " + item.getSocialSituation());
        }

        if (tvDescription != null) {
            tvDescription.setText(item.getDescription());
        }

        if (tvLocation != null) {
            tvLocation.setText("Loading...");
            String cacheKey = item.getPosition().latitude + "," + item.getPosition().longitude;

            if (geocodeCache.containsKey(cacheKey)) {
                tvLocation.setText(geocodeCache.get(cacheKey));
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
                            new Handler(Looper.getMainLooper()).post(() -> tvLocation.setText(result));
                        } else {
                            new Handler(Looper.getMainLooper()).post(() -> tvLocation.setText("Unknown"));
                        }
                    } catch (Exception e) {
                        new Handler(Looper.getMainLooper()).post(() -> tvLocation.setText("Unavailable"));
                    }
                });
            }
        }

        if (ivImage != null) {
            if (item.getImageBase64() != null) {
                try {
                    byte[] decodedBytes = Base64.decode(item.getImageBase64(), Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    ivImage.setImageBitmap(bitmap);
                } catch (Exception e) {
                    ivImage.setImageResource(R.drawable.ic_default_image);
                }
            } else {
                ivImage.setImageResource(R.drawable.ic_default_image);
            }
        }

        ImageView ivProfilePic = infoWindow.findViewById(R.id.ivProfile);

        if (ivProfilePic != null) {
            firestoreHelper.getUser(item.getUserId(), new FirestoreHelper.FirestoreCallback() {
                /**
                 * Called when the Firestore operation succeeds.
                 *
                 * @param result The result of the operation.
                 */
                @Override
                public void onSuccess(Object result) {
                    if (result instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> userData = (Map<String, Object>) result;
                        String profilePicBase64 = (String) userData.get("profilePicture");

                        if (profilePicBase64 != null && !profilePicBase64.isEmpty()) {
                            byte[] decodedBytes = Base64.decode(profilePicBase64, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                            ivProfilePic.post(() -> ivProfilePic.setImageBitmap(bitmap));
                        } else {
                            ivProfilePic.post(() -> ivProfilePic.setImageResource(R.drawable.default_user_icon));
                        }
                    } else {
                        ivProfilePic.post(() -> ivProfilePic.setImageResource(R.drawable.default_user_icon));
                    }
                }

                /**
                 * Called when the Firestore operation fails.
                 *
                 * @param e The exception that occurred.
                 */
                @Override
                public void onFailure(Exception e) {
                    ivProfilePic.post(() -> ivProfilePic.setImageResource(R.drawable.default_user_icon));
                }
            });
        }

        infoWindow.setVisibility(View.VISIBLE);
        infoWindow.post(() -> {
            View parent = (View) infoWindow.getParent();
            int parentHeight = parent.getHeight();
            int infoHeight = infoWindow.getMeasuredHeight();
            int finalY = parentHeight - infoHeight;

            infoWindow.setTranslationY(parentHeight);
            infoWindow.animate().translationY(finalY).setDuration(300).start();
        });

        isInfoWindowVisible = true;
    }

    /**
     * Slides down the info window with an animation.
     */
    private void slideDownInfoWindow() {
        View parent = (View) infoWindow.getParent();
        final int parentHeight = parent.getHeight();
        final float startY = infoWindow.getTranslationY();

        ValueAnimator animator = ValueAnimator.ofFloat(startY, parentHeight);
        animator.setDuration(300);
        animator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            infoWindow.setTranslationY(value);
        });

        animator.addListener(new AnimatorListenerAdapter() {
            /**
             * Called when the animation ends.
             *
             * @param animation The animation which reached its end.
             */
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                infoWindow.setVisibility(View.GONE);
                isInfoWindowVisible = false;
            }
        });

        animator.start();
    }

    /**
     * Shows a calendar dialog to select a date for filtering.
     */
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

            SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            selectedDateDisplay.setText(displayFormat.format(selectedDate));

            if (isLast7DaysFilter) {
                isLast7DaysFilter = false;
                showLast7DaysSwitch.setChecked(false);
            }
        });

        builder.setView(calendarDialogView);
        builder.setPositiveButton("Apply", (dialog, which) ->
                filterAndDisplayMoodEventsAsync(showNearbySwitch.isChecked(), currentLocation));
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}