/**
 * FirestoreDeleteMood.java
 * Handles Firestore logic for deleting Mood documents and updating user mood history accordingly.
 *
 * Outstanding Issues: None
 */

package com.hamidat.nullpointersapp.utils.firebaseUtils;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.hamidat.nullpointersapp.models.Mood;

/**
 * Handles Firestore operations related to deleting moods.
 *
 * <p>This class provides a method to delete a mood event from Firestore and update the user's
 * mood history by removing the mood's ID.</p>
 */
public class FirestoreDeleteMood {

    // Firestore collection names.
    private static final String MOODS_COLLECTION = "moods";
    private static final String USERS_COLLECTION = "users";

    // Firestore instance.
    private final FirebaseFirestore firestore;

    /**
     * Constructs a new FirestoreDeleteMood instance.
     *
     * @param firestore The FirebaseFirestore instance.
     */
    public FirestoreDeleteMood(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Deletes the specified mood from Firestore and updates the user's mood history.
     *
     * @param userID   The identifier for the user.
     * @param mood     The Mood object to be deleted.
     * @param callback The callback for Firestore operations.
     * @throws IllegalArgumentException if the mood or its moodId is null.
     */
    public void deleteMood(String userID, Mood mood, FirestoreHelper.FirestoreCallback callback) {
        if (mood == null || mood.getMoodId() == null) {
            throw new IllegalArgumentException("Mood and its moodId must not be null");
        }

        DocumentReference moodRef = firestore.collection(MOODS_COLLECTION)
                .document(mood.getMoodId());

        // Delete the mood document.
        moodRef.delete()
                .addOnSuccessListener(aVoid -> {
                    updateUserMoodHistoryForDeletion(userID, mood.getMoodId(), new FirestoreHelper.FirestoreCallback() {
                        /**
                         * Called when the user's mood history is successfully updated.
                         *
                         * @param result The result of the operation.
                         */
                        @Override
                        public void onSuccess(Object result) {
                            callback.onSuccess("Mood deletion complete. Mood ID: " + mood.getMoodId());
                        }

                        /**
                         * Called when the user's mood history update fails.
                         *
                         * @param e The exception that occurred during the operation.
                         */
                        @Override
                        public void onFailure(Exception e) {
                            callback.onFailure(e);
                        }
                    });
                })
                .addOnFailureListener(e ->
                        callback.onFailure(new Exception("Error deleting mood. Please try again."))
                );
    }



    /**
     * Updates the user's mood history by removing the deleted mood's ID.
     *
     * @param userID   The identifier for the user.
     * @param moodID   The mood ID to remove.
     * @param callback The callback for Firestore operations.
     */
    private void updateUserMoodHistoryForDeletion(String userID, String moodID,
                                                  FirestoreHelper.FirestoreCallback callback) {
        DocumentReference userRef = firestore.collection(USERS_COLLECTION).document(userID);
        userRef.update("moodHistory", FieldValue.arrayRemove(moodID))
                .addOnSuccessListener(aVoid ->
                        callback.onSuccess("User mood history updated successfully!")
                )
                .addOnFailureListener(e ->
                        callback.onFailure(new Exception("Failed to update mood history."))
                );
    }
}
