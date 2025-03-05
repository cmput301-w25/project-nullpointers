package com.hamidat.nullpointersapp;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;
import com.hamidat.nullpointersapp.utils.notificationUtils.FriendRequestNotifier;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private String currentUserId;
    private FirestoreHelper currentUserFirestoreInstance;
    private NavController navController;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        if (getIntent() != null && getIntent().getBooleanExtra("open_following", false)) {
            navController.navigate(R.id.followingFragment);
        }

        FriendRequestNotifier.getInstance().startListening(this, currentUserId, currentUserFirestoreInstance);


        // Bind navigation icons
        final ImageView ivHome = findViewById(R.id.ivHome);
        final ImageView ivAddMood = findViewById(R.id.ivAddMood);
        final ImageView ivProfile = findViewById(R.id.ivProfile);
        final ImageView ivMap = findViewById(R.id.ivMap);

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
    }

    public FirestoreHelper getFirestoreHelper() {
        return currentUserFirestoreInstance;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }
}
