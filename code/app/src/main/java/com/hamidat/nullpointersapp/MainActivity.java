package com.hamidat.nullpointersapp;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.hamidat.nullpointersapp.mainFragments.MapFragment;
import com.hamidat.nullpointersapp.mainFragments.ProfileFragment;
import com.hamidat.nullpointersapp.mainFragments.SettingsFragment;

/**
 * The main activity that manages the primary navigation.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Called when the activity is created.
     *
     * @param savedInstanceState The previously saved state, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Contains the fragment_container

        // Load ProfileFragment by default if not already restored
        if (savedInstanceState == null) {
            loadFragment(new ProfileFragment());
        }

        // Bind navigation icons
        final ImageView ivHome = findViewById(R.id.ivHome);
        final ImageView ivAddMood = findViewById(R.id.ivAddMood);
        final ImageView ivProfile = findViewById(R.id.ivProfile);
        final ImageView ivMap = findViewById(R.id.ivMap);

        // Set click listeners with Toast feedback and load appropriate fragments
        ivHome.setOnClickListener(view -> {
            Toast.makeText(this, "Home Clicked", Toast.LENGTH_SHORT).show();
            loadFragment(new SettingsFragment()); // Placeholder fragment
        });

        ivAddMood.setOnClickListener(view -> {
            Toast.makeText(this, "Add Mood Clicked", Toast.LENGTH_SHORT).show();
            loadFragment(new ProfileFragment());  // Placeholder fragment
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
