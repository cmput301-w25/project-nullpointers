package com.hamidat.nullpointersapp;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.hamidat.nullpointersapp.mainFragments.ProfileFragment;
import com.hamidat.nullpointersapp.mainFragments.SettingsFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Ensure this contains `fragment_container`

        // Load ProfileFragment by default
        if (savedInstanceState == null) {
            loadFragment(new ProfileFragment());
        }

        // Bind Navigation Icons
        ImageView ivHome = findViewById(R.id.ivHome);
        ImageView ivAddMood = findViewById(R.id.ivAddMood);
        ImageView ivProfile = findViewById(R.id.ivProfile);
        ImageView ivMap = findViewById(R.id.ivMap);

        // Set Click Listeners with Toast Feedback
        ivHome.setOnClickListener(view -> {
            Toast.makeText(this, "Home Clicked", Toast.LENGTH_SHORT).show();
            loadFragment(new SettingsFragment()); // Placeholder
        });

        ivAddMood.setOnClickListener(view -> {
            Toast.makeText(this, "Add Mood Clicked", Toast.LENGTH_SHORT).show();
            loadFragment(new ProfileFragment());  // Placeholder
        });

        ivProfile.setOnClickListener(view -> {
            Toast.makeText(this, "Profile Clicked", Toast.LENGTH_SHORT).show();
            loadFragment(new ProfileFragment());
        });

        ivMap.setOnClickListener(view -> {
            Toast.makeText(this, "Map Clicked", Toast.LENGTH_SHORT).show();
            loadFragment(new SettingsFragment()); // Placeholder
        });
    }

    // Function to load fragments into the fragment container
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
