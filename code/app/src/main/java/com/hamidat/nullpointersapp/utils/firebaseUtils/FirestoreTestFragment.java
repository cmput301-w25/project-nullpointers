package com.hamidat.nullpointersapp.utils.firebaseUtils;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.models.moodHistory;
import com.hamidat.nullpointersapp.R;

/**
 * A fragment to test Firestore operations related to users and mood history.
 */
public class FirestoreTestFragment extends Fragment {

    private FirestoreHelper firestoreHelper;
    // User-related fields
    private EditText editTextUserId;
    private EditText editTextUserName;
    private EditText editTextUserPassword;
    private EditText editTextSearchUsername;
    // Mood-related fields
    private EditText editTextMood;
    private EditText editTextMoodDescription;
    // Buttons
    private Button buttonAddUser;
    private Button buttonGetUser;
    private Button buttonGetUserByUsername;
    private Button buttonAddMood;
    private Button buttonRetrieveHistory;
    private Button buttonQueryEmotional;
    private Button buttonQueryTime;
    // Results display
    private TextView textViewResults;

    /**
     * Required empty public constructor.
     */
    public FirestoreTestFragment() {
        // Empty constructor required for Fragment instantiation.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_firestore_test, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firestoreHelper = new FirestoreHelper();

        // Bind UI elements for user operations.
        editTextUserId = view.findViewById(R.id.editTextUserId);
        editTextUserName = view.findViewById(R.id.editTextUserName);
        editTextUserPassword = view.findViewById(R.id.editTextUserPassword);
        editTextSearchUsername = view.findViewById(R.id.editTextSearchUsername);
        // Bind UI elements for mood operations.
        editTextMood = view.findViewById(R.id.editTextMood);
        editTextMoodDescription = view.findViewById(R.id.editTextMoodDescription);
        // Bind buttons.
        buttonAddUser = view.findViewById(R.id.buttonAddUser);
        buttonGetUser = view.findViewById(R.id.buttonGetUser);
        buttonGetUserByUsername = view.findViewById(R.id.buttonGetUserByUsername);
        buttonAddMood = view.findViewById(R.id.buttonAddMood);
        buttonRetrieveHistory = view.findViewById(R.id.buttonRetrieveHistory);
        buttonQueryEmotional = view.findViewById(R.id.buttonQueryEmotional);
        buttonQueryTime = view.findViewById(R.id.buttonQueryTime);
        // Bind results TextView.
        textViewResults = view.findViewById(R.id.textViewResults);

        // --- USER OPERATIONS ---

        // Add User Button – requires both username and password.
        buttonAddUser.setOnClickListener(v -> {
            final String userName = editTextUserName.getText().toString().trim();
            final String userPassword = editTextUserPassword.getText().toString().trim();
            if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(userPassword)) {
                showToast("Enter both User Name and Password");
                return;
            }
            firestoreHelper.addUser(userName, userPassword, new FirestoreHelper.FirestoreCallback() {
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

        // Get User by UserID Button.
        buttonGetUser.setOnClickListener(v -> {
            final String userId = editTextUserId.getText().toString().trim();
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

        // Get User by Username Button.
        buttonGetUserByUsername.setOnClickListener(v -> {
            final String username = editTextSearchUsername.getText().toString().trim();
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

        // --- MOOD OPERATIONS ---

        // Add Mood Button.
        buttonAddMood.setOnClickListener(v -> {
            final String userId = editTextUserId.getText().toString().trim();
            final String moodType = editTextMood.getText().toString().trim();
            final String moodDescription = editTextMoodDescription.getText().toString().trim();

            if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(moodType)
                    || TextUtils.isEmpty(moodDescription)) {
                showToast("Please fill all mood fields and provide a User ID");
                return;
            }

            // Create Mood with default location and social situation for testing
            Mood mood = new Mood(
                    moodType,
                    moodDescription,
                    0.0,  // latitude
                    0.0,  // longitude
                    "Test Situation"  // social situation
            );

            // Save to Firestore
            firestoreHelper.addMood(userId, mood, new FirestoreHelper.FirestoreCallback() {
                @Override
                public void onSuccess(Object result) {
                    showToast("Test mood added successfully!");
                }

                @Override
                public void onFailure(Exception e) {
                    showToast("Error adding test mood: " + e.getMessage());
                }
            });
        });

        // Retrieve Mood History Button.
        buttonRetrieveHistory.setOnClickListener(v -> {
            final String userId = editTextUserId.getText().toString().trim();
            if (TextUtils.isEmpty(userId)) {
                showToast("Enter User ID");
                return;
            }
            firestoreHelper.firebaseToMoodHistory(userId,
                    new FirestoreHelper.FirestoreCallback() {
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

        // Query Moods by Emotion Button.
        buttonQueryEmotional.setOnClickListener(v -> {
            final String userId = editTextUserId.getText().toString().trim();
            final String moodType = editTextMood.getText().toString().trim();
            if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(moodType)) {
                showToast("Enter User ID and Mood Type");
                return;
            }
            firestoreHelper.firebaseQueryEmotional(userId, moodType,
                    new FirestoreHelper.FirestoreCallback() {
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

        // Query Last 7 Days Mood History Button.
        buttonQueryTime.setOnClickListener(v -> {
            final String userId = editTextUserId.getText().toString().trim();
            if (TextUtils.isEmpty(userId)) {
                showToast("Enter User ID");
                return;
            }
            firestoreHelper.firebaseQueryTime(userId, true, false,
                    new FirestoreHelper.FirestoreCallback() {
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

    /**
     * Displays the mood history in the results TextView.
     *
     * @param userHistory The moodHistory object to display.
     */
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

    /**
     * Displays a toast message.
     *
     * @param message The message to show.
     */
    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
