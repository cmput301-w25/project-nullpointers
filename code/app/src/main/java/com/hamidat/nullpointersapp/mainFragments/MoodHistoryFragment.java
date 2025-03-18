package com.hamidat.nullpointersapp.mainFragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
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

/**
 * Fragment that displays the user's mood history and allows filtering.
 */
public class MoodHistoryFragment extends Fragment {

    private RecyclerView rvMoodHistory;
    private TextView tvMoodHistoryTitle, tvMoodSubtitle, tvMostFrequentMood, tvNoMoodEntries;
    private MoodAdapter moodAdapter;
    private List<Mood> moodList = new ArrayList<>();
    private FirebaseFirestore firestore;
    private String currentUserId;
    private Button btnMoodHistoryFilter;

    // these are just for remembering the users current filter
    private boolean showAllChecked = true;
    private boolean happyChecked = false;
    private boolean sadChecked = false;
    private boolean angryChecked = false;
    private boolean chillChecked = false;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
         return inflater.inflate(R.layout.fragment_mood_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tvMoodHistoryTitle = view.findViewById(R.id.tvMoodHistoryTitle);
        tvMoodSubtitle = view.findViewById(R.id.tvMoodSubtitle);
        tvMostFrequentMood = view.findViewById(R.id.tvMostFrequentMood);
        tvNoMoodEntries = view.findViewById(R.id.tvNoMoodEntries);
        rvMoodHistory = view.findViewById(R.id.rvMoodHistory);
        rvMoodHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        if (getActivity() instanceof MainActivity) {
            currentUserId = ((MainActivity) getActivity()).getCurrentUserId();
        } else {
            Toast.makeText(getContext(), "Error: current user not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fix: Pass currentUserId to MoodAdapter constructor
        moodAdapter = new MoodAdapter(moodList, currentUserId);
        rvMoodHistory.setAdapter(moodAdapter);

        btnMoodHistoryFilter = view.findViewById(R.id.btnFilterMoodHistory);

        firestore = FirebaseFirestore.getInstance();
        loadMoodHistory();

        btnMoodHistoryFilter.setOnClickListener(v -> showFilterDialog());
    }


    /**
     * Loads the user's mood history from Firestore and applies filters.
     */
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

        // I have to separate the qeury a bit to filter moods by the selection
        // start making the query
        Query query = firestore.collection("moods")
                .whereEqualTo("userId", currentUserId)
                .whereGreaterThanOrEqualTo("timestamp", startOfWeek)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        // if "Show All" is NOT checked, filter moods based on selected categories
        if (!showAllChecked) {
            List<String> selectedMoods = new ArrayList<>();
            if (happyChecked) selectedMoods.add("Happy");
            if (sadChecked) selectedMoods.add("Sad");
            if (angryChecked) selectedMoods.add("Angry");
            if (chillChecked) selectedMoods.add("Chill");

            if (!selectedMoods.isEmpty()) {
                query = query.whereIn("mood", selectedMoods);
            }
        }

        // Query Firestore for mood events for the current user starting from the beginning of the week.
        query.get().addOnSuccessListener(querySnapshot -> {
            moodList.clear();
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                Mood mood = doc.toObject(Mood.class);
                if (mood != null) {
                    moodList.add(mood);
                }
            }
            if (moodList.isEmpty()) {
                tvNoMoodEntries.setVisibility(View.VISIBLE);
            } else {
                tvNoMoodEntries.setVisibility(View.GONE);
            }
            moodAdapter.notifyDataSetChanged();
            tvMostFrequentMood.setText("Most frequent: " + calculateMostFrequentMood(moodList));
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Displays a dialog for filtering mood history.
     */
    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_filter_mood_history, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // get references to dialog elements
        CheckBox cbShowAll = dialogView.findViewById(R.id.cb_moodhistory_show_all);
        CheckBox cbMoodHistoryHappy = dialogView.findViewById(R.id.cb_moodhistory_happy);
        CheckBox cbMoodHistorySad = dialogView.findViewById(R.id.cb_moodhistory_sad);
        CheckBox cbMoodHistoryAngry = dialogView.findViewById(R.id.cb_moodhistory_angry);
        CheckBox cbMoodHistoryChill = dialogView.findViewById(R.id.cb_moodhistory_chill);
        Button btnApply = dialogView.findViewById(R.id.btnApplyMoodHistoryFilter);

        // using an arr of all mood checkboxes for easy enabling/disabling
        CheckBox[] moodCheckboxes = {cbMoodHistoryHappy, cbMoodHistorySad, cbMoodHistoryAngry, cbMoodHistoryChill};

        // Restore the last selection
        cbShowAll.setChecked(showAllChecked);
        cbMoodHistoryHappy.setChecked(happyChecked);
        cbMoodHistorySad.setChecked(sadChecked);
        cbMoodHistoryAngry.setChecked(angryChecked);
        cbMoodHistoryChill.setChecked(chillChecked);

        // disable individual mood checkboxes if "Show All" is checked
        for (CheckBox cb : moodCheckboxes) {
            cb.setEnabled(!showAllChecked);
        }

        // handling the "Show All" logic -> lmk if you guys do want to remove this though
        cbShowAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (CheckBox cb : moodCheckboxes) {
                cb.setChecked(false); // Uncheck all moods if "Show All" is checked
                cb.setEnabled(!isChecked); // Disable moods when "Show All" is checked
            }
        });

        btnApply.setOnClickListener(v -> {
            // store the current filter settings
            showAllChecked = cbShowAll.isChecked();
            happyChecked = cbMoodHistoryHappy.isChecked();
            sadChecked = cbMoodHistorySad.isChecked();
            angryChecked = cbMoodHistoryAngry.isChecked();
            chillChecked = cbMoodHistoryChill.isChecked();

            String message;
            if (cbShowAll.isChecked()) {
                message = "Showing all moods";
            } else {
                message = "Filter Applied: ";
                if (cbMoodHistoryHappy.isChecked()) message += "Happy ";
                if (cbMoodHistorySad.isChecked()) message += "Sad ";
                if (cbMoodHistoryAngry.isChecked()) message += "Angry ";
                if (cbMoodHistoryChill.isChecked()) message += "Chill ";
            }

            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            dialog.dismiss();

            loadMoodHistory();
        });

        dialog.show();
    }

    /**
     * Calculates the most frequent mood from the list of mood events.
     * Only returns the mood if available; otherwise returns "N/A".
     *
     * @param moods List of mood entries.
     * @return The most common mood or "N/A" if empty.
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
