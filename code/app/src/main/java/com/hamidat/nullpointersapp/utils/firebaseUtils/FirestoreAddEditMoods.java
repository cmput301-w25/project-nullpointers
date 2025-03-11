package com.hamidat.nullpointersapp.utils.firebaseUtils;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.hamidat.nullpointersapp.models.Mood;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles Firestore operations related to adding and editing moods.
 */
public class FirestoreAddEditMoods {
    private static final String MOODS_COLLECTION = "moods";
    private static final String USERS_COLLECTION = "users";
    private final FirebaseFirestore firestore;

    /**
     * Constructs a new FirestoreAddEditMoods instance.
     *
     * @param firestore The FirebaseFirestore instance.
     */
    public FirestoreAddEditMoods(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Saves a mood WITHOUT an image to Firestore.
     *
     * @param userID   The user identifier.
     * @param mood     The Mood object (without image).
     * @param callback The callback for Firestore operations.
     */
    public void saveMoodWithoutImage(String userID, Mood mood, FirestoreHelper.FirestoreCallback callback) {
        String moodID = UUID.randomUUID().toString();
        DocumentReference moodRef = firestore.collection(MOODS_COLLECTION).document(moodID);

        // Setting more features for the mood, and using the current Date.
        mood.setMoodId(moodID);
        mood.setUserId(userID);
        mood.setTimestamp(new Timestamp(new Date()));


        // Save to Firestore
        moodRef.set(mood)
                .addOnSuccessListener(aVoid -> {
                    updateUserMoodHistory(userID, moodID, callback);
                    callback.onSuccess("Your mood has been recorded successfully! Mood ID: " + moodID);
                })
                .addOnFailureListener(e -> callback.onFailure(new Exception("Oops! Something went wrong while saving your mood. Try again.")));
    }

    /**
     * Saves a mood WITH an image to Firestore.
     *
     * @param userID   The user identifier.
     * @param mood     The Mood object (with image).
     * @param callback The callback for Firestore operations.
     */
    public void saveMoodWithImage(String userID, Mood mood, FirestoreHelper.FirestoreCallback callback) {
        String moodID = UUID.randomUUID().toString();
        DocumentReference moodRef = firestore.collection(MOODS_COLLECTION).document(moodID);

        // Setting more features for the mood, and using the current Date.
        mood.setMoodId(moodID);
        mood.setUserId(userID);
        mood.setTimestamp(new Timestamp(new Date()));

        // Save to Firestore
        moodRef.set(mood)
                .addOnSuccessListener(aVoid -> {
                    updateUserMoodHistory(userID, moodID, callback);
                })
                .addOnFailureListener(e -> callback.onFailure(new Exception("Something went wrong while saving your mood with the image. Try again!")));
    }

    /**
     * Updates the user's mood history with the new mood ID.
     *
     * @param userID   The user identifier.
     * @param moodID   The mood ID to be added.
     * @param callback The callback for Firestore operations.
     */
    private void updateUserMoodHistory(String userID, String moodID, FirestoreHelper.FirestoreCallback callback) {
        DocumentReference userRef = firestore.collection(USERS_COLLECTION).document(userID);

        userRef.update("moodHistory", FieldValue.arrayUnion(moodID))
                .addOnSuccessListener(aVoid -> callback.onSuccess("Your mood history has been updated successfully!"))
                .addOnFailureListener(e -> callback.onFailure(new Exception("Couldn't update your mood history. Please check your connection and try again.")));
    }
}
