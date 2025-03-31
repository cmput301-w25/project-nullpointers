/**
 * FirestoreHelper.java
 * Provides a unified interface for interacting with Firestore,
 * delegating user, mood, and following-related operations to their respective helper classes.
 *
 * Outstanding Issues: None
 */

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

    /**
     * Adds a new user to Firestore.
     *
     * @param userName The username of the new user.
     * @param password The password of the new user.
     * @param callback The callback to handle success or failure.
     */
    public void addUser(String userName, String password, FirestoreCallback callback) {
        firestoreUsers.addUser(userName, password, callback);
    }

    /**
     * Retrieves user data from Firestore.
     *
     * @param userID The ID of the user to retrieve.
     * @param callback The callback to handle success or failure.
     */
    public void getUser(String userID, FirestoreCallback callback) {
        firestoreUsers.getUser(userID, callback);
    }

    /**
     * Retrieves user data from Firestore based on username.
     *
     * @param username The username of the user to retrieve.
     * @param callback The callback to handle success or failure.
     */
    public void getUserByUsername(String username, FirestoreCallback callback) {
        firestore.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    /**
                     * Called when the query is successful.
                     * Handles both user found and user not found scenarios.
                     */
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

    /**
     * Adds a new mood to Firestore without an image.
     *
     * @param userID The ID of the user adding the mood.
     * @param mood The mood object to add.
     * @param callback The callback to handle success or failure.
     */
    public void addMood(String userID, Mood mood, FirestoreCallback callback) {
        firestoreAddEditMoods.saveMoodWithoutImage(userID, mood, new FirestoreCallback() {
            /**
             * Called when the mood is successfully saved.
             *
             * @param result The ID of the saved mood.
             */
            @Override
            public void onSuccess(Object result) {
                callback.onSuccess("Mood saved successfully! 😊 Mood ID: " + result);
            }

            /**
             * Called when saving the mood fails.
             *
             * @param e The exception that occurred during the save operation.
             */
            @Override
            public void onFailure(Exception e) {
                callback.onFailure(new Exception("Oops! Couldn't save your mood. Try again."));
            }
        });
    }

    /**
     * Adds a new mood to Firestore with an image.
     *
     * @param userID The ID of the user adding the mood.
     * @param mood The mood object to add.
     * @param callback The callback to handle success or failure.
     */
    public void addMoodWithPhoto(String userID, Mood mood, FirestoreCallback callback) {
        firestoreAddEditMoods.saveMoodWithImage(userID, mood, new FirestoreCallback() {
            /**
             * Called when the mood with photo is successfully saved.
             *
             * @param result The ID of the saved mood.
             */
            @Override
            public void onSuccess(Object result) {
                callback.onSuccess("Mood with photo saved! 🎉 Mood ID: " + result);
            }

            /**
             * Called when saving the mood with photo fails.
             *
             * @param e The exception that occurred during the save operation.
             */
            @Override
            public void onFailure(Exception e) {
                callback.onFailure(new Exception("Something went wrong while saving your mood with the image. Try again!"));
            }
        });
    }

    /**
     * Updates an existing Mood in Firestore based on mood.getMoodId().
     *
     * @param mood     The Mood object to update (must have moodId set).
     * @param callback Callback for success/failure.
     */
    public void updateMood(Mood mood, FirestoreCallback callback) {
        firestoreAddEditMoods.updateMood(mood, new FirestoreCallback() {
            /**
             * Called when the mood is successfully updated.
             *
             * @param result The result of the update operation.
             */
            @Override
            public void onSuccess(Object result) {
                callback.onSuccess("Mood updated successfully! " + result);
            }

            /**
             * Called when updating the mood fails.
             *
             * @param e The exception that occurred during the update operation.
             */
            @Override
            public void onFailure(Exception e) {
                callback.onFailure(new Exception("Something went wrong while updating your mood. Try again!"));
            }
        });
    }

    // ======= MOOD HISTORY FUNCTIONS =======

    /**
     * Fetches mood history from Firestore for a single user.
     *
     * @param userID The ID of the user whose mood history to fetch.
     * @param callback The callback to handle success or failure.
     */
    public void firebaseToMoodHistory(String userID, FirestoreCallback callback) {
        firestoreMoodHistory.firebaseToMoodHistory(userID, callback);
    }

    /**
     * Fetches mood history from Firestore for multiple users.
     *
     * @param userIds The list of user IDs whose mood history to fetch.
     * @param callback The callback to handle success or failure.
     */
    public void firebaseToMoodHistory(java.util.ArrayList<String> userIds, FirestoreCallback callback) {
        firestoreMoodHistory.firebaseToMoodHistory(userIds, callback);
    }

    /**
     * Queries mood history based on emotional type.
     *
     * @param userID The ID of the user whose mood history to query.
     * @param moodType The emotional type to filter by.
     * @param callback The callback to handle success or failure.
     */
    public void firebaseQueryEmotional(String userID, String moodType, FirestoreCallback callback) {
        firestoreMoodHistory.firebaseQueryEmotional(userID, moodType, callback);
    }

    /**
     * Queries mood history based on time.
     *
     * @param userID The ID of the user whose mood history to query.
     * @param sevenDays Whether to query for the last seven days.
     * @param ascending Whether to sort in ascending order.
     * @param callback The callback to handle success or failure.
     */
    public void firebaseQueryTime(String userID, boolean sevenDays, boolean ascending, FirestoreCallback callback) {
        firestoreMoodHistory.firebaseQueryTime(userID, sevenDays, ascending, callback);
    }

    // ======= FOLLOWING FUNCTIONS =======

    /**
     * Sends a friend request.
     *
     * @param fromUserId The ID of the user sending the request.
     * @param toUserId The ID of the user receiving the request.
     * @param callback The callback to handle success or failure.
     */
    public void sendFriendRequest(String fromUserId, String toUserId, FirestoreFollowing.FollowingCallback callback) {
        firestoreFollowing.sendFriendRequest(fromUserId, toUserId, callback);
    }

    /**
     * Accepts a friend request.
     *
     * @param requestId The ID of the friend request to accept.
     * @param callback The callback to handle success or failure.
     */
    public void acceptFriendRequest(String requestId, FirestoreFollowing.FollowingCallback callback) {
        firestoreFollowing.acceptFriendRequest(requestId, callback);
    }

    /**
     * Declines a friend request.
     *
     * @param requestId The ID of the friend request to decline.
     * @param callback The callback to handle success or failure.
     */
    public void declineFriendRequest(String requestId, FirestoreFollowing.FollowingCallback callback) {
        firestoreFollowing.declineFriendRequest(requestId, callback);
    }

    /**
     * Removes a user from the following list.
     *
     * @param userId The ID of the user removing from following.
     * @param unfollowUserId The ID of the user to unfollow.
     * @param callback The callback to handle success or failure.
     */
    public void removeFollowing(String userId, String unfollowUserId, FirestoreFollowing.FollowingCallback callback) {
        firestoreFollowing.removeFollowing(userId, unfollowUserId, callback);
    }

    /**
     * Listens for friend requests for a user.
     *
     * @param currentUserId The ID of the user to listen for friend requests.
     * @param callback The callback to handle success or failure.
     */
    public void listenForFriendRequests(String currentUserId, FirestoreFollowing.FollowingCallback callback) {
        firestoreFollowing.listenForFriendRequests(currentUserId, callback);
    }

    /**
     * Retrieves all users from Firestore.
     *
     * @param callback The callback to handle success or failure.
     */
    public void getAllUsers(FirestoreCallback callback) {
        firestoreUsers.getAllUsers(callback);
    }

    /**
     * Updates the profile picture of a user.
     *
     * @param userId The ID of the user to update.
     * @param base64Image The base64 encoded image string.
     * @param callback The callback to handle success or failure.
     */
    public void updateUserProfilePicture(String userId, String base64Image, FirestoreCallback callback) {
        firestoreUsers.updateUserProfilePicture(userId, base64Image, callback);
    }

    /**
     * Updates the status of a user.
     *
     * @param userId The ID of the user to update.
     * @param status The new status string.
     * @param callback The callback to handle success or failure.
     */
    public void updateUserStatus(String userId, String status, FirestoreCallback callback) {
        firestoreUsers.updateUserStatus(userId, status, callback);
    }
}