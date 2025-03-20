package com.hamidat.nullpointersapp.mainFragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.hamidat.nullpointersapp.R;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.models.moodHistory;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Map;


public class HomeFilterHistoryFragment extends BottomSheetDialogFragment {

    private String currentUserId;

    private FirebaseFirestore firestore;

    private FirestoreHelper firestoreHelper;

    private View filterView;

    public HomeFilterHistoryFragment (String UserID, FirestoreHelper firestoreHelperInstance) {
        currentUserId = UserID;
        firestoreHelper = firestoreHelperInstance;

    }

    public interface MoodFilterCallback {
        void onMoodFilterApplied(List<Mood> filteredMoods);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Filter view
        filterView = inflater.inflate(R.layout.filter_moods, container, false);

        // All the fields needed.
        Switch toggleSevenDays = filterView.findViewById(R.id.switchRecent7Days);
        Spinner filterSpinner = filterView.findViewById(R.id.spinnerEmotionalState);
        EditText filterDescription = filterView.findViewById(R.id.reasonDescription);

        // Buttons for applying/resetting
        MaterialButton applyFilterButton = filterView.findViewById(R.id.buttonApplyFilter);
        MaterialButton resetFilterButton = filterView.findViewById(R.id.buttonResetFilter);

        // Getting the firebase instance
        firestore = FirebaseFirestore.getInstance();

        // Setting the spinner values
        String[] emotionalStates = {"None", "Happy", "Sad", "Angry","Chill"};
        ArrayAdapter<String> emotionalAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, emotionalStates);
        filterSpinner.setAdapter(emotionalAdapter);

        // Setting the toggle text to be today - 7 days.
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        Date sevenDaysAgo = calendar.getTime();

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
        String formattedRange = sdf.format(sevenDaysAgo) + " - " + sdf.format(today);

        // Set the text on the switch
        toggleSevenDays.setText("Toggle 7 Days: (" + formattedRange + ")");

        // Apply filters
        applyFilterButton.setOnClickListener(v -> {
            retrieveUser(currentUserId, new MoodFilterCallback() {
                @Override
                public void onMoodFilterApplied(List<Mood> filteredMoods) {

                }
            });
        });

        // Reset button
        resetFilterButton.setOnClickListener(v -> {
            resetAll();
        });

        // Inflate your layout for the bottom fragment (e.g., mood_filter.xml)
        return filterView;

    }
    public void resetAll() {
        Switch toggleSevenDays = filterView.findViewById(R.id.switchRecent7Days);
        Spinner spinnerEmotionalState = filterView.findViewById(R.id.spinnerEmotionalState);
        EditText filterDescription = filterView.findViewById(R.id.reasonDescription);

        // Resets all of the parameters
        toggleSevenDays.setChecked(false);
        spinnerEmotionalState.setSelection(0);
        filterDescription.setText("");

        // Possibly do A re-query result here.
    }
    public void retrieveUser(String currentUserId, MoodFilterCallback callback) {

        // 1. Get the current firebase instance.
        firestoreHelper.getUser(currentUserId, new FirestoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                // Getting the following list as well as the user data.
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
        // Applying the query for all the states, requires 1 long query.
    }

    public void applyFilters(List<String> followingList, MoodFilterCallback callback) {
        // Start building the main query.

        // Add followingList to current user for querying all moods from followers including the users.
        Log.d("FirestoreQuery", "applyFilters() called");

        // Get all the values.
        Switch toggleSevenDays = filterView.findViewById(R.id.switchRecent7Days);
        Spinner filterSpinner = filterView.findViewById(R.id.spinnerEmotionalState);
        EditText filterDescription = filterView.findViewById(R.id.reasonDescription);

        boolean SevenDaysFilter = toggleSevenDays.isChecked();
        String selectedEmotion = filterSpinner.getSelectedItem().toString();
        String searchDescription = filterDescription.getText().toString().trim();

        CollectionReference allMoods = firestore.collection("moods");
        Query filterQuery = allMoods;



        if (followingList != null && !followingList.isEmpty()) {
            filterQuery = filterQuery.whereIn("userId", followingList);
        }

        // For moods might set to a toggle and then check if its in the moodsList.
        if (selectedEmotion != null && !selectedEmotion.isEmpty() && selectedEmotion != "None") {
            filterQuery = filterQuery.whereEqualTo("mood", selectedEmotion);
        }

//        if (SevenDaysFilter) {
//            // Compute the timestamp for 7 days ago.
//            Calendar calendar = Calendar.getInstance();
//            calendar.add(Calendar.DAY_OF_YEAR, -7);
//            Date sevenDaysAgo = calendar.getTime();
//            filterQuery = filterQuery.whereGreaterThanOrEqualTo("timestamp", new com.google.firebase.Timestamp(sevenDaysAgo));
//        }

        filterQuery.addSnapshotListener((querySnapshot, e) -> {
            ArrayList<Mood> filteredMoods = new ArrayList<>();

            if (querySnapshot != null) {
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Mood mood = doc.toObject(Mood.class);
                    filteredMoods.add(mood);
                    Log.d("FirestoreQuery", "Mood Retrieved: " + mood.getMood() + " - " + mood.getMoodDescription());
                }
            }
            if (callback != null) {
                callback.onMoodFilterApplied(filteredMoods);
            }
        });


    }

}

