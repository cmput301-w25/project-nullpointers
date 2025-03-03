package com.hamidat.nullpointersapp.utils.firebaseUtils;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.models.moodHistory;

import java.util.Date;
import java.util.Calendar;

/**
 * Handles Firestore operations related to retrieving mood history.
 * Moods are stored in the "moods" collection (each mood document includes a "userId" field).
 * The user's moodHistory (stored in their "users" document) just keeps an array of mood IDs.
 */
public class FirestoreMoodHistory {
    private static final String MOODS_COLLECTION = "moods";
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
     * Attaches a snapshot listener to the provided query for real-time mood history updates.
     *
     * @param query    The Firestore query.
     * @param userID   The user identifier.
     * @param callback The callback to receive the results.
     */
    private void attachSnapshotListener(Query query, String userID, FirestoreHelper.FirestoreCallback callback) {
        // Create an empty moodHistory object to fill with the moods
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
     * Retrieves the entire mood history for a given user from the "moods" collection.
     *
     * @param userID   The user identifier.
     * @param callback The callback to receive the mood history.
     */
    public void firebaseToMoodHistory(String userID, FirestoreHelper.FirestoreCallback callback) {
        CollectionReference moodsRef = firestore.collection(MOODS_COLLECTION);
        Query query = moodsRef.whereEqualTo("userId", userID);
        attachSnapshotListener(query, userID, callback);
    }

    /**
     * Queries the mood history for a specific emotion type.
     *
     * @param userID   The user identifier.
     * @param moodType The mood type to filter by.
     * @param callback The callback to receive the results.
     */
    public void firebaseQueryEmotional(String userID, String moodType, FirestoreHelper.FirestoreCallback callback) {
        CollectionReference moodsRef = firestore.collection(MOODS_COLLECTION);
        Query query = moodsRef.whereEqualTo("userId", userID)
                .whereEqualTo("moodEmotion", moodType);
        attachSnapshotListener(query, userID, callback);
    }

    /**
     * Queries the mood history for entries within the last 7 days and orders the results.
     *
     * @param userID    The user identifier.
     * @param sevenDays true to query the last seven days; false otherwise.
     * @param ascending true for ascending order; false for descending.
     * @param callback  The callback to receive the results.
     */
    public void firebaseQueryTime(String userID, boolean sevenDays, boolean ascending,
                                  FirestoreHelper.FirestoreCallback callback) {
        CollectionReference moodsRef = firestore.collection(MOODS_COLLECTION);
        Query query = moodsRef.whereEqualTo("userId", userID);

        if (sevenDays) {
            Calendar calendar = Calendar.getInstance();
            Date now = new Date();
            calendar.add(Calendar.DAY_OF_YEAR, -7);
            Date oneWeekAgo = calendar.getTime();

            Timestamp nowTimestamp = new Timestamp(now);
            Timestamp oneWeekAgoTimestamp = new Timestamp(oneWeekAgo);

            query = query.whereGreaterThanOrEqualTo("timestamp", oneWeekAgoTimestamp)
                    .whereLessThanOrEqualTo("timestamp", nowTimestamp);
        }
        query = toggleOrder(query, ascending);
        attachSnapshotListener(query, userID, callback);
    }

    /**
     * Orders the query results by the "timestamp" field.
     *
     * @param query     The Firestore query.
     * @param ascending true for ascending order; false for descending.
     * @return The ordered query.
     */
    private Query toggleOrder(Query query, boolean ascending) {
        if (ascending) {
            return query.orderBy("timestamp", Query.Direction.ASCENDING);
        } else {
            return query.orderBy("timestamp", Query.Direction.DESCENDING);
        }
    }
}
