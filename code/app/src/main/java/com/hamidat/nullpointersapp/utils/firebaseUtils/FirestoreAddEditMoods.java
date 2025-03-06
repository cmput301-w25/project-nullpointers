package com.hamidat.nullpointersapp.utils.firebaseUtils;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.hamidat.nullpointersapp.models.Mood;

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

        // Prepare mood data
        Map<String, Object> moodData = new HashMap<>();
        moodData.put("moodId", moodID);
        moodData.put("userId", userID);
        moodData.put("mood", mood.getMood());
        moodData.put("moodDescription", mood.getMoodDescription());
        moodData.put("latitude", mood.getLatitude());
        moodData.put("longitude", mood.getLongitude());
        moodData.put("socialSituation", mood.getSocialSituation());
        moodData.put("timestamp", FieldValue.serverTimestamp());

        // Save to Firestore
        moodRef.set(moodData)
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

        // Prepare mood data
        Map<String, Object> moodData = new HashMap<>();
        moodData.put("moodId", moodID);
        moodData.put("userId", userID);
        moodData.put("latitude", mood.getLatitude());
        moodData.put("longitude", mood.getLongitude());
        moodData.put("socialSituation", mood.getSocialSituation());
        moodData.put("mood", mood.getMood());
        moodData.put("moodDescription", mood.getMoodDescription());
        moodData.put("imageBase64", mood.getImageBase64()); // Store the image
        moodData.put("timestamp", FieldValue.serverTimestamp());

        // Save to Firestore
        moodRef.set(moodData)
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
