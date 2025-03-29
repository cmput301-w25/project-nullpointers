/**
 * MoodHistoryFragment.java
 *
 * This fragment displays a chronological list of the current user's past mood entries.
 * It includes advanced filtering options (e.g., mood type, date range, keyword, recent 7 days)
 * and allows users to view and comment on each mood. It also computes the most frequent mood.
 *
 * <p><b>Outstanding issues:</b> None.</p>
 */

package com.hamidat.nullpointersapp.mainFragments;

import android.app.DatePickerDialog;
import android.graphics.Color;
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
import androidx.cardview.widget.CardView;
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
import com.hamidat.nullpointersapp.utils.homeFeedUtils.CommentsBottomSheetFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import com.hamidat.nullpointersapp.utils.homeFeedUtils.CommentsBottomSheetFragment;

public class MoodHistoryFragment extends Fragment {

    private RecyclerView rvMoodHistory;
    private TextView tvMostFrequentMood, tvNoMoodEntries;
    private MoodAdapter moodAdapter;
    private List<Mood> moodList = new ArrayList<>();
    private FirebaseFirestore firestore;
    private String currentUserId;
    private Button btnMoodHistoryFilter;
    private CardView cardFromDate, cardToDate;
    private TextView tvFromDate, tvToDate;
    private Timestamp fromTimestamp, toTimestamp;

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

        // Add the comment listener for the person mood history too
        rvMoodHistory.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {
                Button btnComment = view.findViewById(R.id.btnComment);
                if (btnComment != null) {
                    btnComment.setOnClickListener(v -> {
                        int pos = rvMoodHistory.getChildAdapterPosition(view);
                        if (pos != RecyclerView.NO_POSITION) {
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
        // Close, Apply, and Reset buttons
        ImageButton btnCloseFilter = filterPanel.findViewById(R.id.btnCloseFilter);
        Button btnApplyFilter = filterPanel.findViewById(R.id.btnApplyFilter);
        Button btnResetFilter = filterPanel.findViewById(R.id.btnResetFilter);

        // Date controls
        cardFromDate = filterPanel.findViewById(R.id.cardFromDate);
        cardToDate = filterPanel.findViewById(R.id.cardToDate);
        tvToDate = filterPanel.findViewById(R.id.textToDate);
        tvFromDate = filterPanel.findViewById(R.id.textFromDate);

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
        // Set initial text for Select All button
        btnSelectAll.setText("Select All");

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

        // Card Date filtering.
        updateDateText(tvFromDate, fromTimestamp);
        updateDateText(tvToDate, toTimestamp);

        cardFromDate.setOnClickListener(v -> openDatePicker(tvFromDate, true));
        cardToDate.setOnClickListener(v -> openDatePicker(tvToDate, false));

        switchRecentWeek.setChecked(recentWeekFilter);
        if (recentWeekFilter) {
            applySevenDayToggleState(recentWeekFilter);
        }

        switchRecentWeek.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                recentWeekFilter = true;
                applySevenDayToggleState(recentWeekFilter);

            } else {
                recentWeekFilter = false;
                applySevenDayToggleState(recentWeekFilter);
            }
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

            switchRecentWeek.setChecked(false);
            cbHappy.setChecked(happyChecked);
            cbSad.setChecked(sadChecked);
            cbAngry.setChecked(angryChecked);
            cbAfraid.setChecked(afraidChecked);
            cbDisgusted.setChecked(disgustedChecked);
            cbShame.setChecked(shameChecked);
            cbSurprise.setChecked(surpriseChecked);
            cbConfusion.setChecked(confusionChecked);
            etReasonFilter.setText(reasonKeyword);
            btnSelectAll.setText("Select All");
            fromTimestamp = null;
            toTimestamp = null;
            updateDateText(tvFromDate, fromTimestamp);
            updateDateText(tvToDate, toTimestamp);

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


        if (fromTimestamp != null && toTimestamp != null) {
            if (fromTimestamp.compareTo(toTimestamp) > 0) {
                Toast.makeText(getContext(), "Cannot filter with 'From Date' after 'To Date'!", Toast.LENGTH_SHORT).show();
            }
            query = query.whereGreaterThanOrEqualTo("timestamp", fromTimestamp)
                    .whereLessThanOrEqualTo("timestamp", toTimestamp);
        } else if ((fromTimestamp == null) ^ (toTimestamp == null)) {
            Toast.makeText(getContext(), "To filter by time, please have both date ranges selected!", Toast.LENGTH_SHORT).show();
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

    private void openCommentsDialog(Mood mood) {
        if (mood.getMoodId() == null) {
            Toast.makeText(getContext(), "Cannot load comments: mood id is missing", Toast.LENGTH_SHORT).show();
            return;
        }
        CommentsBottomSheetFragment bottomSheet = CommentsBottomSheetFragment.newInstance(mood.getMoodId(), currentUserId);

        // When the dialog is dismissed, update only the comment count from Firestore
        bottomSheet.setOnDismissListener(() -> {
            FirebaseFirestore.getInstance()
                    .collection("moods")
                    .document(mood.getMoodId())
                    .get()
                    .addOnSuccessListener(doc -> {
                        Long updatedCount = doc.getLong("commentCount");
                        if (updatedCount != null) {
                            mood.setCommentCount(updatedCount.intValue());
                            int index = moodList.indexOf(mood);
                            if (index != -1) {
                                moodAdapter.notifyItemChanged(index, "commentCountOnly");
                            }
                        }
                    });
        });

        bottomSheet.show(getChildFragmentManager(), "CommentsBottomSheet");
    }

    /**
     * Takes in a textView and timestamp, converts timestamp to sdf format and into textView for displaying inside of fragment.
     *
     * @param textView the textview (which should hold the time)
     * @param timestamp the timestamp we are displaying to a textView.
     *
     */
    private void updateDateText(TextView textView, Timestamp timestamp) {
        if (timestamp == null) {
            textView.setText("Select Date");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        textView.setText(sdf.format(timestamp.toDate()));
    }

    /**
     * Takes in a textView and timestamp, converts timestamp to sdf format and into textView for displaying inside of fragment.
     *
     * @param textView the textview (which should hold the time)
     * @param isFromDate a boolean value which indicates if it is we are picking the too value or the from value.
     *
     */
    private void openDatePicker(TextView textView, boolean isFromDate) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(year, month, dayOfMonth, 0, 0, 0);

                    Timestamp selectedTimestamp = new Timestamp(selectedCalendar.getTime());

                    // Getting today's timestamp
                    Calendar today = Calendar.getInstance();
                    Timestamp todayTimestamp = new Timestamp(today.getTime());

                    if (isFromDate) {
                        if (selectedTimestamp.compareTo(todayTimestamp) > 0) {
                            Toast.makeText(getContext(), "End date cannot be in the future!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        fromTimestamp = selectedTimestamp;
                    } else {
                        // For toTimestamp ensure that it goes to end of the day.
                        selectedCalendar.set(Calendar.HOUR_OF_DAY, 23);
                        selectedCalendar.set(Calendar.MINUTE, 59);
                        selectedCalendar.set(Calendar.SECOND, 59);
                        selectedCalendar.set(Calendar.MILLISECOND, 999);

                        // Checking if toDate was set into future (not allowed)
                        if (selectedTimestamp.compareTo(todayTimestamp) > 0) {
                            Toast.makeText(getContext(), "End date cannot be in the future!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        toTimestamp = new Timestamp(selectedCalendar.getTime());
                    }
                    updateDateText(textView, selectedTimestamp);

                },calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }


    /**
     * applySevenDayToggleState is a function which checks if setToggleWeek is true, accordingly updates the
     * usability of the calendar.
     *
     * @param isEnabled the boolean value to determine if calendar functionality should be set.
     */
    private void applySevenDayToggleState(boolean isEnabled) {
        if (isEnabled) {
            cardFromDate.setEnabled(false);
            cardToDate.setEnabled(false);
            tvFromDate.setTextColor(Color.GRAY);
            tvToDate.setTextColor(Color.GRAY);

            Calendar sevenDays = Calendar.getInstance();
            sevenDays.add(Calendar.DAY_OF_YEAR, -7);
            fromTimestamp = new Timestamp(sevenDays.getTime());

            Calendar today = Calendar.getInstance();
            toTimestamp = new Timestamp(today.getTime());

            updateDateText(tvFromDate, fromTimestamp);
            updateDateText(tvToDate, toTimestamp);
        } else {
            cardFromDate.setEnabled(true);
            cardToDate.setEnabled(true);
            tvFromDate.setTextColor(Color.BLACK);
            tvToDate.setTextColor(Color.BLACK);

            fromTimestamp = null;
            toTimestamp = null;

            updateDateText(tvFromDate, null);
            updateDateText(tvToDate, null);
        }
    }
}
