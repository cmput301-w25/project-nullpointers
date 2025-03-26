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

import java.util.Map;

public class ProfileFragment extends Fragment {

    private ImageView profileIcon;
    private TextView usernameText;

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
        profileIcon = view.findViewById(R.id.profile_icon);
        usernameText = view.findViewById(R.id.username_text);
        Button viewMoodHistoryButton = view.findViewById(R.id.view_mood_history_button);
        Button settingsButton = view.findViewById(R.id.settings_button);

        // Retrieve FirestoreHelper and currentUserId from MainActivity.
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            FirestoreHelper firestoreHelper = mainActivity.getFirestoreHelper();
            String currentUserId = mainActivity.getCurrentUserId();

            // Fetch user data from Firestore
            firestoreHelper.getUser(currentUserId, new FirestoreHelper.FirestoreCallback() {
                @Override
                public void onSuccess(Object result) {
                    if (result instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> userData = (Map<String, Object>) result;
                        String username = (String) userData.get("username");
                        usernameText.setText(username);

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
