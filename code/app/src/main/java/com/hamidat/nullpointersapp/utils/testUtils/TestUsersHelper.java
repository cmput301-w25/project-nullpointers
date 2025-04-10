/**
 * TestUsersHelper.java
 *
 * Utility class for managing test user relationships in Firestore.
 * Includes methods to insert, accept, decline, and remove fake friend requests and follow relationships.
 * Primarily used for testing social connection features in the app.
 *
 * Outstanding Issues: None
 */

package com.hamidat.nullpointersapp.utils.testUtils;

import android.util.Log;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreFollowing;

/**
 * Utility class for managing test user relationships in Firestore.
 */
public class TestUsersHelper {
    private static final String TAG = "TestUsersHelper";

    /**
     * Sends two real friend requests from two existing fake users to the target user.
     *
     * @param targetUserId The user receiving the follow requests.
     * @param fakeUserId1  The first fake user sending the request.
     */
    public static void insertFakeFollowRequests(String targetUserId, String fakeUserId1) {
        Log.d(TAG, "Sending friend request from " + fakeUserId1 + " to " + targetUserId);

        FirestoreFollowing followingHelper = new FirestoreFollowing(FirebaseFirestore.getInstance());

        followingHelper.sendFriendRequest(fakeUserId1, targetUserId, new FirestoreFollowing.FollowingCallback() {
            /**
             * Called when the friend request is successfully sent.
             *
             * @param result The result of the operation.
             */
            @Override
            public void onSuccess(Object result) {
                Log.d(TAG, "Friend request sent from " + fakeUserId1 + " → " + targetUserId);
            }

            /**
             * Called when sending the friend request fails.
             *
             * @param e The exception that occurred.
             */
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to send friend request from " + fakeUserId1 + ": " + e.getMessage());
            }
        });
    }

    /**
     * Deletes a friend request (if it exists) from one user to another.
     *
     * @param fromUserId Sender's user ID
     * @param toUserId   Receiver's user ID
     */
    public static void deleteFakeFollowRequest(String fromUserId, String toUserId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.d(TAG, "Deleting friend request from " + fromUserId + " to " + toUserId);

        db.collection("friend_requests")
                .whereEqualTo("fromUserId", fromUserId)
                .whereEqualTo("toUserId", toUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (var doc : querySnapshot.getDocuments()) {
                        db.collection("friend_requests").document(doc.getId()).delete()
                                .addOnSuccessListener(aVoid ->
                                        Log.d(TAG, "Deleted request from " + fromUserId + " to " + toUserId));
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to delete request from " + fromUserId + ": " + e.getMessage()));
    }

    /**
     * Accepts a friend request between two users.
     *
     * @param fromUserId The user who sent the friend request.
     * @param toUserId   The user who is accepting the friend request.
     */
    public static void acceptFriendRequestBetween(String fromUserId, String toUserId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirestoreFollowing followingHelper = new FirestoreFollowing(db);

        db.collection("friend_requests")
                .whereEqualTo("fromUserId", fromUserId)
                .whereEqualTo("toUserId", toUserId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String requestId = querySnapshot.getDocuments().get(0).getId();
                        Log.d("TestUsersHelper", "Found requestId: " + requestId + " → accepting...");
                        followingHelper.acceptFriendRequest(requestId, new FirestoreFollowing.FollowingCallback() {
                            /**
                             * Called when the friend request is successfully accepted.
                             *
                             * @param result The result of the operation.
                             */
                            @Override
                            public void onSuccess(Object result) {
                                Log.d("TestUsersHelper", "Accepted friend request from " + fromUserId + " to " + toUserId);
                            }

                            /**
                             * Called when accepting the friend request fails.
                             *
                             * @param e The exception that occurred.
                             */
                            @Override
                            public void onFailure(Exception e) {
                                Log.e("TestUsersHelper", "Failed to accept friend request: " + e.getMessage());
                            }
                        });
                    } else {
                        Log.w("TestUsersHelper", "No pending request found from " + fromUserId + " to " + toUserId);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("TestUsersHelper", "Error fetching friend request: " + e.getMessage())
                );
    }

    /**
     * Removes the following relationship between two users.
     *
     * @param userId         The user who is unfollowing.
     * @param unfollowUserId The user who is being unfollowed.
     */
    public static void removeFollowingBetween(String userId, String unfollowUserId) {
        FirestoreFollowing followingHelper = new FirestoreFollowing(FirebaseFirestore.getInstance());

        Log.d("TestUsersHelper", "Removing following between " + userId + " and " + unfollowUserId);

        followingHelper.removeFollowing(userId, unfollowUserId, new FirestoreFollowing.FollowingCallback() {
            /**
             * Called when the following relationship is successfully removed.
             *
             * @param result The result of the operation.
             */
            @Override
            public void onSuccess(Object result) {
                Log.d("TestUsersHelper", "Successfully removed following between " + userId + " and " + unfollowUserId);
            }

            /**
             * Called when removing the following relationship fails.
             *
             * @param e The exception that occurred.
             */
            @Override
            public void onFailure(Exception e) {
                Log.e("TestUsersHelper", "Failed to remove following: " + e.getMessage());
            }
        });
    }

    /**
     * Inserts a following relationship between two users.
     *
     * @param userId1 The first user ID.
     * @param userId2 The second user ID.
     */
    public static void insertFollowingBetween(String userId1, String userId2) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirestoreFollowing followingHelper = new FirestoreFollowing(firestore);

        Log.d(TAG, "Creating following relationship between " + userId1 + " and " + userId2);

        followingHelper.removeFollowing(userId1, userId2, new FirestoreFollowing.FollowingCallback() {
            /**
             * Called when the following relationship is successfully removed (for resetting).
             *
             * @param result The result of the operation.
             */
            @Override
            public void onSuccess(Object result) {
                // First remove any existing following just to avoid duplication
                firestore.collection("users").document(userId1)
                        .update("following", FieldValue.arrayUnion(userId2))
                        .addOnSuccessListener(aVoid1 ->
                                Log.d(TAG, "Added " + userId2 + " to following list of " + userId1));

                firestore.collection("users").document(userId2)
                        .update("following", FieldValue.arrayUnion(userId1))
                        .addOnSuccessListener(aVoid2 ->
                                Log.d(TAG, "Added " + userId1 + " to following list of " + userId2));
            }

            /**
             * Called when removing the following relationship fails.
             *
             * @param e The exception that occurred.
             */
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error while resetting following before insert: " + e.getMessage());
            }
        });
    }

    /**
     * Deletes a user document from the "users" collection by username.
     *
     * @param username The username of the user to delete.
     */
    public static void deleteUserByUsername(String username) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.d(TAG, "Attempting to delete user with username: " + username);

        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        querySnapshot.getDocuments().forEach(doc -> {
                            db.collection("users").document(doc.getId())
                                    .delete()
                                    .addOnSuccessListener(aVoid ->
                                            Log.d(TAG, "Successfully deleted user: " + username))
                                    .addOnFailureListener(e ->
                                            Log.e(TAG, "Failed to delete user: " + e.getMessage()));
                        });
                    } else {
                        Log.w(TAG, "No user found with username: " + username);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to fetch user by username: " + e.getMessage()));
    }
}