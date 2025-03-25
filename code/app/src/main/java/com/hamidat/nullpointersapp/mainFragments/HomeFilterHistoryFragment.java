package com.hamidat.nullpointersapp.mainFragments;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.hamidat.nullpointersapp.R;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hamidat.nullpointersapp.models.Mood;

import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;
import com.google.firebase.Timestamp;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class HomeFilterHistoryFragment extends BottomSheetDialogFragment {

    private String currentUserId;

    private FirebaseFirestore firestore;
    private FirestoreHelper firestoreHelper;
    private View filterView;
    private MoodFilterCallback callback;

    private CardView cardFromDate, cardToDate;
    private TextView tvFromDate, tvToDate;
    private Timestamp fromTimestamp, toTimestamp;
    private CheckBox cbHappy, cbSad, cbAngry, cbChill, cbAll, cbConfused, cbDisgust, cbFear, cbShame, cbSuprised;
    private EditText filterDescription;
    private List<String> checkedEmotions;
    private String filterDescriptionText;
    private Switch toggleSevenDays, toggleAscendingOrder;

    private Boolean setToggleWeek, setOrder;

    public HomeFilterHistoryFragment (String UserID, FirestoreHelper firestoreHelperInstance, Timestamp savedToTimestamp, Timestamp savedFromTimestamp,
                                      String savedFilterDescription, List<String> savedCheckedEmotions, boolean savedToggleWeek, boolean savedToggleAscending, MoodFilterCallback moodCallback) {
        currentUserId = UserID;
        firestoreHelper = firestoreHelperInstance;

        // Retrieving all the saved filer values.
        toTimestamp = savedToTimestamp;
        fromTimestamp = savedFromTimestamp;
        filterDescriptionText = savedFilterDescription;
        checkedEmotions = savedCheckedEmotions;

        setToggleWeek = savedToggleWeek;
        setOrder = savedToggleAscending;

        callback = moodCallback;
    }

    public interface MoodFilterCallback {
        void onMoodFilterApplied(List<Mood> filteredMoods, Timestamp savingTo, Timestamp savingFrom, String savingDescription,
                                 List<String> savingEmotions, boolean setToggleWeek, boolean setOrder);
        void onShowToast(String message);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        filterView = inflater.inflate(R.layout.filter_moods, container, false);
        firestore = FirebaseFirestore.getInstance();

        cbAll = filterView.findViewById(R.id.checkboxAll);
        cbHappy = filterView.findViewById(R.id.checkboxHappy);
        cbSad = filterView.findViewById(R.id.checkboxSad);
        cbAngry = filterView.findViewById(R.id.checkboxAngry);
        cbChill = filterView.findViewById(R.id.checkboxChill);

        cbConfused = filterView.findViewById(R.id.checkboxConfused);
        cbShame = filterView.findViewById(R.id.checkboxShame);
        cbFear = filterView.findViewById(R.id.checkboxFear);
        cbDisgust = filterView.findViewById(R.id.checkboxDisgust);
        cbSuprised = filterView.findViewById(R.id.checkboxSuprised);

        tvToDate = filterView.findViewById(R.id.textToDate);
        tvFromDate = filterView.findViewById(R.id.textFromDate);

        // cardView for choosing date.
        cardFromDate = filterView.findViewById(R.id.cardFromDate);
        cardToDate = filterView.findViewById(R.id.cardToDate);

        // description for filtering text.
        filterDescription = filterView.findViewById(R.id.reasonDescription);
        toggleSevenDays = filterView.findViewById(R.id.switchRecent7Days);
        toggleAscendingOrder = filterView.findViewById(R.id.ascendingOrder);

        // Buttons for applying/resetting
        MaterialButton applyFilterButton = filterView.findViewById(R.id.buttonApplyFilter);
        MaterialButton resetFilterButton = filterView.findViewById(R.id.buttonResetFilter);

        // Setting the checked emotions from the saved
        if (checkedEmotions != null) {
            cbHappy.setChecked(checkedEmotions.contains("Happy"));
            cbSad.setChecked(checkedEmotions.contains("Sad"));
            cbAngry.setChecked(checkedEmotions.contains("Angry"));
            cbChill.setChecked(checkedEmotions.contains("Chill"));
            cbConfused.setChecked(checkedEmotions.contains("Confused"));
            cbShame.setChecked(checkedEmotions.contains("Shame"));
            cbFear.setChecked(checkedEmotions.contains("Fear"));
            cbDisgust.setChecked(checkedEmotions.contains("Disgust"));
            cbSuprised.setChecked(checkedEmotions.contains("Suprised"));

        } else {
            // If null then none are selected.
            cbAll.setChecked(true);
        }

        updateDateText(tvFromDate, fromTimestamp);
        updateDateText(tvToDate, toTimestamp);

        toggleSevenDays.setChecked(setToggleWeek);
        toggleAscendingOrder.setChecked(setOrder);

        // If setToggle is true, then we are still having disabled calendar
        if (setToggleWeek) {
            applySevenDayToggleState(setToggleWeek);
        }


        // Toggle seven says selector automatically sets the date to days before and allow the queries.
        toggleSevenDays.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                setToggleWeek = true;
                applySevenDayToggleState(setToggleWeek);


            } else {
                setToggleWeek = false;
                applySevenDayToggleState(setToggleWeek);
            }
        });

        // Set click listeners on the CardViews for picking the dates.
        cardFromDate.setOnClickListener(v -> openDatePicker(tvFromDate, true));
        cardToDate.setOnClickListener(v -> openDatePicker(tvToDate, false));

        // set the filter current EditText to be the text.
        filterDescription.setText(filterDescriptionText);

        applyFilterButton.setOnClickListener(v -> {
            // Button clicked use the callback.
            retrieveUser(currentUserId, callback);
            dismiss();
        });

        resetFilterButton.setOnClickListener(v -> {
            // Resets all the fields and then calls retrieveUser and re-queries again.
            resetAll();
            retrieveUser(currentUserId, callback);
            dismiss();
        });
        return filterView;
    }

    /**
     * Function connected to the resetButton to reset all of the fields.
     */
    public void resetAll() {
        // resetting all the fields, and then recalling

        filterDescription.setText("");
        toTimestamp = null;
        fromTimestamp = null;
        // ascending = false and recent week false
        setToggleWeek = false;
        setOrder = false;
        toggleSevenDays.setChecked(false);
        toggleAscendingOrder.setChecked(false);

        // all emotional states removed
        cbHappy.setChecked(false);
        cbSad.setChecked(false);
        cbAngry.setChecked(false);
        cbChill.setChecked(false);
        cbFear.setChecked(false);
        cbDisgust.setChecked(false);
        cbShame.setChecked(false);
        cbSuprised.setChecked(false);
        cbConfused.setChecked(false);

        // default cbAll set.
        cbAll.setChecked(true);
    }

    /**
     * Retrieves the currentUser's document in users collection and adds the individuals that are being followed to an ArrayList.
     * On successful callback, it will create the full following list + currentUser IDS which will be used to get moods for filtering.
     *
     * @param currentUserId Current ID of the user.
     * @param callback The callback used for displaying the data onto the homepage once moods are filtered.
     *
     */
    public void retrieveUser(String currentUserId, MoodFilterCallback callback) {

        // 1. Get the current firebase instance.
        firestoreHelper.getUser(currentUserId, new FirestoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {

                // Getting the user document into Map.
                Map<String, Object> userData = (Map<String, Object>) result;

                // Initialize followingList to have all the users that the current user is following.
                List<String> followingList = (List<String>) userData.get("following");

                // If following nobody then we initialize an empty array list.
                if (followingList == null) {
                    followingList = new ArrayList<>();
                }

                // Add user to the following list
                followingList.add(currentUserId);

                // Apply the filters, the callback goes back to the button to be displayed onto HomeFragment.
                applyFilters(followingList, callback);
            }
            @Override
            public void onFailure(Exception e) {
                Log.d("FirestoreFail", "Failed:");
            }
        });

    }

    /**
     * Retrieves the currentUser's document in users collection and adds the individuals that are being followed to an ArrayList.
     * On successful callback, it will create the full following list + currentUser IDS which will be used to get moods for filtering.
     *
     * @param followingList the list of IDS followed by user, including own ID.
     * @param callback The callback used for displaying the data onto the homepage once moods are filtered.
     *
     */
    public void applyFilters(List<String> followingList, MoodFilterCallback callback) {
        // Start building the main query.

        // Add followingList to current user for querying all moods from followers including the users.
        List<String> selectedEmotions = new ArrayList<>();
        if (cbHappy.isChecked()) selectedEmotions.add("Happy");
        if (cbSad.isChecked()) selectedEmotions.add("Sad");
        if (cbAngry.isChecked()) selectedEmotions.add("Angry");
        if (cbChill.isChecked()) selectedEmotions.add("Chill");

        if (cbFear.isChecked()) selectedEmotions.add("Fear");
        if (cbDisgust.isChecked()) selectedEmotions.add("Disgust");
        if (cbShame.isChecked()) selectedEmotions.add("Shame");
        if (cbSuprised.isChecked()) selectedEmotions.add("Suprised");
        if (cbConfused.isChecked()) selectedEmotions.add("Confused");

        // Getting the current description.
        EditText filterDescription = filterView.findViewById(R.id.reasonDescription);
        String searchDescription = filterDescription.getText().toString().trim();

        // Beginning to Construct the Query

        CollectionReference allMoods = firestore.collection("moods");
        Query filterQuery = allMoods;

        if (followingList != null && !followingList.isEmpty()) {
            filterQuery = filterQuery.whereIn("userId", followingList);
        }

        // Get the timestamps in the range. initially null
        if (fromTimestamp != null && toTimestamp != null) {
            filterQuery = filterQuery
                    .whereGreaterThanOrEqualTo("timestamp", fromTimestamp)
                    .whereLessThanOrEqualTo("timestamp", toTimestamp);
        } else if ((fromTimestamp == null) ^ (toTimestamp == null)) {
            if (callback != null) {
                callback.onShowToast("To filter by time, please have both date ranges selected!");
            }

        }


        // Get all the checked emotions if cbAll is checked is true then we don't need to run this query here.
        if (!selectedEmotions.isEmpty() && !cbAll.isChecked()) {
            filterQuery = filterQuery.whereIn("mood", selectedEmotions);
        }

        // Running query for querying by ascending (will display oldest first)

        if (toggleAscendingOrder.isChecked()) {
            filterQuery = filterQuery.orderBy("timestamp", Query.Direction.ASCENDING);
            setOrder = true;
        } else {
            filterQuery = filterQuery.orderBy("timestamp", Query.Direction.DESCENDING);
            setOrder = false;
        }

        filterQuery.addSnapshotListener((querySnapshot, e) -> {
            ArrayList<Mood> filteredMoods = new ArrayList<>();

            if (querySnapshot != null) {
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Mood mood = doc.toObject(Mood.class);
                    if (mood.isPrivate() && !mood.getUserId().equals(currentUserId)) {
                        continue;
                    }
                    filteredMoods.add(mood);

                    // Call the filtering reason for the description.
                    filteredMoods = filterReason(filteredMoods, searchDescription);
                }
            }
            if (callback != null) {
                callback.onMoodFilterApplied(filteredMoods, toTimestamp, fromTimestamp, searchDescription, selectedEmotions, setToggleWeek, setOrder);
            }
        });

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
     * filterReason is a function that takes in An arraylist of moods and then the keyword we want to filter for,
     *
     * @param moods an array of mood objects that need to be filtered by description.
     * @param keyword the value that we need to filter by.
     * @return the filtered moods array.
     */
    private ArrayList<Mood> filterReason(ArrayList<Mood> moods, String keyword) {
        // Filtering the reason, need to work directly with the current filteredMoods and return moods that match the query.

        if (keyword == null || keyword.trim().isEmpty()) return moods;

        ArrayList<Mood> filteredList = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();

        for (Mood mood : moods) {
            String description = mood.getMoodDescription() != null ? mood.getMoodDescription().toLowerCase() : "";
            if (description.contains(lowerKeyword)) {
                filteredList.add(mood);
            }
        }
        return filteredList;
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

        // Also update the variable so it's passed back in the callback
    }


}

