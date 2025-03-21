package com.hamidat.nullpointersapp.mainFragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
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
    private CheckBox cbHappy, cbSad, cbAngry, cbChill, cbAll;
    private EditText filterDescription;


    public HomeFilterHistoryFragment (String UserID, FirestoreHelper firestoreHelperInstance, MoodFilterCallback moodCallback) {
        currentUserId = UserID;
        firestoreHelper = firestoreHelperInstance;
        callback = moodCallback;
    }

    public interface MoodFilterCallback {
        void onMoodFilterApplied(List<Mood> filteredMoods);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        filterView = inflater.inflate(R.layout.filter_moods, container, false);
        firestore = FirebaseFirestore.getInstance();

        cbAll = filterView.findViewById(R.id.checkboxAll);
        cbAll.setChecked(true);
        cbHappy = filterView.findViewById(R.id.checkboxHappy);
        cbSad = filterView.findViewById(R.id.checkboxSad);
        cbAngry = filterView.findViewById(R.id.checkboxAngry);
        cbChill = filterView.findViewById(R.id.checkboxChill);

        tvToDate = filterView.findViewById(R.id.textToDate);
        tvFromDate = filterView.findViewById(R.id.textFromDate);

        // cardView for choosing date.
        cardFromDate = filterView.findViewById(R.id.cardFromDate);
        cardToDate = filterView.findViewById(R.id.cardToDate);

        // description for filtering text.
        filterDescription = filterView.findViewById(R.id.reasonDescription);

        // Buttons for applying/resetting
        MaterialButton applyFilterButton = filterView.findViewById(R.id.buttonApplyFilter);
        MaterialButton resetFilterButton = filterView.findViewById(R.id.buttonResetFilter);

        Calendar calendar = Calendar.getInstance();

        // initialize the timestamp to be current.
        fromTimestamp = new Timestamp(calendar.getTime());
        toTimestamp = new Timestamp(calendar.getTime());

        // displaying to the text views in simple date format.
        updateDateText(tvFromDate, fromTimestamp);
        updateDateText(tvToDate, toTimestamp);

        // Set click listeners on the CardViews for picking the dates.
        cardFromDate.setOnClickListener(v -> openDatePicker(tvFromDate, true));
        cardToDate.setOnClickListener(v -> openDatePicker(tvToDate, false));

        // Apply filters
        applyFilterButton.setOnClickListener(v -> {
            // Button clicked use the callback.
            retrieveUser(currentUserId, callback);
            dismiss();
        });

        // Reset button
        resetFilterButton.setOnClickListener(v -> {
            resetAll();
        });

        return filterView;
    }

    public void resetAll() {
        EditText filterDescription = filterView.findViewById(R.id.reasonDescription);
        filterDescription.setText("");
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

        // Getting the current description.
        EditText filterDescription = filterView.findViewById(R.id.reasonDescription);
        String searchDescription = filterDescription.getText().toString().trim();

        // Retrieving the moods collection.
        CollectionReference allMoods = firestore.collection("moods");
        Query filterQuery = allMoods;

        // Getting all the moods of the followed users (base query containing everything)
        if (followingList != null && !followingList.isEmpty()) {
            filterQuery = filterQuery.whereIn("userId", followingList);
        }

        // Get the timestamps in the range. initially null
        if (fromTimestamp != null && toTimestamp != null) {
            filterQuery = filterQuery
                    .whereGreaterThanOrEqualTo("timestamp", fromTimestamp)
                    .whereLessThanOrEqualTo("timestamp", toTimestamp);
            Log.d("TimestampDebug", "From: " + fromTimestamp.toDate().toString());
            Log.d("TimestampDebug", "To: " + toTimestamp.toDate().toString());
        }

        // Get all the checked emotions.if cbAll is checked is true then we don't need to run this query here.
        if (!selectedEmotions.isEmpty() && !cbAll.isChecked()) {
            filterQuery = filterQuery.whereIn("mood", selectedEmotions);
        }

        filterQuery.addSnapshotListener((querySnapshot, e) -> {
            ArrayList<Mood> filteredMoods = new ArrayList<>();

            if (querySnapshot != null) {
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Mood mood = doc.toObject(Mood.class);
                    filteredMoods.add(mood);

                }
            }
            if (callback != null) {
                callback.onMoodFilterApplied(filteredMoods);
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
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        textView.setText(sdf.format(timestamp.toDate()));
    }

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

}

