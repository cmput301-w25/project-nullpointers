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
 * FirestoreMoodHistory handles Firestore operations related to mood history.
 */
public class FirestoreMoodHistory {
    private static final String USERS_COLLECTION = "users";
    private static final String MOOD_HISTORY_COLLECTION = "MoodHistory";
    private FirebaseFirestore firestore;

    public FirestoreMoodHistory(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Adds all mood entries from the provided moodHistory to Firestore.
     */
    public void moodHistoryToFirebase(String userID, moodHistory userMoodHistory, FirestoreHelper.FirestoreCallback callback) {
        CollectionReference moodReference = firestore.collection(USERS_COLLECTION)
                .document(userID)
                .collection(MOOD_HISTORY_COLLECTION);

        // Loop through each mood in the moodHistory object and add it.
        for (Mood mood : userMoodHistory.getMoodArray()) {
            moodReference.add(mood)
                    .addOnSuccessListener(documentReference -> callback.onSuccess("Mood added with ID: " + documentReference.getId()))
                    .addOnFailureListener(callback::onFailure);
        }
    }

    /**
     * Attaches a snapshot listener to a query for real-time mood history updates.
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
     */
    public void firebaseToMoodHistory(String userID, FirestoreHelper.FirestoreCallback callback) {
        CollectionReference moodHistoryRef = firestore.collection(USERS_COLLECTION)
                .document(userID)
                .collection(MOOD_HISTORY_COLLECTION);
        attachSnapshotListener(moodHistoryRef, userID, callback);
    }

    /**
     * Queries mood history by a specific emotion type.
     */
    public void firebaseQueryEmotional(String userID, String moodType, FirestoreHelper.FirestoreCallback callback) {
        CollectionReference moodHistoryRef = firestore.collection(USERS_COLLECTION)
                .document(userID)
                .collection(MOOD_HISTORY_COLLECTION);
        // Assuming moodEmotion is the field that holds the mood type.
        Query query = moodHistoryRef.whereEqualTo("moodEmotion", moodType);
        attachSnapshotListener(query, userID, callback);
    }

    /**
     * Queries mood history for entries in the last 7 days and orders the results.
     */
    public void firebaseQueryTime(String userID, boolean sevenDays, boolean ascending, FirestoreHelper.FirestoreCallback callback) {
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
     */
    private Query toggleOrder(Query query, boolean ascending) {
        if (ascending) {
            return query.orderBy("moodTimestamp", Query.Direction.ASCENDING);
        } else {
            return query.orderBy("moodTimestamp", Query.Direction.DESCENDING);
        }
    }
}
