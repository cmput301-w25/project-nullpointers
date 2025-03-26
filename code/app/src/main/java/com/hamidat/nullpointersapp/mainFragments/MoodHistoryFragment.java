package com.hamidat.nullpointersapp.mainFragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.hamidat.nullpointersapp.MainActivity;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.models.MoodAdapter;
import com.hamidat.nullpointersapp.utils.homeFeedUtils.CommentsBottomSheetFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class MoodHistoryFragment extends Fragment {

    private RecyclerView rvMoodHistory;
    private TextView tvMoodHistoryTitle, tvMoodSubtitle, tvMostFrequentMood, tvNoMoodEntries;
    private MoodAdapter moodAdapter;
    private List<Mood> moodList = new ArrayList<>();
    private FirebaseFirestore firestore;
    private String currentUserId;
    private Button btnMoodHistoryFilter;

    // Filter state variables (default: show all events)
    private boolean recentWeekFilter = false;
    private boolean happyChecked = false;
    private boolean sadChecked = false;
    private boolean angryChecked = false;
    private boolean afraidChecked = false;
    private boolean disgustedChecked = false;
    private boolean shameChecked = false;
    private boolean surpriseChecked = false;
    private boolean confusionChecked = false;
    private String reasonKeyword = "";
    private Date selectedDateFilter = null;

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
        btnMoodHistoryFilter = view.findViewById(R.id.btnFilterMoodHistory);

        if (getActivity() instanceof MainActivity) {
            currentUserId = ((MainActivity) getActivity()).getCurrentUserId();
        } else {
            Toast.makeText(getContext(), "Error: current user not found", Toast.LENGTH_SHORT).show();
            return;
        }

        moodAdapter = new MoodAdapter(moodList, currentUserId);
        rvMoodHistory.setAdapter(moodAdapter);

        rvMoodHistory.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {
                Button btnComment = view.findViewById(R.id.btnComment);
                if(btnComment != null) {
                    btnComment.setOnClickListener(v -> {
                        int pos = rvMoodHistory.getChildAdapterPosition(view);
                        if(pos != RecyclerView.NO_POSITION) {
                            Mood mood = moodList.get(pos);
                            openCommentsDialog(mood);
                        }
                    });
                }
            }
            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) { }
        });

        firestore = FirebaseFirestore.getInstance();

        // Inflate and add the filter panel (initially hidden) BEFORE loading mood history.
        filterPanel = LayoutInflater.from(getContext()).inflate(R.layout.filter_panel_mood_history, null);
        filterPanel.setVisibility(View.GONE);
        ((ViewGroup) view).addView(filterPanel);

        // Setup filter panel controls and listeners.
        setupFilterPanel();

        // Now load mood history.
        loadMoodHistory();

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
        CheckBox cbAfraid = filterPanel.findViewById(R.id.cbAfraid);
        CheckBox cbDisgusted = filterPanel.findViewById(R.id.cbDisgusted);
        CheckBox cbShame = filterPanel.findViewById(R.id.cbShame);
        CheckBox cbSurprise = filterPanel.findViewById(R.id.cbSurprise);
        CheckBox cbConfusion = filterPanel.findViewById(R.id.cbConfused);
        // Select All button for moods
        Button btnSelectAll = filterPanel.findViewById(R.id.btnSelectAll);
        // Reason filter text
        EditText etReasonFilter = filterPanel.findViewById(R.id.etReasonFilter);
        // Date filter controls
        Button btnSelectDate = filterPanel.findViewById(R.id.btnSelectDate);
        TextView tvSelectedDate = filterPanel.findViewById(R.id.tvSelectedDate);
        // Close, Apply, and Reset buttons
        ImageButton btnCloseFilter = filterPanel.findViewById(R.id.btnCloseFilter);
        Button btnApplyFilter = filterPanel.findViewById(R.id.btnApplyFilter);
        Button btnResetFilter = filterPanel.findViewById(R.id.btnResetFilter);

        // Restore previous selections
        switchRecentWeek.setChecked(recentWeekFilter);
        cbHappy.setChecked(happyChecked);
        cbSad.setChecked(sadChecked);
        cbAngry.setChecked(angryChecked);
        cbAfraid.setChecked(afraidChecked);
        cbDisgusted.setChecked(disgustedChecked);
        cbShame.setChecked(shameChecked);
        cbSurprise.setChecked(surpriseChecked);
        cbConfusion.setChecked(confusionChecked);
        etReasonFilter.setText(reasonKeyword);
        if (selectedDateFilter != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            tvSelectedDate.setText(sdf.format(selectedDateFilter));
        } else {
            tvSelectedDate.setText("Any Date");
        }
        // Set initial text for Select All button
        btnSelectAll.setText("Select All");

        // When the recent week switch is toggled, override the date filter.
        switchRecentWeek.setOnCheckedChangeListener((buttonView, isChecked) -> {
            recentWeekFilter = isChecked;
            if (isChecked) {
                // Disable date selection if 7-day filter is active.
                btnSelectDate.setEnabled(false);
                tvSelectedDate.setText("Last 7 Days");
            } else {
                btnSelectDate.setEnabled(true);
                if (selectedDateFilter != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    tvSelectedDate.setText(sdf.format(selectedDateFilter));
                } else {
                    tvSelectedDate.setText("Any Date");
                }
            }
        });

        // Select All button toggles between selecting and deselecting all mood checkboxes.
        btnSelectAll.setOnClickListener(v -> {
            if (cbHappy.isChecked() && cbSad.isChecked() && cbAngry.isChecked() &&
                    cbAfraid.isChecked() && cbDisgusted.isChecked() && cbShame.isChecked() && cbSurprise.isChecked() &&
                    cbConfusion.isChecked()) {
                // Deselect all.
                cbHappy.setChecked(false);
                cbSad.setChecked(false);
                cbAngry.setChecked(false);
                cbAfraid.setChecked(false);
                cbDisgusted.setChecked(false);
                cbShame.setChecked(false);
                cbSurprise.setChecked(false);
                cbConfusion.setChecked(false);
                btnSelectAll.setText("Select All");
            } else {
                // Select all.
                cbHappy.setChecked(true);
                cbSad.setChecked(true);
                cbAngry.setChecked(true);
                cbAfraid.setChecked(true);
                cbDisgusted.setChecked(true);
                cbShame.setChecked(true);
                cbSurprise.setChecked(true);
                cbConfusion.setChecked(true);
                btnSelectAll.setText("Deselect All");
            }
        });

        // Date filter: open DatePickerDialog when Select Date is clicked.
        btnSelectDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            if (selectedDateFilter != null) {
                calendar.setTime(selectedDateFilter);
            }
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (DatePicker view1, int year1, int month1, int dayOfMonth) -> {
                Calendar selectedCal = Calendar.getInstance();
                selectedCal.set(year1, month1, dayOfMonth, 0, 0, 0);
                selectedCal.set(Calendar.MILLISECOND, 0);
                selectedDateFilter = selectedCal.getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                tvSelectedDate.setText(sdf.format(selectedDateFilter));
            }, year, month, day);
            datePickerDialog.show();
        });

        // Close filter panel button (animate down and hide)
        btnCloseFilter.setOnClickListener(v -> hideFilterPanel());

        // Apply filter button: update filter states and reload mood history.
        btnApplyFilter.setOnClickListener(v -> {
            recentWeekFilter = switchRecentWeek.isChecked();
            happyChecked = cbHappy.isChecked();
            sadChecked = cbSad.isChecked();
            angryChecked = cbAngry.isChecked();
            afraidChecked = cbAfraid.isChecked();
            disgustedChecked = cbDisgusted.isChecked();
            shameChecked = cbShame.isChecked();
            surpriseChecked = cbSurprise.isChecked();
            confusionChecked = cbConfusion.isChecked();
            reasonKeyword = etReasonFilter.getText().toString().trim();

            Toast.makeText(getContext(), "Filter Applied", Toast.LENGTH_SHORT).show();
            hideFilterPanel();
            loadMoodHistory();
        });

        // Reset filter button: clear all selections.
        btnResetFilter.setOnClickListener(v -> {
            recentWeekFilter = false;
            happyChecked = false;
            sadChecked = false;
            angryChecked = false;
            afraidChecked = false;
            disgustedChecked = false;
            shameChecked = false;
            surpriseChecked = false;
            confusionChecked = false;
            reasonKeyword = "";
            selectedDateFilter = null;

            switchRecentWeek.setChecked(recentWeekFilter);
            cbHappy.setChecked(happyChecked);
            cbSad.setChecked(sadChecked);
            cbAngry.setChecked(angryChecked);
            cbAfraid.setChecked(afraidChecked);
            cbDisgusted.setChecked(disgustedChecked);
            cbShame.setChecked(shameChecked);
            cbSurprise.setChecked(surpriseChecked);
            cbConfusion.setChecked(confusionChecked);
            etReasonFilter.setText(reasonKeyword);
            tvSelectedDate.setText("Any Date");
            btnSelectDate.setEnabled(true);
            btnSelectAll.setText("Select All");

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

        // If recent week filter is on, use it exclusively.
        if (recentWeekFilter) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -7);
            Date sevenDaysAgo = cal.getTime();
            query = query.whereGreaterThanOrEqualTo("timestamp", new com.google.firebase.Timestamp(sevenDaysAgo));
        }
        // Otherwise, if a specific date is selected, use that.
        else if (selectedDateFilter != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(selectedDateFilter);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date startOfDay = cal.getTime();
            cal.add(Calendar.DAY_OF_MONTH, 1);
            Date endOfDay = cal.getTime();
            query = query.whereGreaterThanOrEqualTo("timestamp", new com.google.firebase.Timestamp(startOfDay))
                    .whereLessThan("timestamp", new com.google.firebase.Timestamp(endOfDay));
        }

        // Apply mood filter only if not all moods are selected.
        if (!cbAllMoodsSelected()) {
            List<String> selectedMoods = new ArrayList<>();
            CheckBox cbHappy = filterPanel.findViewById(R.id.cbHappy);
            CheckBox cbSad = filterPanel.findViewById(R.id.cbSad);
            CheckBox cbAngry = filterPanel.findViewById(R.id.cbAngry);
            CheckBox cbAfraid = filterPanel.findViewById(R.id.cbAfraid);
            CheckBox cbDisgusted = filterPanel.findViewById(R.id.cbDisgusted);
            CheckBox cbShame = filterPanel.findViewById(R.id.cbShame);
            CheckBox cbSurprise = filterPanel.findViewById(R.id.cbSurprise);
            CheckBox cbConfusion = filterPanel.findViewById(R.id.cbConfused);
            if (cbHappy.isChecked()) selectedMoods.add("Happy");
            if (cbSad.isChecked()) selectedMoods.add("Sad");
            if (cbAngry.isChecked()) selectedMoods.add("Angry");
            if (cbAfraid.isChecked()) selectedMoods.add("Afraid");
            if (cbDisgusted.isChecked()) selectedMoods.add("Disgusted");
            if (cbShame.isChecked()) selectedMoods.add("Shameful");
            if (cbSurprise.isChecked()) selectedMoods.add("Surprised");
            if (cbConfusion.isChecked()) selectedMoods.add("Confused");
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
            // Further client-side filtering for reason keyword.
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
     * Helper method to determine if all mood checkboxes are selected.
     */
    private boolean cbAllMoodsSelected() {
        CheckBox cbHappy = filterPanel.findViewById(R.id.cbHappy);
        CheckBox cbSad = filterPanel.findViewById(R.id.cbSad);
        CheckBox cbAngry = filterPanel.findViewById(R.id.cbAngry);
        CheckBox cbAfraid = filterPanel.findViewById(R.id.cbAfraid);
        CheckBox cbDisgust = filterPanel.findViewById(R.id.cbDisgusted);
        CheckBox cbShame = filterPanel.findViewById(R.id.cbShame);
        CheckBox cbSurprise = filterPanel.findViewById(R.id.cbSurprise);
        CheckBox cbConfusion = filterPanel.findViewById(R.id.cbConfused);
        return cbHappy.isChecked() && cbSad.isChecked() && cbAngry.isChecked() &&
                cbAfraid.isChecked() && cbDisgust.isChecked() && cbShame.isChecked() && cbSurprise.isChecked() &&
                cbConfusion.isChecked();
    }

    /**
     * Slides up the filter panel from the bottom.
     */
    private void showFilterPanel() {
        final View root = getView();
        if (root == null) return;
        filterPanel.setVisibility(View.VISIBLE);
        root.post(() -> {
            int rootHeight = root.getHeight();
            int panelHeight = filterPanel.getHeight();
            filterPanel.setY(rootHeight);
            filterPanel.animate()
                    .y(rootHeight - panelHeight)
                    .setDuration(300)
                    .start();
        });
    }

    /**
     * Slides down the filter panel and hides it.
     */
    private void hideFilterPanel() {
        final View root = getView();
        if (root == null) return;
        root.post(() -> {
            int rootHeight = root.getHeight();
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

    /**
     * Opens a dialog for viewing and adding comments for the given mood event.
     */
    private void openCommentsDialog(Mood mood) {
        if (mood.getMoodId() == null) {
            Toast.makeText(getContext(), "Cannot load comments: mood id is missing", Toast.LENGTH_SHORT).show();
            return;
        }
        CommentsBottomSheetFragment bottomSheet = CommentsBottomSheetFragment.newInstance(mood.getMoodId(), currentUserId);
        bottomSheet.show(getChildFragmentManager(), "CommentsBottomSheet");
    }
}
