/**
 * MainActivity.java
 *
 * The primary activity hosting all major app fragments and navigation.
 * Manages:
 * - Navigation via bottom icons
 * - Friend request notifications
 * - Location and notification permission handling
 * - Mood caching for quick access
 * - Real-time updates to post notifications based on changes to the following list.
 *
 * Outstanding Issues: None
 */
package com.hamidat.nullpointersapp;

import android.Manifest;
import android.app.NotificationManager;
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

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.Timestamp;  // <-- Required import
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreFollowing;
import com.hamidat.nullpointersapp.utils.notificationUtils.FriendRequestNotifier;
import com.hamidat.nullpointersapp.utils.notificationUtils.NotificationHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private String currentUserId;
    private FirestoreHelper currentUserFirestoreInstance;
    private NavController navController;
    private FirestoreHelper firestoreHelper;
    private Set<String> processedMoodIds = new HashSet<>();

    // Listener registrations to update post notifications in real time.
    private ListenerRegistration followingListener;
    private ListenerRegistration moodListener;

    // For in-memory list of moods.
    private final List<Mood> moodCache = new ArrayList<>();

    public List<Mood> getMoodCache() {
        return moodCache;
    }

    // Helper method to add a new mood to moodCache.
    public void addMoodToCache(Mood newMood) {
        // Insert at the start so newest appears first.
        moodCache.add(0, newMood);
    }

    private static final int PERMISSION_REQUEST_CODE = 101;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 102;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent.getBooleanExtra("open_profile", false)) {
            navController.navigate(R.id.searchFragment);
            // Clear extras so they are not processed again.
            intent.removeExtra("open_profile");
            intent.removeExtra("profile_user_id");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Retrieve the passed user ID.
        currentUserId = getIntent().getStringExtra("USER_ID");
        if (currentUserId == null || currentUserId.isEmpty()) {
            Toast.makeText(this, "Error: No logged in user ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize FirestoreHelper instances.
        firestoreHelper = new FirestoreHelper();
        currentUserFirestoreInstance = firestoreHelper; // Use the same instance.

        // Set up friend request listener.
        setupNotificationListener();
        // Set up following listener to update post notifications in real time.
        setupFollowingListener();

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

        // Initialize NavController.
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        // If launched via notification, navigate accordingly.
        if (getIntent() != null) {
            if (getIntent().getBooleanExtra("open_notification", false)) {
                navController.navigate(R.id.notificationFragment);
            }
            if (getIntent().getBooleanExtra("open_profile", false)) {
                // Navigate to SearchFragment to load the profile overlay.
                navController.navigate(R.id.searchFragment);
            }
        }

        // Set up friend request notifier listeners.
        FriendRequestNotifier notifier = FriendRequestNotifier.getInstance();
        notifier.startListeningIncomingRequests(this, currentUserId, currentUserFirestoreInstance);
        notifier.startListeningAcceptedRequests(this, currentUserId);

        // Bind navigation icons.
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (followingListener != null) {
            followingListener.remove();
        }
        if (moodListener != null) {
            moodListener.remove();
        }
    }

    /**
     * Sets up the friend request listener (existing logic).
     */
    private void setupNotificationListener() {
        firestoreHelper.listenForFriendRequests(currentUserId, new FirestoreFollowing.FollowingCallback() {
            @Override
            public void onSuccess(Object result) {
                // When a friend request is received, update the icon to show the badge.
                runOnUiThread(() -> updateNotificationIcon(true));
            }
            @Override
            public void onFailure(Exception e) {
                // Optionally hide the badge if there's an error.
                runOnUiThread(() -> updateNotificationIcon(false));
            }
        });
    }

    /**
     * Listens for changes on the current user's "following" field and reinitializes
     * the post notifications listener with the updated list.
     */
    private void setupFollowingListener() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        followingListener = db.collection("users").document(currentUserId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null || !snapshot.exists()) return;
                    List<String> newFollowedIds = (List<String>) snapshot.get("following");
                    if (newFollowedIds == null) {
                        newFollowedIds = new ArrayList<>();
                    }
                    // Remove self.
                    newFollowedIds.remove(currentUserId);
                    // Remove the existing post notifications listener if any.
                    if (moodListener != null) {
                        moodListener.remove();
                    }
                    // Clear processed mood IDs so that new moods can be processed.
                    processedMoodIds.clear();
                    setupNewPostNotificationListenerWithFollowedList(newFollowedIds);
                });
    }

    /**
     * Sets up the post notification listener with the given list of followed user IDs.
     * Only moods posted after login are processed.
     */
    private void setupNewPostNotificationListenerWithFollowedList(List<String> followedIds) {
        // Limit to 10 IDs because whereIn supports a maximum of 10.
        if (followedIds.size() > 10) {
            followedIds = followedIds.subList(0, 10);
        }
        if (followedIds.isEmpty()) {
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Record login time to filter out old moods.
        final long loginTime = System.currentTimeMillis();
        final Timestamp loginTimestamp = new Timestamp(new Date(loginTime));

        moodListener = db.collection("moods")
                .whereIn("userId", followedIds)
                .whereGreaterThan("timestamp", loginTimestamp)
                .addSnapshotListener((@Nullable QuerySnapshot value, @Nullable com.google.firebase.firestore.FirebaseFirestoreException error) -> {
                    if (error != null || value == null) return;
                    for (DocumentChange dc : value.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            String moodDocId = dc.getDocument().getId();
                            if (processedMoodIds.contains(moodDocId)) continue;
                            processedMoodIds.add(moodDocId);
                            String posterUserId = dc.getDocument().getString("userId");
                            if (posterUserId != null && !posterUserId.equals(currentUserId)) {
                                String posterUsername = dc.getDocument().getString("username");
                                if (posterUsername == null) {
                                    posterUsername = "Someone";
                                }
                                // Use a deterministic notification document ID.
                                String notificationId = "post_" + moodDocId + "_" + currentUserId;
                                java.util.Map<String, Object> notificationData = new java.util.HashMap<>();
                                notificationData.put("type", "post");
                                notificationData.put("fromUserId", posterUserId);
                                notificationData.put("username", posterUsername);
                                notificationData.put("timestamp", Timestamp.now());
                                notificationData.put("toUserId", currentUserId);
                                db.collection("notifications").document(notificationId).set(notificationData);

                                // Send a system-level notification.
                                NotificationHelper.sendPostNotification(getApplicationContext(), currentUserId, posterUsername, posterUserId);
                            }
                        }
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
