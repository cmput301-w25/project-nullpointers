package com.hamidat.nullpointersapp.utils.firebaseUtils;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.models.moodHistory;

import java.util.Calendar;
import java.util.Date;

/**
 * Handles Firestore operations related to mood history.
 */
public class FirestoreMoodHistory {
    private static final String USERS_COLLECTION = "users";
    private static final String MOOD_HISTORY_COLLECTION = "MoodHistory";

    private final FirebaseFirestore firestore;

    /**
     * Constructs a new FirestoreMoodHistory instance.
     *
     * @param firestore The FirebaseFirestore instance.
     */
    public FirestoreMoodHistory(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Adds all mood entries from the provided mood history to Firestore.
     *
     * @param userID         The user identifier.
     * @param userMoodHistory The moodHistory object containing mood entries.
     * @param callback       The callback for Firestore operations.
     */
    public void moodHistoryToFirebase(String userID, moodHistory userMoodHistory, FirestoreHelper.FirestoreCallback callback) {
        CollectionReference moodReference = firestore.collection(USERS_COLLECTION)
                .document(userID)
                .collection(MOOD_HISTORY_COLLECTION);

        // Loop through each mood in the moodHistory object and add it.
        for (Mood mood : userMoodHistory.getMoodArray()) {
            moodReference.add(mood)
                    .addOnSuccessListener(documentReference -> callback.onSuccess(
                            "Mood added with ID: " + documentReference.getId()))
                    .addOnFailureListener(callback::onFailure);
        }
    }

    /**
     * Attaches a snapshot listener to a query for real-time mood history updates.
     *
     * @param query    The Firestore query.
     * @param userID   The user identifier.
     * @param callback The callback to receive the results.
     */
    private void attachSnapshotListener(Query query, String userID, FirestoreHelper.FirestoreCallback callback) {
        moodHistory filteredMoodHistory = new moodHistory();
        filteredMoodHistory.setUserID(userID);

        query.addSnapshotListener((moodSnapshot, error) -> {
            if (error != null) {
                callback.onFailure(error);
                return;
            }
            if (moodSnapshot != null) {
                for (QueryDocumentSnapshot doc : moodSnapshot) {
                    Mood mood = doc.toObject(Mood.class);
                    filteredMoodHistory.addMood(mood);
                }
                callback.onSuccess(filteredMoodHistory);
            }
        });
    }

    /**
     * Retrieves the entire mood history for a given user.
     *
     * @param userID   The user identifier.
     * @param callback The callback to receive the mood history.
     */
    public void firebaseToMoodHistory(String userID, FirestoreHelper.FirestoreCallback callback) {
        CollectionReference moodHistoryRef = firestore.collection(USERS_COLLECTION)
                .document(userID)
                .collection(MOOD_HISTORY_COLLECTION);
        attachSnapshotListener(moodHistoryRef, userID, callback);
    }

    /**
     * Queries mood history by a specific emotion type.
     *
     * @param userID   The user identifier.
     * @param moodType The mood type to filter by.
     * @param callback The callback to receive the results.
     */
    public void firebaseQueryEmotional(String userID, String moodType, FirestoreHelper.FirestoreCallback callback) {
        CollectionReference moodHistoryRef = firestore.collection(USERS_COLLECTION)
                .document(userID)
                .collection(MOOD_HISTORY_COLLECTION);
        Query query = moodHistoryRef.whereEqualTo("moodEmotion", moodType);
        attachSnapshotListener(query, userID, callback);
    }

    /**
     * Queries mood history for entries in the last 7 days and orders the results.
     *
     * @param userID    The user identifier.
     * @param sevenDays true to query the last seven days; false otherwise.
     * @param ascending true for ascending order; false for descending.
     * @param callback  The callback to receive the results.
     */
    public void firebaseQueryTime(String userID, boolean sevenDays, boolean ascending,
                                  FirestoreHelper.FirestoreCallback callback) {
        CollectionReference moodHistoryRef = firestore.collection(USERS_COLLECTION)
                .document(userID)
                .collection(MOOD_HISTORY_COLLECTION);

        Calendar calendar = Calendar.getInstance();
        Date now = new Date();
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        Date oneWeekAgo = calendar.getTime();

        Timestamp nowTimestamp = new Timestamp(now);
        Timestamp oneWeekAgoTimestamp = new Timestamp(oneWeekAgo);

        Query query = moodHistoryRef;
        if (sevenDays) {
            query = moodHistoryRef.whereGreaterThanOrEqualTo("moodTimestamp", oneWeekAgoTimestamp)
                    .whereLessThanOrEqualTo("moodTimestamp", nowTimestamp);
        }
        query = toggleOrder(query, ascending);

        attachSnapshotListener(query, userID, callback);
    }

    /**
     * Orders the query results by moodTimestamp.
     *
     * @param query     The Firestore query.
     * @param ascending true for ascending order; false for descending.
     * @return The ordered query.
     */
    private Query toggleOrder(Query query, boolean ascending) {
        if (ascending) {
            return query.orderBy("moodTimestamp", Query.Direction.ASCENDING);
        } else {
            return query.orderBy("moodTimestamp", Query.Direction.DESCENDING);
        }
    }
}
