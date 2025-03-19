package com.hamidat.nullpointersapp.mainFragments;

import android.os.Bundle;
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
import com.hamidat.nullpointersapp.R;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;

import java.util.Locale;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;


public class HomeFilterHistoryFragment extends BottomSheetDialogFragment {

    private String currentUserId;

    private FirebaseFirestore firestore;

    private FirestoreHelper firestoreHelper;

    private View filterView;

    public HomeFilterHistoryFragment (String UserID, FirestoreHelper firestoreHelperInstance) {
        currentUserId = UserID;
        firestoreHelper = firestoreHelperInstance;

//        Empty constructor for now.
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
    public void applyQueries() {

    }
}

