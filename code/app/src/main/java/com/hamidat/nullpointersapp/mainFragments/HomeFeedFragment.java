package com.hamidat.nullpointersapp.mainFragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hamidat.nullpointersapp.MainActivity;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.models.MoodAdapter;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;
import com.hamidat.nullpointersapp.models.moodHistory;

import java.util.ArrayList;
import java.util.Map;

public class HomeFeedFragment extends Fragment {
    private RecyclerView rvMoodList;
    private MoodAdapter moodAdapter;
    private ArrayList<Mood> allMoods = new ArrayList<>();
    private FirestoreHelper firestoreHelper;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate your home feed layout
        View view = inflater.inflate(R.layout.full_mood_event, container, false);
        rvMoodList = view.findViewById(R.id.rvMoodList);
        rvMoodList.setLayoutManager(new LinearLayoutManager(getContext()));

        moodAdapter = new MoodAdapter(allMoods);
        rvMoodList.setAdapter(moodAdapter);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Get Firestore helper and current user ID from MainActivity.
        if(getActivity() instanceof MainActivity){
            MainActivity mainActivity = (MainActivity)getActivity();
            firestoreHelper = mainActivity.getFirestoreHelper();
            currentUserId = mainActivity.getCurrentUserId();
        }

        // Query mood events from current user and their followed users.
        fetchMoodData();
    }

    private void fetchMoodData() {
        if (currentUserId == null || firestoreHelper == null) return;

        // First, fetch current user document to get the "following" array.
        firestoreHelper.getUser(currentUserId, new FirestoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                if (result instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> userData = (Map<String, Object>) result;
                    ArrayList<String> followingIds = (ArrayList<String>) userData.get("following");
                    if (followingIds == null) {
                        followingIds = new ArrayList<>();
                    }
                    // Ensure the current user's ID is included.
                    if (!followingIds.contains(currentUserId)) {
                        followingIds.add(currentUserId);
                    }
                    // Now query mood events for all these user IDs.
                    firestoreHelper.firebaseToMoodHistory(followingIds, new FirestoreHelper.FirestoreCallback() {
                        @Override
                        public void onSuccess(Object result) {
                            moodHistory history = (moodHistory) result;
                            // Update our local list and notify adapter.
                            allMoods.clear();
                            allMoods.addAll(history.getMoodArray());
                            requireActivity().runOnUiThread(() -> moodAdapter.notifyDataSetChanged());
                        }
                        @Override
                        public void onFailure(Exception e) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "Error loading moods: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    });
                }
            }
            @Override
            public void onFailure(Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }
}
