/**
 * ProfileFragment.java
 *
 * Displays the current user's profile, including:
 * - Username
 * - Profile picture
 * - Friend count (users they follow)
 *
 * This fragment also provides navigation to:
 * - MoodHistoryFragment
 * - SettingsFragment
 * - FollowingFragment (My Friends)
 *
 * <p>User data is fetched from Firestore using FirestoreHelper.</p>
 * <p>Profile picture is decoded from Base64 if available.</p>
 * <p><b>Outstanding issues:</b> None.</p>
 */

package com.hamidat.nullpointersapp.mainFragments;

import static com.hamidat.nullpointersapp.utils.AppConstants.*;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
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
import androidx.navigation.Navigation;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.hamidat.nullpointersapp.MainActivity;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.models.MoodAdapter;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;

import java.util.List;
import java.util.Map;

/**
 * Fragment that displays the user's profile information and provides navigation to other user-related fragments.
 */
public class ProfileFragment extends Fragment {

    private ImageView profileIcon;
    private TextView usernameText;

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     * UI should be attached to. The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from
     * a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    /**
     * Called immediately after onCreateView(LayoutInflater, ViewGroup, Bundle) has returned,
     * but before any saved state has been restored in to the view.
     *
     * @param view               The View returned by onCreateView(LayoutInflater, ViewGroup, Bundle).
     * @param savedInstanceState If non-null, this fragment is being re-constructed from
     * a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind UI elements
        profileIcon = view.findViewById(R.id.profile_icon);
        usernameText = view.findViewById(R.id.username_text);
        Button viewMoodHistoryButton = view.findViewById(R.id.view_mood_history_button);
        Button settingsButton = view.findViewById(R.id.settings_button);
        Button tvFriends = view.findViewById(R.id.btnFollowing);
        TextView statusBubble = view.findViewById(R.id.user_status_bubble);

        tvFriends.setOnClickListener(v -> {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_profileNavGraphFragment_to_followingFragment);
        });

        // Retrieve FirestoreHelper and currentUserId from MainActivity.
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            FirestoreHelper firestoreHelper = mainActivity.getFirestoreHelper();
            String currentUserId = mainActivity.getCurrentUserId();

            // Fetch user data from Firestore
            firestoreHelper.getUser(currentUserId, new FirestoreHelper.FirestoreCallback() {
                /**
                 * Called when the operation succeeds.
                 *
                 * @param result The result of the operation.
                 */
                @Override
                public void onSuccess(Object result) {
                    if (result instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> userData = (Map<String, Object>) result;
                        String username = (String) userData.get("username");
                        usernameText.setText(username);
                        List<String> following = (List<String>) userData.get("following");
                        int count = (following != null && !following.isEmpty()) ? following.size() - 1 : 0;
                        if (following != null && following.size() == 1){
                            count = 1;
                            tvFriends.setText("My Friends: " + count);

                        }else if (following != null){
                            tvFriends.setText("My Friends: " + count);
                        } else{
                            tvFriends.setText("My Friends: " + 0);
                        }

                        String status = (String) userData.get("status");
                        if (status != null && !status.isEmpty()) {
                            statusBubble.setText(status);
                            statusBubble.setVisibility(View.VISIBLE);
                        } else {
                            statusBubble.setVisibility(View.GONE);
                        }

                        // Update the profile image if available
                        String base64ProfilePic = (String) userData.get("profilePicture");
                        if (base64ProfilePic != null && !base64ProfilePic.isEmpty()) {
                            byte[] decodedBytes = Base64.decode(base64ProfilePic, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                            profileIcon.setImageBitmap(bitmap);
                        } else {
                            // Optionally, set a default profile image.
                            profileIcon.setImageResource(R.drawable.default_user_icon);
                        }
                    }
                }

                /**
                 * Called when the operation fails.
                 *
                 * @param e The exception that occurred.
                 */
                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getActivity(), "Failed to fetch user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getActivity(), "Error retrieving Firestore instance", Toast.LENGTH_SHORT).show();
        }

        // Navigate to MoodHistoryFragment when "View My Mood History" is clicked.
        viewMoodHistoryButton.setOnClickListener(v -> {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_profileNavGraphFragment_to_moodHistoryFragment);
        });

        // Navigate to the SettingsFragment when the settings button is clicked.
        settingsButton.setOnClickListener(v -> {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_profileNavGraphFragment_to_settingsFragment);
        });
    }
}