package com.hamidat.nullpointersapp;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.hamidat.nullpointersapp.mainFragments.AddMoodFragment;
import com.hamidat.nullpointersapp.mainFragments.MapFragment;
import com.hamidat.nullpointersapp.mainFragments.ProfileFragment;
import com.hamidat.nullpointersapp.mainFragments.SettingsFragment;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreTestFragment;

public class MainActivity extends AppCompatActivity {
    private String currentUserId;
    private FirestoreHelper currentUserFirestoreInstance;

    /**
     * Called when the activity is created.
     *
     * @param savedInstanceState The previously saved state, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Contains the fragment_container

        // Retrieve the passed user ID
        currentUserId = getIntent().getStringExtra("USER_ID");
        if (currentUserId == null || currentUserId.isEmpty()) {
            Toast.makeText(this, "Error: No logged in user ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Create a single FirestoreHelper instance for the logged in user
        currentUserFirestoreInstance = new FirestoreHelper();

        if (savedInstanceState == null) {
            loadFragment(new ProfileFragment());
        }

        // Bind navigation icons
        final ImageView ivHome = findViewById(R.id.ivHome);
        final ImageView ivAddMood = findViewById(R.id.ivAddMood);
        final ImageView ivProfile = findViewById(R.id.ivProfile);
        final ImageView ivMap = findViewById(R.id.ivMap);

        ivHome.setOnClickListener(view -> {
            Toast.makeText(this, "Home Clicked", Toast.LENGTH_SHORT).show();
            loadFragment(new ProfileFragment()); // Placeholder fragment
        });

        ivAddMood.setOnClickListener(view -> {
            Toast.makeText(this, "Add Mood Clicked", Toast.LENGTH_SHORT).show();
            loadFragment(new AddMoodFragment());  // Placeholder fragment
        });

        ivProfile.setOnClickListener(view -> {
            Toast.makeText(this, "Profile Clicked", Toast.LENGTH_SHORT).show();
            loadFragment(new ProfileFragment());
        });

        ivMap.setOnClickListener(view -> {
            Toast.makeText(this, "Map Clicked", Toast.LENGTH_SHORT).show();
            loadFragment(new MapFragment()); // Placeholder fragment
        });
    }

    /**
     * Getter for the current user's FirestoreHelper instance.
     *
     * @return The FirestoreHelper instance.
     */
    public FirestoreHelper getFirestoreHelper() {
        return currentUserFirestoreInstance;
    }

    /**
     * Getter for the current user's document ID (user ID)
     *
     * @return The userID of the current user.
     */
    public String getCurrentUserId() {
        return currentUserId;
    }

    /**
     * Loads a fragment into the fragment container.
     *
     * @param fragment The fragment to load.
     */
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
