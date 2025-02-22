package com.hamidat.nullpointersapp.utils.firebaseUtils;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.Timestamp;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.models.moodHistory;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides methods for interacting with Firebase Firestore to manage a user's mood history.
 * This class encapsulates operations such as adding mood history data to Firebase,
 * querying the mood history, and applying various filters and sorting orders.
 * It integrates with the com.hamidat.nullpointersapp.moodClasses.Mood and
 * com.hamidat.nullpointersapp.moodClasses.moodHistory classes to manage mood data.
 *
 */
public class firestoreMoodHistory {
    protected FirebaseFirestore firestore;

    /**
     * Constructs a firestoreMoodHistory instance with the provided FirebaseFirestore reference.
     *
     * @param firestore_ref the FirebaseFirestore instance to interact with Firestore.
     */
    public firestoreMoodHistory(FirebaseFirestore firestore_ref) {
        this.firestore = firestore_ref;
    }

    /**
     * Callback interface for asynchronous mood history operations.
     */
    public interface MoodHistoryCallback {
        /**
         * Called when the mood history is successfully retrieved.
         *
         * @param userHistory the moodHistory object containing the retrieved mood entries.
         */
        void onSuccess(moodHistory userHistory);

        /**
         * Called when there is an error retrieving the mood history.
         *
         * @param e the exception that occurred.
         */
        void onFailure(Exception e);
    }

    /**
     * Adds all mood entries from the given moodHistory to Firebase.
     * This method iterates over each Mood in the provided moodHistory and adds it
     * to the moodHistory collection for the specified user document.
     *
     * @param userID          the unique identifier of the user.
     * @param userMoodHistory the moodHistory object containing the mood entries to add.
     */
    public void moodHistoryToFirebase(String userID, moodHistory userMoodHistory) {
//        moodHistoryToFirebase is a function that adds all values from a moodHistory to firebase, this is mostly for test adding data.
//        Args: userID, moodHistory userMoodHistory (current users moods).

        CollectionReference moodReference = this.firestore.collection("Users").document(userID).collection("moodHistory");
//      Check database to see if this works
        for (Mood mood : userMoodHistory.getMoodArray()) {
            moodReference.add(mood);
        }
    }


//  ALL QUERYING RELATED FUNCTIONS
    /**
     * Attaches a snapshot listener to the given query to listen for real-time updates from Firestore.
     * The listener converts each document in the query snapshot to a link Mood object and adds it to
     * a new moodHistory instance, which is then passed to the callback on success.
     *
     * @param query    the Firestore query to attach the snapshot listener to.
     * @param userID   the unique identifier of the user.
     * @param callback the callback to be invoked upon success or failure.
     */
    public void attachSnapshotListener(Query query, String userID, MoodHistoryCallback callback) {

        moodHistory filteredMoodHistory = new moodHistory();
        filteredMoodHistory.setUserID(userID);

        query.addSnapshotListener((moodSnapshot, error) -> {
            if (error != null) {
                callback.onFailure(error);
                return;
            }
            if (moodSnapshot != null) {
                for(QueryDocumentSnapshot doc : moodSnapshot) {
                    Mood mood = doc.toObject(Mood.class);
                    filteredMoodHistory.addMood(mood);
                }
                callback.onSuccess(filteredMoodHistory);
            }
        });
    }

    /**
     * Applies an ordering to the provided query for the timestamp field.
     * If the ascending parameter is true, the query is ordered in ascending order;
     * otherwise, it is ordered in descending order.
     *
     * @param query     the Firestore query to order.
     * @param ascending if true, orders in ascending order; if false, in descending order.
     * @return the query with the applied ordering.
     */
    public Query toggleOrder(Query query, boolean ascending) {
        Log.d("FilterTest", "toggleOrder Executed");
        if (ascending) {
            query = query.orderBy("timestamp", Query.Direction.ASCENDING);
            Log.d("FilterTest", "IF STATEMENT EXECUTED");
        } else {
            query = query.orderBy("timestamp", Query.Direction.DESCENDING);
            Log.d("FilterTest", "ELSE EXECUTED");
        }

        return query;
    }

    /**
     * Retrieves the entire moodHistory collection for a specified user from Firestore.
     * This method attaches a snapshot listener to the user's moodHistory collection, returning
     * the results via the provided callback.
     *
     * @param userID   the unique identifier of the user.
     * @param callback the callback to handle the retrieved mood history or errors.
     */
    public void firebaseToMoodHistory(String userID, MoodHistoryCallback callback) {

//      Retrieving the moodHistory Reference from firestore
        CollectionReference moodHistoryRef = firestore.collection("Users").document(userID).collection("moodHistory");
        attachSnapshotListener(moodHistoryRef, userID, callback);
    }

    /**
     * Queries the moodHistory collection for entries matching a specific emotional state.
     * The query filters mood entries where the "mood" field equals the provided moodType.
     * The results are returned in real time via the callback.
     *
     * @param userID   the unique identifier of the user.
     * @param moodType the emotional state to filter by (e.g., "Happy", "Sad").
     * @param callback the callback to handle the query results or errors.
     */
    public void firebaseQueryEmotional(String userID, String moodType, MoodHistoryCallback callback) {

        CollectionReference moodHistoryRef = firestore.collection("Users").document(userID).collection("moodHistory");
        Query queryMoodType = moodHistoryRef.whereEqualTo("mood", moodType);

        attachSnapshotListener(queryMoodType, userID, callback);

    }

    /**
     * Queries the moodHistory collection based on time constraints and orders the results.
     * If sevenDays is true, the query filters mood entries to those within the past week.
     * Additionally, the query is ordered by the "timestamp" field in ascending or descending order based
     * on the ascending parameter.
     *
     * @param userID    the unique identifier of the user.
     * @param sevenDays if true, filters mood entries to only those from the most recent week.
     * @param ascending if true, orders the results in ascending order; otherwise, in descending order.
     * @param callback  the callback to handle the query results or errors.
     */
    public void firebaseQueryTime(String userID, Boolean sevenDays, Boolean ascending, MoodHistoryCallback callback) {

        CollectionReference moodHistoryRef = firestore.collection("Users").document(userID).collection("moodHistory");

        Calendar calendar = Calendar.getInstance();
        Date now = new Date();
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        Date oneWeekAgo = calendar.getTime();

        Timestamp nowTimestamp = new Timestamp(now);
        Timestamp oneWeekAgoTimestamp = new Timestamp(oneWeekAgo);

        // Query within 7 days and no data can have a date higher than present date.
        Query queryTime = moodHistoryRef;

        if (sevenDays) {
            queryTime = moodHistoryRef.whereGreaterThanOrEqualTo("timestamp", oneWeekAgoTimestamp).whereLessThanOrEqualTo("timestamp", nowTimestamp);
        }
        // Function for toggling ascending or descending.
        queryTime = toggleOrder(queryTime, ascending);

        attachSnapshotListener(queryTime, userID, callback);
    }

    public void addUser(moodHistory user) {
        // Note: expecting user to already exist so this function will not be used
        Map<String, Object> userFields = new HashMap<>();
        userFields.put("userName", user.getUserName());
        firestore.collection("Users").add(userFields);
    }


}

