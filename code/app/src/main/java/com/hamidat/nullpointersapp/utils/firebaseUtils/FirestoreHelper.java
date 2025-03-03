package com.hamidat.nullpointersapp.utils.firebaseUtils;

import com.google.firebase.firestore.FirebaseFirestore;
import com.hamidat.nullpointersapp.models.Mood;

/**
 * FirestoreHelper provides a unified interface for interacting with Firestore.
 * It delegates user operations to FirestoreUsers and mood history operations to
 * FirestoreMoodHistory.
 */
public class FirestoreHelper {
    private FirebaseFirestore firestore;
    private FirestoreUsers firestoreUsers;
    private FirestoreMoodHistory firestoreMoodHistory;
    private FirestoreAddEditMoods firestoreAddEditMoods;

    /**
     * Callback interface for Firestore operations.
     */
    public interface FirestoreCallback {
        /**
         * Called when the Firestore operation is successful.
         *
         * @param result The result of the operation.
         */
        void onSuccess(Object result);

        /**
         * Called when the Firestore operation fails.
         *
         * @param e The exception describing the failure.
         */
        void onFailure(Exception e);
    }

    /**
     * Constructor initializes Firestore and its helper classes.
     */
    public FirestoreHelper() {
        this.firestore = FirebaseFirestore.getInstance();
        this.firestoreUsers = new FirestoreUsers(firestore);
        this.firestoreMoodHistory = new FirestoreMoodHistory(firestore);
        this.firestoreAddEditMoods = new FirestoreAddEditMoods(firestore);
    }

    // ======= USER FUNCTIONS =======

    /**
     * Adds a new user with an auto-generated ID.
     *
     * @param userName The username of the new user.
     * @param password The password for the new user.
     * @param callback The callback to receive the result.
     */
    public void addUser(String userName, String password, FirestoreCallback callback) {
        firestoreUsers.addUser(userName, password, callback);
    }

    /**
     * Retrieves a user by their unique ID.
     * On success, returns a Map<String, Object> containing user data (has the fields).
     *
     * @param userID   The unique identifier of the user.
     * @param callback The callback to receive the result.
     */
    public void getUser(String userID, FirestoreCallback callback) {
        firestoreUsers.getUser(userID, callback);
    }

    /**
     * Retrieves a user by their username.
     *
     * @param username The username of the user.
     * @param callback The callback to receive the result.
     */
    public void getUserByUsername(String username, FirestoreCallback callback) {
        firestoreUsers.getUserByUsername(username, callback);
    }

    // ======= ADD/EDIT MOOD FUNCTIONS =======

    /**
     * Saves a mood WITHOUT an image.
     *
     * @param userID   The user identifier.
     * @param mood     The Mood object (without image).
     * @param callback The callback for Firestore operations.
     */
    public void addMood(String userID, Mood mood, FirestoreCallback callback) {
        firestoreAddEditMoods.saveMoodWithoutImage(userID, mood, new FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                callback.onSuccess("Mood saved successfully! 😊 Mood ID: " + result);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(new Exception("Oops! Couldn't save your mood. Try again."));
            }
        });
    }

    /**
     * Saves a mood WITH an image.
     *
     * @param userID   The user identifier.
     * @param mood     The Mood object (with image).
     * @param callback The callback for Firestore operations.
     */
    public void addMoodWithPhoto(String userID, Mood mood, FirestoreCallback callback) {
        firestoreAddEditMoods.saveMoodWithImage(userID, mood, new FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                callback.onSuccess("Mood with photo saved! 🎉 Mood ID: " + result);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(new Exception("Something went wrong while saving your mood with the image. Try again!"));
            }
        });
    }

    // ======= MOOD HISTORY FUNCTIONS =======
    /**
     * Uploads the user's mood history to Firestore.
     *
     * @param userID         The unique identifier of the user.
     * @param userMoodHistory The mood history object.
     * @param callback       The callback to receive the result.
     */
    public void moodHistoryToFirebase(String userID, com.hamidat.nullpointersapp.models.moodHistory userMoodHistory, FirestoreCallback callback) {
        firestoreMoodHistory.moodHistoryToFirebase(userID, userMoodHistory, callback);
    }

    /**
     * Retrieves the mood history for a user from Firestore.
     *
     * @param userID   The unique identifier of the user.
     * @param callback The callback to receive the result.
     */
    public void firebaseToMoodHistory(String userID, FirestoreCallback callback) {
        firestoreMoodHistory.firebaseToMoodHistory(userID, callback);
    }

    /**
     * Queries Firestore for mood history based on mood type.
     *
     * @param userID   The unique identifier of the user.
     * @param moodType The type of mood to query.
     * @param callback The callback to receive the result.
     */
    public void firebaseQueryEmotional(String userID, String moodType, FirestoreCallback callback) {
        firestoreMoodHistory.firebaseQueryEmotional(userID, moodType, callback);
    }

    /**
     * Queries Firestore for mood history within a time range.
     *
     * @param userID    The unique identifier of the user.
     * @param sevenDays true to query the last seven days; false otherwise.
     * @param ascending true for ascending order; false for descending.
     * @param callback  The callback to receive the result.
     */
    public void firebaseQueryTime(String userID, boolean sevenDays, boolean ascending, FirestoreCallback callback) {
        firestoreMoodHistory.firebaseQueryTime(userID, sevenDays, ascending, callback);
    }
}
