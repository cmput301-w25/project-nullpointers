/**
 * MainActivity.java
 *
 * The primary activity hosting all major app fragments and navigation.
 * Manages:
 * - Navigation via bottom icons
 * - Friend request notifications
 * - Location and notification permission handling
 * - Mood caching for quick access
 *
 * Outstanding Issues: None
 */

package com.hamidat.nullpointersapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;
import com.hamidat.nullpointersapp.utils.notificationUtils.FriendRequestNotifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreFollowing;
import com.hamidat.nullpointersapp.utils.notificationUtils.NotificationHelper;


public class MainActivity extends AppCompatActivity {
    private String currentUserId;
    private FirestoreHelper currentUserFirestoreInstance;
    private NavController navController;
    private FirestoreHelper firestoreHelper;

    // for in memory list of moods
    private final List<Mood> moodCache = new ArrayList<>();

    public List<Mood> getMoodCache() {
        return moodCache;
    }

    // Add a helper to add a new mood to moodCache
    public void addMoodToCache(Mood newMood) {
        // insert at the start so newest appears first
        moodCache.add(0, newMood);
    }

    private static final int PERMISSION_REQUEST_CODE = 101;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);





        setContentView(R.layout.activity_main);


        // Get the current user ID and FirestoreHelper instance.
        currentUserId = getIntent().getStringExtra("USER_ID");
        firestoreHelper = new FirestoreHelper();

        // Set up the persistent listener for friend requests.
        setupNotificationListener();


        // Request POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
            }
        }

        // Request location permission.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Retrieve the passed user ID
        currentUserId = getIntent().getStringExtra("USER_ID");
        if (currentUserId == null || currentUserId.isEmpty()) {
            Toast.makeText(this, "Error: No logged in user ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Create a single FirestoreHelper instance for the logged in user
        currentUserFirestoreInstance = new FirestoreHelper();

        // Initialize NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        // If launched via notification, navigate directly to FollowingFragment.
        // After initializing navController
        if (getIntent() != null) {
            if (getIntent().getBooleanExtra("open_notification", false)) {
                navController.navigate(R.id.notificationFragment);
            }
        }

        if (getIntent().getBooleanExtra("open_profile", false)) {
            String profileUserId = getIntent().getStringExtra("profile_user_id");
            // Navigate to ProfileFragment and pass along the profileUserId so it can load the appropriate user.
        }


        FriendRequestNotifier notifier = FriendRequestNotifier.getInstance();
        notifier.startListeningIncomingRequests(this, currentUserId, currentUserFirestoreInstance);
        notifier.startListeningAcceptedRequests(this, currentUserId);

        // Bind navigation icons
        final ImageView ivHome = findViewById(R.id.ivHome);
        final ImageView ivAddMood = findViewById(R.id.ivAddMood);
        final ImageView ivProfile = findViewById(R.id.ivProfile);
        final ImageView ivMap = findViewById(R.id.ivMap);
        final ImageView ivSearch = findViewById(R.id.ivSearch);
        final ImageView ivNotification = findViewById(R.id.ivNotification);


        ivHome.setOnClickListener(view -> {
            Toast.makeText(this, "Home Clicked", Toast.LENGTH_SHORT).show();
            navController.navigate(R.id.homeFeedFragment);
        });

        ivAddMood.setOnClickListener(view -> {
            Toast.makeText(this, "Add Mood Clicked", Toast.LENGTH_SHORT).show();
            navController.navigate(R.id.addNewMoodNavGraphFragment);
        });

        ivProfile.setOnClickListener(view -> {
            Toast.makeText(this, "Profile Clicked", Toast.LENGTH_SHORT).show();
            navController.navigate(R.id.profileNavGraphFragment);
        });

        ivMap.setOnClickListener(view -> {
            Toast.makeText(this, "Map Clicked", Toast.LENGTH_SHORT).show();
            navController.navigate(R.id.mapFragment);
        });

        ivSearch.setOnClickListener(v -> {
            Toast.makeText(this, "Search Clicked", Toast.LENGTH_SHORT).show();
            navController.navigate(R.id.searchFragment);
        });

        ivNotification.setOnClickListener(v -> {
            Toast.makeText(this, "Notifications Clicked", Toast.LENGTH_SHORT).show();
            navController.navigate(R.id.notificationFragment);
        });


    }

    private void setupNotificationListener() {
        firestoreHelper.listenForFriendRequests(currentUserId, new FirestoreFollowing.FollowingCallback() {
            @Override
            public void onSuccess(Object result) {
                // When a friend request is received, update the icon to show the badge.
                runOnUiThread(() -> updateNotificationIcon(true));
            }
            @Override
            public void onFailure(Exception e) {
                // Optionally, if there's an error (or if no friend request exists), hide the badge.
                runOnUiThread(() -> updateNotificationIcon(false));
            }
        });
    }

    private void setupNewPostNotificationListener() {
        // First, fetch the current user's data to get the list of followed user IDs.
        currentUserFirestoreInstance.getUser(currentUserId, new FirestoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                if (result instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> userData = (Map<String, Object>) result;
                    List<String> followedIds = (List<String>) userData.get("following");
                    if (followedIds == null) {
                        followedIds = new ArrayList<>();
                    }
                    // Remove self from followedIds if present.
                    followedIds.remove(currentUserId);
                    if (!followedIds.isEmpty()) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        // Listen for new mood posts from users in followedIds.
                        db.collection("moods")
                                .whereIn("userId", followedIds)
                                .addSnapshotListener((@Nullable QuerySnapshot value, @Nullable com.google.firebase.firestore.FirebaseFirestoreException error) -> {
                                    if (error != null || value == null) return;
                                    for (DocumentChange dc : value.getDocumentChanges()) {
                                        if (dc.getType() == DocumentChange.Type.ADDED) {
                                            String posterUserId = dc.getDocument().getString("userId");
                                            String posterUsername = dc.getDocument().getString("username");
                                            if (posterUsername == null) {
                                                posterUsername = "Someone";
                                            }
                                            // Send notification on follower's device.
                                            NotificationHelper.sendPostNotification(getApplicationContext(), posterUsername, posterUserId);
                                        }
                                    }
                                });
                    }
                }
            }
            @Override
            public void onFailure(Exception e) {
                // Log error if needed.
            }
        });
    }



    public void updateNotificationIcon(boolean hasNotifications) {
        ImageView ivNotification = findViewById(R.id.ivNotification);
        if (ivNotification != null) {
            if (hasNotifications) {
                ivNotification.setImageResource(R.drawable.ic_notification_badge);
            } else {
                ivNotification.setImageResource(R.drawable.ic_notification);
            }
        }
    }





    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission is required for friend request alerts", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission is required for location-based features", Toast.LENGTH_LONG).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public FirestoreHelper getFirestoreHelper() {
        return currentUserFirestoreInstance;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }



}
