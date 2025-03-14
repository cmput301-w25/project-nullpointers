package com.hamidat.nullpointersapp.mainFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.hamidat.nullpointersapp.MainActivity;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.models.MoodAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoodHistoryFragment extends Fragment {

    private RecyclerView rvMoodHistory;
    private TextView tvMoodHistoryTitle, tvMoodSubtitle, tvMostFrequentMood, tvNoMoodEntries;
    private MoodAdapter moodAdapter;
    private List<Mood> moodList = new ArrayList<>();
    private FirebaseFirestore firestore;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate your provided XML layout (assumed to be fragment_mood_history.xml)
        return inflater.inflate(R.layout.fragment_mood_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Bind header elements from the layout.
        tvMoodHistoryTitle = view.findViewById(R.id.tvMoodHistoryTitle);
        tvMoodSubtitle = view.findViewById(R.id.tvMoodSubtitle);
        tvMostFrequentMood = view.findViewById(R.id.tvMostFrequentMood);
        tvNoMoodEntries = view.findViewById(R.id.tvNoMoodEntries);
        rvMoodHistory = view.findViewById(R.id.rvMoodHistory);
        rvMoodHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        moodAdapter = new MoodAdapter(moodList);
        rvMoodHistory.setAdapter(moodAdapter);

        firestore = FirebaseFirestore.getInstance();
        // Get currentUserId from MainActivity.
        if(getActivity() instanceof MainActivity) {
            currentUserId = ((MainActivity)getActivity()).getCurrentUserId();
        } else {
            Toast.makeText(getContext(), "Error: current user not found", Toast.LENGTH_SHORT).show();
            return;
        }

        loadMoodHistory();
    }

    private void loadMoodHistory() {
        // Compute the start of the current week.
        Calendar calendar = Calendar.getInstance();
        // Set to the first day of week (adjust if your week starts on a different day)
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startOfWeek = calendar.getTime();

        // Query Firestore for mood events for the current user starting from the beginning of the week.
        firestore.collection("moods")
                .whereEqualTo("userId", currentUserId)
                .whereGreaterThanOrEqualTo("timestamp", startOfWeek)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    moodList.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Mood mood = doc.toObject(Mood.class);
                        if (mood != null) {
                            moodList.add(mood);
                        }
                    }
                    if(moodList.isEmpty()){
                        tvNoMoodEntries.setVisibility(View.VISIBLE);
                    } else {
                        tvNoMoodEntries.setVisibility(View.GONE);
                    }
                    moodAdapter.notifyDataSetChanged();
                    // calc (short for calculate if youre new to the stream) the most frequent mood for the week.
                    String mostFrequent = calculateMostFrequentMood(moodList);
                    tvMostFrequentMood.setText("Most frequent: " + mostFrequent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading mood history: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Calculates the most frequent mood from the list of mood events.
     * Only returns the mood if available; otherwise returns "N/A".
     */
    private String calculateMostFrequentMood(List<Mood> moods) {
        if (moods.isEmpty()) return "N/A";
        Map<String, Integer> frequencyMap = new HashMap<>();
        for (Mood mood : moods) {
            String m = mood.getMood();
            if (m != null) {
                frequencyMap.put(m, frequencyMap.getOrDefault(m, 0) + 1);
            }
        }
        String mostFrequent = "N/A";
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : frequencyMap.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostFrequent = entry.getKey();
            }
        }
        return mostFrequent;
    }
}
