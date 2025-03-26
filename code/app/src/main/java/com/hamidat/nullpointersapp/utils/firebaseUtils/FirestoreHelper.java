package com.hamidat.nullpointersapp.utils.firebaseUtils;

import com.google.firebase.firestore.FirebaseFirestore;
import com.hamidat.nullpointersapp.models.Mood;

import java.util.ArrayList;
import java.util.Map;

/**
 * FirestoreHelper provides a unified interface for interacting with Firestore.
 * It delegates user operations to FirestoreUsers and mood history operations to
 * FirestoreMoodHistory.
 */
public class FirestoreHelper {
    private FirebaseFirestore firestore;
    private FirestoreUsers firestoreUsers;
    private FirestoreMoodHistory firestoreMoodHistory;
    private FirestoreAddEditMoods firestoreAddEditMoods;
    private FirestoreFollowing firestoreFollowing;

    /**
     * Callback interface for Firestore operations.
     */
    public interface FirestoreCallback {
        /**
         * Called when the Firestore operation is successful.
         *
         * @param result The result of the operation.
         */
        void onSuccess(Object result);

        /**
         * Called when the Firestore operation fails.
         *
         * @param e The exception describing the failure.
         */
        void onFailure(Exception e);
    }

    /**
     * Constructor initializes Firestore and its helper classes.
     */
    public FirestoreHelper() {
        this.firestore = FirebaseFirestore.getInstance();
        this.firestoreUsers = new FirestoreUsers(firestore);
        this.firestoreMoodHistory = new FirestoreMoodHistory(firestore);
        this.firestoreAddEditMoods = new FirestoreAddEditMoods(firestore);
        this.firestoreFollowing = new FirestoreFollowing(firestore);
    }

    // ======= USER FUNCTIONS =======

    public void addUser(String userName, String password, FirestoreCallback callback) {
        firestoreUsers.addUser(userName, password, callback);
    }

    public void getUser(String userID, FirestoreCallback callback) {
        firestoreUsers.getUser(userID, callback);
    }

    public void getUserByUsername(String username, FirestoreCallback callback) {
        firestore.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // User exists → not unique
                        var doc = querySnapshot.getDocuments().get(0);
                        Map<String, Object> userData = doc.getData();
                        userData.put("userId", doc.getId());
                        callback.onSuccess(userData);
                    } else {
                        // Username is unique
                        callback.onFailure(new Exception("Username not found"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    // ======= ADD/EDIT MOOD FUNCTIONS =======

    public void addMood(String userID, Mood mood, FirestoreCallback callback) {
        firestoreAddEditMoods.saveMoodWithoutImage(userID, mood, new FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                callback.onSuccess("Mood saved successfully! 😊 Mood ID: " + result);
            }
            @Override
            public void onFailure(Exception e) {
                callback.onFailure(new Exception("Oops! Couldn't save your mood. Try again."));
            }
        });
    }

    public void addMoodWithPhoto(String userID, Mood mood, FirestoreCallback callback) {
        firestoreAddEditMoods.saveMoodWithImage(userID, mood, new FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                callback.onSuccess("Mood with photo saved! 🎉 Mood ID: " + result);
            }
            @Override
            public void onFailure(Exception e) {
                callback.onFailure(new Exception("Something went wrong while saving your mood with the image. Try again!"));
            }
        });
    }

    /**
     * Updates an existing Mood in Firestore based on mood.getMoodId().
     * Make sure your FirestoreAddEditMoods class has a matching updateMood(...) method.
     *
     * @param mood     The Mood object to update (must have moodId set).
     * @param callback Callback for success/failure.
     */
    public void updateMood(Mood mood, FirestoreCallback callback) {
        firestoreAddEditMoods.updateMood(mood, new FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                callback.onSuccess("Mood updated successfully! " + result);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(new Exception("Something went wrong while updating your mood. Try again!"));
            }
        });
    }

    // ======= MOOD HISTORY FUNCTIONS =======
    public void firebaseToMoodHistory(String userID, FirestoreCallback callback) {
        firestoreMoodHistory.firebaseToMoodHistory(userID, callback);
    }

    // New method: fetch mood history for multiple users.
    public void firebaseToMoodHistory(java.util.ArrayList<String> userIds, FirestoreCallback callback) {
        firestoreMoodHistory.firebaseToMoodHistory(userIds, callback);
    }

    public void firebaseQueryEmotional(String userID, String moodType, FirestoreCallback callback) {
        firestoreMoodHistory.firebaseQueryEmotional(userID, moodType, callback);
    }

    public void firebaseQueryTime(String userID, boolean sevenDays, boolean ascending, FirestoreCallback callback) {
        firestoreMoodHistory.firebaseQueryTime(userID, sevenDays, ascending, callback);
    }


    // ======= FOLLOWING FUNCTIONS =======
    public void sendFriendRequest(String fromUserId, String toUserId, FirestoreFollowing.FollowingCallback callback) {
        firestoreFollowing.sendFriendRequest(fromUserId, toUserId, callback);
    }

    public void acceptFriendRequest(String requestId, FirestoreFollowing.FollowingCallback callback) {
        firestoreFollowing.acceptFriendRequest(requestId, callback);
    }

    public void declineFriendRequest(String requestId, FirestoreFollowing.FollowingCallback callback) {
        firestoreFollowing.declineFriendRequest(requestId, callback);
    }

    public void removeFollowing(String userId, String unfollowUserId, FirestoreFollowing.FollowingCallback callback) {
        firestoreFollowing.removeFollowing(userId, unfollowUserId, callback);
    }

    public void listenForFriendRequests(String currentUserId, FirestoreFollowing.FollowingCallback callback) {
        firestoreFollowing.listenForFriendRequests(currentUserId, callback);
    }

    public void getAllUsers(FirestoreCallback callback) {
        firestoreUsers.getAllUsers(callback);
    }

    public void updateUserProfilePicture(String userId, String base64Image, FirestoreCallback callback) {
        firestoreUsers.updateUserProfilePicture(userId, base64Image, callback);
    }


}
