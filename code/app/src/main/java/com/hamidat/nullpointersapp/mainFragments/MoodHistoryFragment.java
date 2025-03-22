package com.hamidat.nullpointersapp.mainFragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
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
    private Button btnMoodHistoryFilter;

    // Filter state variables
    private boolean recentWeekFilter = false;
    private boolean showAllChecked = true;
    private boolean happyChecked = false;
    private boolean sadChecked = false;
    private boolean angryChecked = false;
    private boolean chillChecked = false;
    private boolean fearChecked = false;
    private boolean disgustChecked = false;
    private boolean shameChecked = false;
    private boolean surpriseChecked = false;
    private boolean confusionChecked = false;
    private String reasonKeyword = "";

    // Reference to our sliding filter panel
    private View filterPanel;

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

        moodAdapter = new MoodAdapter(moodList, currentUserId);
        rvMoodHistory.setAdapter(moodAdapter);

        btnMoodHistoryFilter = view.findViewById(R.id.btnFilterMoodHistory);

        firestore = FirebaseFirestore.getInstance();
        loadMoodHistory();

        // Inflate and add the filter panel (initially hidden) to the main container.
        filterPanel = LayoutInflater.from(getContext()).inflate(R.layout.filter_panel_mood_history, null);
        filterPanel.setVisibility(View.GONE);
        // Assuming the root layout of fragment_mood_history.xml has id "main"
        ((ViewGroup) view).addView(filterPanel);

        // Setup filter panel controls and listeners.
        setupFilterPanel();

        // When filter button is clicked, slide up the filter panel.
        btnMoodHistoryFilter.setOnClickListener(v -> showFilterPanel());
    }

    /**
     * Sets up the controls on the filter panel.
     */


    private void setupFilterPanel() {
        // Recent week switch
        Switch switchRecentWeek = filterPanel.findViewById(R.id.switchRecentWeek);
        // Mood checkboxes
        CheckBox cbHappy = filterPanel.findViewById(R.id.cbHappy);
        CheckBox cbSad = filterPanel.findViewById(R.id.cbSad);
        CheckBox cbAngry = filterPanel.findViewById(R.id.cbAngry);
        CheckBox cbChill = filterPanel.findViewById(R.id.cbChill);
        CheckBox cbFear = filterPanel.findViewById(R.id.cbFear);
        CheckBox cbDisgust = filterPanel.findViewById(R.id.cbDisgust);
        CheckBox cbShame = filterPanel.findViewById(R.id.cbShame);
        CheckBox cbSurprise = filterPanel.findViewById(R.id.cbSurprise);
        CheckBox cbConfusion = filterPanel.findViewById(R.id.cbConfusion);
        // Reason filter text
        EditText etReasonFilter = filterPanel.findViewById(R.id.etReasonFilter);
        // Close, Apply, and Reset buttons
        Button btnApplyFilter = filterPanel.findViewById(R.id.btnApplyFilter);
        ImageButton btnCloseFilter = filterPanel.findViewById(R.id.btnCloseFilter); // already ImageButton
        Button btnResetFilter = filterPanel.findViewById(R.id.btnResetFilter);

        // Restore previous selections
        switchRecentWeek.setChecked(recentWeekFilter);
        cbHappy.setChecked(happyChecked);
        cbSad.setChecked(sadChecked);
        cbAngry.setChecked(angryChecked);
        cbChill.setChecked(chillChecked);
        cbFear.setChecked(fearChecked);
        cbDisgust.setChecked(disgustChecked);
        cbShame.setChecked(shameChecked);
        cbSurprise.setChecked(surpriseChecked);
        cbConfusion.setChecked(confusionChecked);
        etReasonFilter.setText(reasonKeyword);

        // Close filter panel button (animate down and hide)
        btnCloseFilter.setOnClickListener(v -> hideFilterPanel());

        // Apply filter button: update filter states and reload mood history.
        btnApplyFilter.setOnClickListener(v -> {
            recentWeekFilter = switchRecentWeek.isChecked();
            happyChecked = cbHappy.isChecked();
            sadChecked = cbSad.isChecked();
            angryChecked = cbAngry.isChecked();
            chillChecked = cbChill.isChecked();
            fearChecked = cbFear.isChecked();
            disgustChecked = cbDisgust.isChecked();
            shameChecked = cbShame.isChecked();
            surpriseChecked = cbSurprise.isChecked();
            confusionChecked = cbConfusion.isChecked();
            // If no mood is checked, assume "show all" is true.
            showAllChecked = !(happyChecked || sadChecked || angryChecked || chillChecked ||
                    fearChecked || disgustChecked || shameChecked || surpriseChecked || confusionChecked);
            reasonKeyword = etReasonFilter.getText().toString().trim();

            Toast.makeText(getContext(), "Filter Applied", Toast.LENGTH_SHORT).show();
            hideFilterPanel();
            loadMoodHistory();
        });

        // Reset filter button: restore defaults, update UI, then reload mood history.
        btnResetFilter.setOnClickListener(v -> {
            // Reset filter state variables
            recentWeekFilter = false;
            showAllChecked = true;
            happyChecked = false;
            sadChecked = false;
            angryChecked = false;
            chillChecked = false;
            fearChecked = false;
            disgustChecked = false;
            shameChecked = false;
            surpriseChecked = false;
            confusionChecked = false;
            reasonKeyword = "";

            // Update UI controls
            switchRecentWeek.setChecked(recentWeekFilter);
            cbHappy.setChecked(happyChecked);
            cbSad.setChecked(sadChecked);
            cbAngry.setChecked(angryChecked);
            cbChill.setChecked(chillChecked);
            cbFear.setChecked(fearChecked);
            cbDisgust.setChecked(disgustChecked);
            cbShame.setChecked(shameChecked);
            cbSurprise.setChecked(surpriseChecked);
            cbConfusion.setChecked(confusionChecked);
            etReasonFilter.setText(reasonKeyword);

            Toast.makeText(getContext(), "Filters Reset", Toast.LENGTH_SHORT).show();
            hideFilterPanel();
            loadMoodHistory();
        });
    }


    /**
     * Loads the mood history from Firestore applying the selected filters.
     */
    private void loadMoodHistory() {
        Query query = firestore.collection("moods")
                .whereEqualTo("userId", currentUserId);

        if (recentWeekFilter) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -7);
            Date sevenDaysAgo = cal.getTime();
            query = query.whereGreaterThanOrEqualTo("timestamp", new com.google.firebase.Timestamp(sevenDaysAgo));
        }
        if (!showAllChecked) {
            List<String> selectedMoods = new ArrayList<>();
            if (happyChecked) selectedMoods.add("Happy");
            if (sadChecked) selectedMoods.add("Sad");
            if (angryChecked) selectedMoods.add("Angry");
            if (chillChecked) selectedMoods.add("Chill");
            if (fearChecked) selectedMoods.add("Fear");
            if (disgustChecked) selectedMoods.add("Disgust");
            if (shameChecked) selectedMoods.add("Shame");
            if (surpriseChecked) selectedMoods.add("Surprise");
            if (confusionChecked) selectedMoods.add("Confusion");

            if (!selectedMoods.isEmpty()) {
                query = query.whereIn("mood", selectedMoods);
            }
        }
        query = query.orderBy("timestamp", Query.Direction.DESCENDING);
        query.get().addOnSuccessListener(querySnapshot -> {
            moodList.clear();
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                Mood mood = doc.toObject(Mood.class);
                if (mood != null) {
                    moodList.add(mood);
                }
            }
            // Apply client-side filtering by reason text if provided.
            if (!reasonKeyword.isEmpty()) {
                List<Mood> filteredList = new ArrayList<>();
                for (Mood m : moodList) {
                    if (m.getMoodDescription() != null &&
                            m.getMoodDescription().toLowerCase().contains(reasonKeyword.toLowerCase())) {
                        filteredList.add(m);
                    }
                }
                moodList.clear();
                moodList.addAll(filteredList);
            }
            tvNoMoodEntries.setVisibility(moodList.isEmpty() ? View.VISIBLE : View.GONE);
            moodAdapter.notifyDataSetChanged();
            tvMostFrequentMood.setText("Most frequent: " + calculateMostFrequentMood(moodList));
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Slides up the filter panel.
     */
    private void showFilterPanel() {
        final View root = getView();
        if (root == null) return;
        // Make the panel visible.
        filterPanel.setVisibility(View.VISIBLE);
        // Post to ensure layout is complete.
        root.post(() -> {
            int rootHeight = root.getHeight();
            int panelHeight = filterPanel.getHeight();
            // Set starting Y to bottom (offscreen).
            filterPanel.setY(rootHeight);
            // Animate to final Y: rootHeight - panelHeight (anchored at bottom).
            filterPanel.animate()
                    .y(rootHeight - panelHeight)
                    .setDuration(300)
                    .start();
        });
    }

    private void hideFilterPanel() {
        final View root = getView();
        if (root == null) return;
        root.post(() -> {
            int rootHeight = root.getHeight();
            // Animate the panel's Y back to rootHeight (offscreen at the bottom).
            filterPanel.animate()
                    .y(rootHeight)
                    .setDuration(300)
                    .withEndAction(() -> filterPanel.setVisibility(View.GONE))
                    .start();
        });
    }


    /**
     * Calculates the most frequent mood in the list.
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
