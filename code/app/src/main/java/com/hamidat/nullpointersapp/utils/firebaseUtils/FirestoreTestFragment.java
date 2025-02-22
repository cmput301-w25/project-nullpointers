package com.hamidat.nullpointersapp.utils.firebaseUtils;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.models.moodHistory;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;

public class FirestoreTestFragment extends Fragment {

    private FirestoreHelper firestoreHelper;
    private EditText editTextUserId, editTextUserName, editTextSearchUsername, editTextMood, editTextMoodDescription;
    private Button buttonAddUser, buttonGetUser, buttonGetUserByUsername, buttonAddMood, buttonRetrieveHistory, buttonQueryEmotional, buttonQueryTime;
    private TextView textViewResults;

    public FirestoreTestFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_firestore_test, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize FirestoreHelper
        firestoreHelper = new FirestoreHelper();

        // UI elements
        editTextUserId = view.findViewById(R.id.editTextUserId);
        editTextUserName = view.findViewById(R.id.editTextUserName);
        editTextSearchUsername = view.findViewById(R.id.editTextSearchUsername);
        editTextMood = view.findViewById(R.id.editTextMood);
        editTextMoodDescription = view.findViewById(R.id.editTextMoodDescription);
        buttonAddUser = view.findViewById(R.id.buttonAddUser);
        buttonGetUser = view.findViewById(R.id.buttonGetUser);
        buttonGetUserByUsername = view.findViewById(R.id.buttonGetUserByUsername);
        buttonAddMood = view.findViewById(R.id.buttonAddMood);
        buttonRetrieveHistory = view.findViewById(R.id.buttonRetrieveHistory);
        buttonQueryEmotional = view.findViewById(R.id.buttonQueryEmotional);
        buttonQueryTime = view.findViewById(R.id.buttonQueryTime);
        textViewResults = view.findViewById(R.id.textViewResults);

        // Add User Button (if you already know the userID, otherwise Firestore can auto-generate one)
        buttonAddUser.setOnClickListener(v -> {
            String userId = editTextUserId.getText().toString().trim();
            String userName = editTextUserName.getText().toString().trim();
            if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(userName)) {
                showToast("Enter User ID and Name");
                return;
            }

            firestoreHelper.addUser(userId, userName, new FirestoreHelper.FirestoreCallback() {
                @Override
                public void onSuccess(Object result) {
                    showToast(result.toString());
                }

                @Override
                public void onFailure(Exception e) {
                    showToast("Error: " + e.getMessage());
                }
            });
        });

        // Get User by UserID Button
        buttonGetUser.setOnClickListener(v -> {
            String userId = editTextUserId.getText().toString().trim();
            if (TextUtils.isEmpty(userId)) {
                showToast("Enter User ID");
                return;
            }

            firestoreHelper.getUser(userId, new FirestoreHelper.FirestoreCallback() {
                @Override
                public void onSuccess(Object result) {
                    textViewResults.setText("User Data: " + result.toString());
                }

                @Override
                public void onFailure(Exception e) {
                    showToast("Error: " + e.getMessage());
                }
            });
        });

        // Get User by Username Button
        buttonGetUserByUsername.setOnClickListener(v -> {
            String username = editTextSearchUsername.getText().toString().trim();
            if (TextUtils.isEmpty(username)) {
                showToast("Enter Username");
                return;
            }

            firestoreHelper.getUserByUsername(username, new FirestoreHelper.FirestoreCallback() {
                @Override
                public void onSuccess(Object result) {
                    textViewResults.setText("User Data: " + result.toString());
                }

                @Override
                public void onFailure(Exception e) {
                    showToast("Error: " + e.getMessage());
                }
            });
        });

        // Add Mood Button
        buttonAddMood.setOnClickListener(v -> {
            String userId = editTextUserId.getText().toString().trim();
            String moodType = editTextMood.getText().toString().trim();
            String moodDescription = editTextMoodDescription.getText().toString().trim();

            if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(moodType) || TextUtils.isEmpty(moodDescription)) {
                showToast("Please fill all mood fields and have a User ID");
                return;
            }

            Mood mood = new Mood(moodType, moodDescription);
            moodHistory userMoodHistory = new moodHistory();
            userMoodHistory.setUserID(userId);
            userMoodHistory.addMood(mood);

            firestoreHelper.moodHistoryToFirebase(userId, userMoodHistory, new FirestoreHelper.FirestoreCallback() {
                @Override
                public void onSuccess(Object result) {
                    showToast(result.toString());
                }

                @Override
                public void onFailure(Exception e) {
                    showToast("Error: " + e.getMessage());
                }
            });
        });

        // Retrieve Mood History Button
        buttonRetrieveHistory.setOnClickListener(v -> {
            String userId = editTextUserId.getText().toString().trim();
            if (TextUtils.isEmpty(userId)) {
                showToast("Enter User ID");
                return;
            }

            firestoreHelper.firebaseToMoodHistory(userId, new FirestoreHelper.FirestoreCallback() {
                @Override
                public void onSuccess(Object result) {
                    displayMoodHistory((moodHistory) result);
                }

                @Override
                public void onFailure(Exception e) {
                    showToast("Error: " + e.getMessage());
                }
            });
        });

        // Query Moods by Emotion
        buttonQueryEmotional.setOnClickListener(v -> {
            String userId = editTextUserId.getText().toString().trim();
            String moodType = editTextMood.getText().toString().trim();
            if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(moodType)) {
                showToast("Enter User ID and Mood Type");
                return;
            }

            firestoreHelper.firebaseQueryEmotional(userId, moodType, new FirestoreHelper.FirestoreCallback() {
                @Override
                public void onSuccess(Object result) {
                    displayMoodHistory((moodHistory) result);
                }

                @Override
                public void onFailure(Exception e) {
                    showToast("Error: " + e.getMessage());
                }
            });
        });

        // Query Last 7 Days Mood History
        buttonQueryTime.setOnClickListener(v -> {
            String userId = editTextUserId.getText().toString().trim();
            if (TextUtils.isEmpty(userId)) {
                showToast("Enter User ID");
                return;
            }

            firestoreHelper.firebaseQueryTime(userId, true, false, new FirestoreHelper.FirestoreCallback() {
                @Override
                public void onSuccess(Object result) {
                    displayMoodHistory((moodHistory) result);
                }

                @Override
                public void onFailure(Exception e) {
                    showToast("Error: " + e.getMessage());
                }
            });
        });
    }

    private void displayMoodHistory(moodHistory userHistory) {
        StringBuilder result = new StringBuilder("Mood History:\n");
        for (Mood mood : userHistory.getMoodArray()) {
            result.append("Mood: ").append(mood.getMood())
                    .append("\nDescription: ").append(mood.getMoodDescription())
                    .append("\nTimestamp: ").append(mood.getTimestamp().toDate())
                    .append("\n\n");
        }
        textViewResults.setText(result.toString());
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}

