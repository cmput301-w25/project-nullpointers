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
import androidx.fragment.app.Fragment;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.models.UserProfile;

/**
 * Displays the user's profile information.
 * Converted from an Activity to a Fragment.
 */
public class ProfileFragment extends Fragment {
    /**
     * Inflates the fragment layout.
     *
     * @param inflater           LayoutInflater to inflate the view.
     * @param container          The parent view.
     * @param savedInstanceState Saved state data.
     * @return The inflated view.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    /**
     * Binds UI elements and initializes user profile data.
     *
     * @param view               The view returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState Saved state data.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind UI elements
        ImageView profileIcon = view.findViewById(R.id.profile_icon);
        TextView usernameText = view.findViewById(R.id.username_text);
        Button viewMoodHistoryButton = view.findViewById(R.id.view_mood_history_button);
        Button settingsButton = view.findViewById(R.id.settings_button);

        // Initialize user profile and display username
        UserProfile userProfile = new UserProfile(DEFAULT_USERNAME);
        usernameText.setText(String.format("My Username: %s", userProfile.getUsername()));

        // Set button listeners with Toast feedback
        viewMoodHistoryButton.setOnClickListener(v ->
                Toast.makeText(getActivity(), "View Mood History clicked", Toast.LENGTH_SHORT).show());

        settingsButton.setOnClickListener(v ->
                Toast.makeText(getActivity(), "Settings clicked", Toast.LENGTH_SHORT).show());
    }
}
