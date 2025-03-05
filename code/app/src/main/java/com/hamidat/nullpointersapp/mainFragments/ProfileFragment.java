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
import com.hamidat.nullpointersapp.MainActivity;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.models.UserProfile;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;
import java.util.Map;

/**
 * Displays the user's profile information.
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

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind UI elements
        ImageView profileIcon = view.findViewById(R.id.profile_icon);
        final TextView usernameText = view.findViewById(R.id.username_text);
        Button viewMoodHistoryButton = view.findViewById(R.id.view_mood_history_button);
        Button settingsButton = view.findViewById(R.id.settings_button);

        // Retrieve the shared FirestoreHelper and currentUserId from MainActivity
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            FirestoreHelper firestoreHelper = mainActivity.getFirestoreHelper();
            String currentUserId = mainActivity.getCurrentUserId();

            // Fetch the user data from Firestore using the current user ID
            firestoreHelper.getUser(currentUserId, new FirestoreHelper.FirestoreCallback() {
                @Override
                public void onSuccess(Object result) {
                    if (result instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> userData = (Map<String, Object>) result;
                        String username = (String) userData.get("username");
                        // Update the UI with the fetched username
                        usernameText.setText(String.format("My Username: %s", username));
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getActivity(), "Failed to fetch user data: "
                            + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getActivity(), "Error retrieving Firestore instance",
                    Toast.LENGTH_SHORT).show();
        }

        // if we want to test without auth, initialize a default UserProfile if needed)
        // UserProfile userProfile = new UserProfile(DEFAULT_USERNAME);
        // usernameText.setText(String.format("My Username: %s", userProfile.getUsername()));

        // Set button listeners with Toast feedback
        viewMoodHistoryButton.setOnClickListener(v ->
                Toast.makeText(getActivity(), "View Mood History clicked",
                        Toast.LENGTH_SHORT).show());

        settingsButton.setOnClickListener(v ->
                Toast.makeText(getActivity(), "Settings clicked",
                        Toast.LENGTH_SHORT).show());
    }
}
