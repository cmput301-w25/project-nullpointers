package com.hamidat.nullpointersapp.utils.firebaseUtils;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * FirestoreHelper provides a unified interface for interacting with Firestore.
 * It delegates user operations to FirestoreUsers and mood history operations to FirestoreMoodHistory.
 */
public class FirestoreHelper {
    private FirebaseFirestore firestore;
    private FirestoreUsers firestoreUsers;
    private FirestoreMoodHistory firestoreMoodHistory;

    /**
     * Callback interface for Firestore operations.
     */
    public interface FirestoreCallback {
        void onSuccess(Object result);
        void onFailure(Exception e);
    }

    public FirestoreHelper() {
        this.firestore = FirebaseFirestore.getInstance();
        this.firestoreUsers = new FirestoreUsers(firestore);
        this.firestoreMoodHistory = new FirestoreMoodHistory(firestore);
    }

    // ======= USER FUNCTIONS =======

    public void addUser(String userID, String userName, FirestoreCallback callback) {
        firestoreUsers.addUser(userID, userName, callback);
    }

    public void getUser(String userID, FirestoreCallback callback) {
        firestoreUsers.getUser(userID, callback);
    }

    public void getUserByUsername(String username, FirestoreCallback callback) {
        firestoreUsers.getUserByUsername(username, callback);
    }

    // ======= MOOD HISTORY FUNCTIONS =======

    public void moodHistoryToFirebase(String userID, com.hamidat.nullpointersapp.models.moodHistory userMoodHistory, FirestoreCallback callback) {
        firestoreMoodHistory.moodHistoryToFirebase(userID, userMoodHistory, callback);
    }

    public void firebaseToMoodHistory(String userID, FirestoreCallback callback) {
        firestoreMoodHistory.firebaseToMoodHistory(userID, callback);
    }

    public void firebaseQueryEmotional(String userID, String moodType, FirestoreCallback callback) {
        firestoreMoodHistory.firebaseQueryEmotional(userID, moodType, callback);
    }

    public void firebaseQueryTime(String userID, boolean sevenDays, boolean ascending, FirestoreCallback callback) {
        firestoreMoodHistory.firebaseQueryTime(userID, sevenDays, ascending, callback);
    }
}
