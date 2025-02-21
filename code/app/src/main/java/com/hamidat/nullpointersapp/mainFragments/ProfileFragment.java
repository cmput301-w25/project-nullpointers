package com.hamidat.nullpointersapp.mainFragments;

import static com.hamidat.nullpointersapp.utils.AppConstants.*;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.utils.profileModel.UserProfile;

/**
 * Displays the user's profile information, including username.
 * Converted from Activity to Fragment.
 */
public class ProfileFragment extends Fragment {
    private ImageView profileIcon;
    private TextView usernameText;
    private Button viewMoodHistoryButton;
    private Button settingsButton;
    private UserProfile userProfile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind UI elements
        profileIcon = view.findViewById(R.id.profile_icon);
        usernameText = view.findViewById(R.id.username_text);
        viewMoodHistoryButton = view.findViewById(R.id.view_mood_history_button);
        settingsButton = view.findViewById(R.id.settings_button);

        // Initialize user profile
        userProfile = new UserProfile(DEFAULT_USERNAME);
        usernameText.setText(String.format("My Username: %s", userProfile.getUsername()));

        // Set button listeners
        viewMoodHistoryButton.setOnClickListener(v ->
                Toast.makeText(getActivity(),
                        "View Mood History clicked",
                        Toast.LENGTH_SHORT).show());

        settingsButton.setOnClickListener(v ->
                Toast.makeText(getActivity(),
                        "Settings clicked",
                        Toast.LENGTH_SHORT).show());
    }

    /**
     * Displays the user's profile information, including username.
     * Demonstrates how to link the UserProfile model to the UI.
     */
    public static class ProfileActivity extends AppCompatActivity {
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
            setContentView(R.layout.fragment_profile);

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
}
