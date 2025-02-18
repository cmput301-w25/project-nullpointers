package com.hamidat.nullpointersapp;

import static com.hamidat.nullpointersapp.AppConstants.*;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hamidat.nullpointersapp.model.UserProfile;

//
// 3. Class Definition
//

/**
 * Displays the user's profile information, including username.
 * Demonstrates how to link the UserProfile model to the UI.
 */
public class ProfileActivity extends AppCompatActivity {
    private ImageView profileIcon;
    private TextView usernameText;
    private Button viewMoodHistoryButton;
    private Button settingsButton;
    private UserProfile userProfile;

    /**
     * Called when the activity is created.
     * Sets up the layout, binds UI elements, and initializes the user profile.
     *
     * @param savedInstanceState The previously saved state, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Bind UI elements
        profileIcon = findViewById(R.id.profile_icon);
        usernameText = findViewById(R.id.username_text);
        viewMoodHistoryButton = findViewById(R.id.view_mood_history_button);
        settingsButton = findViewById(R.id.settings_button);

        // Initialize user profile
        userProfile = new UserProfile(DEFAULT_USERNAME);
        // Display username from the model
        usernameText.setText(String.format("My Username: %s", userProfile.getUsername()));

        // Set button listeners
        viewMoodHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ProfileActivity.this,
                        "View Mood History clicked",
                        Toast.LENGTH_SHORT).show();
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ProfileActivity.this,
                        "Settings clicked",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
