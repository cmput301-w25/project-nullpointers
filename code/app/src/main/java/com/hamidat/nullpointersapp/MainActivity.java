package com.hamidat.nullpointersapp;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.hamidat.nullpointersapp.fragments.HomeFeedFragment;
import com.hamidat.nullpointersapp.fragments.MapFragment;
import com.hamidat.nullpointersapp.fragments.ProfileFragment;
import com.hamidat.nullpointersapp.fragments.SettingsFragment;

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

        // Set Click Listeners
        ivHome.setOnClickListener(view -> loadFragment(new ProfileFragment())); // Placeholder
        ivAddMood.setOnClickListener(view -> loadFragment(new ProfileFragment()));  // Placeholder until edit mood view is done
        ivProfile.setOnClickListener(view -> loadFragment(new ProfileFragment()));  // Placeholder
        ivMap.setOnClickListener(view -> loadFragment(new ProfileFragment())); // Placeholder
    }

    // Function to load fragments into the fragment container
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
