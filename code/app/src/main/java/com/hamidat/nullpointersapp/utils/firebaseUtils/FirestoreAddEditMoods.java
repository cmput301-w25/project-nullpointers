/**
 * FirestoreAddEditMoods.java
 * Handles operations for saving and updating Mood objects in Firebase Firestore.
 * Supports moods with and without images, and tracks changes in user mood history.
 *
 * Outstanding Issues: None
 */

package com.hamidat.nullpointersapp.utils.firebaseUtils;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
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
        mood.setEdited(false);  // ensure new mood is not "edited"

        mood.setTimestamp(new Timestamp(new Date()));

        // Save to Firestore
        moodRef.set(mood)
                .addOnSuccessListener(aVoid -> {
                    updateUserMoodHistory(userID, moodID, callback);
                    callback.onSuccess("Your mood has been recorded successfully! Mood ID: " + moodID);
                })
                .addOnFailureListener(e ->
                        callback.onFailure(new Exception("Oops! Something went wrong while saving your mood. Try again."))
                );
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
        mood.setEdited(false);  // ensure new mood is not "edited"
        mood.setTimestamp(new Timestamp(new Date()));

        // Save to Firestore
        moodRef.set(mood)
                .addOnSuccessListener(aVoid -> {
                    updateUserMoodHistory(userID, moodID, callback);
                })
                .addOnFailureListener(e ->
                        callback.onFailure(new Exception("Something went wrong while saving your mood with the image. Try again!"))
                );
    }

    /**
     * Updates an existing Mood document in Firestore.
     * Uses mood.getMoodId() to reference the correct doc and updates fields accordingly.
     *
     * @param mood     The Mood object to update (must have moodId set).
     * @param callback The callback for Firestore operations.
     */
    public void updateMood(Mood mood, FirestoreHelper.FirestoreCallback callback) {
        if (mood.getMoodId() == null || mood.getMoodId().trim().isEmpty()) {
            callback.onFailure(new Exception("No moodId set. Cannot update this mood."));
            return;
        }

        mood.setEdited(true);

        // Reference the existing mood document
        DocumentReference docRef = firestore.collection(MOODS_COLLECTION).document(mood.getMoodId());

        // Build a map of the fields to update
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("mood", mood.getMood());
        updatedData.put("moodDescription", mood.getMoodDescription());
        updatedData.put("latitude", mood.getLatitude());
        updatedData.put("longitude", mood.getLongitude());
        updatedData.put("socialSituation", mood.getSocialSituation());
        updatedData.put("imageBase64", mood.getImageBase64());
        // If you want to to update the timestamp to "now," uncomment:
        // updatedData.put("timestamp", new Timestamp(new Date()));
        updatedData.put("edited", mood.isEdited());

        // Perform the update
        docRef.update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess("Mood updated successfully in Firestore!");
                })
                .addOnFailureListener(e ->
                        callback.onFailure(new Exception("Error updating mood: " + e.getMessage()))
                );
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
                .addOnSuccessListener(aVoid ->
                        callback.onSuccess("Your mood history has been updated successfully!")
                )
                .addOnFailureListener(e ->
                        callback.onFailure(new Exception("Couldn't update your mood history. Please check your connection and try again."))
                );
    }
}