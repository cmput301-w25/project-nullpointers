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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.hamidat.nullpointersapp.MainActivity;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.models.MoodAdapter;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProfileFragment extends Fragment {

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
        Button lougoutButton = view.findViewById(R.id.btn_logout_user);
        Button btnFollowing = view.findViewById(R.id.btnFollowing);

        btnFollowing.setOnClickListener(v -> {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_profileNavGraphFragment_to_followingFragment);
        });

        // Retrieve FirestoreHelper and currentUserId from MainActivity.
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            FirestoreHelper firestoreHelper = mainActivity.getFirestoreHelper();
            String currentUserId = mainActivity.getCurrentUserId();

            // Fetch the user data from Firestore using the current user ID.
            firestoreHelper.getUser(currentUserId, new FirestoreHelper.FirestoreCallback() {
                @Override
                public void onSuccess(Object result) {
                    if (result instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> userData = (Map<String, Object>) result;
                        String username = (String) userData.get("username");
                        usernameText.setText(String.format("My Username: %s", username));
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

        // When "View My Mood History" is clicked, query and display all mood events in reverse chronological order.
        viewMoodHistoryButton.setOnClickListener(v -> {
            // Navigate to the MoodHistoryFragment.
            // Make sure your nav_graph.xml has a proper action with id action_profileFragment_to_moodHistoryFragment.
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_profileNavGraphFragment_to_moodHistoryFragment);
        });


        lougoutButton.setOnClickListener(v ->
                Toast.makeText(getActivity(), "Logout Button clicked", Toast.LENGTH_SHORT).show());
    }
}
