package com.hamidat.nullpointersapp.utils.firebaseUtils;

import android.util.Log;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.models.moodHistory;
import java.util.Date;
import java.util.Calendar;
import java.util.ArrayList;

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
     * @param userID   The user identifier (optional, can be null for multi-user queries).
     * @param callback The callback to receive the results.
     */
    private void attachSnapshotListener(Query query, String userID, FirestoreHelper.FirestoreCallback callback) {
        query.addSnapshotListener((value, error) -> {
            if (error != null) {
                callback.onFailure(error);
                return;
            }
            moodHistory filteredMoodHistory = new moodHistory();
            if (userID != null) {
                filteredMoodHistory.setUserID(userID);
            }
            if (value != null && !value.isEmpty()) {
                for (QueryDocumentSnapshot doc : value) {
                    try {
                        Mood mood = doc.toObject(Mood.class);
                        mood.setMoodId(doc.getId()); //NO NULLs
                        // Manually set coordinates from document
                        Double lat = doc.getDouble("latitude");
                        Double lng = doc.getDouble("longitude");
                        if (lat != null && lng != null) {
                            mood.setLatitude(lat);
                            mood.setLongitude(lng);
                        }

                        // Ensure timestamp is set
                        Timestamp timestamp = doc.getTimestamp("timestamp");
                        if (timestamp != null) {
                            mood.setTimestamp(timestamp);
                        }

                        filteredMoodHistory.addMood(mood);
                    } catch (Exception e) {
                        Log.e("Firestore", "Error parsing mood document", e);
                    }
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
     * Retrieves the mood history for multiple users from the "moods" collection.
     *
     * @param userIds  An ArrayList of user IDs to query.
     * @param callback The callback to receive the mood history.
     */
    public void firebaseToMoodHistory(ArrayList<String> userIds, FirestoreHelper.FirestoreCallback callback) {
        CollectionReference moodsRef = firestore.collection(MOODS_COLLECTION);
        // Note: whereIn supports up to 10 items; for more, you may need to split the query.
        Query query = moodsRef.whereIn("userId", userIds);
        // We pass null for the userID since we have multiple users.
        attachSnapshotListener(query, null, callback);
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
                .whereEqualTo("mood", moodType);
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

            // Convert dates to Timestamps if needed.
            // (Assuming mood.timestamp is stored as a Firebase Timestamp.)
            query = query.whereGreaterThanOrEqualTo("timestamp", new com.google.firebase.Timestamp(oneWeekAgo))
                    .whereLessThanOrEqualTo("timestamp", new com.google.firebase.Timestamp(now));
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
