package com.hamidat.nullpointersapp.utils.firebaseUtils;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for managing friend request operations using Firestore.
 */
public class FirestoreFollowing {
    private static final String FRIEND_REQUESTS_COLLECTION = "friend_requests";
    private static final String USERS_COLLECTION = "users";
    private final FirebaseFirestore firestore;

    /**
     * Callback interface for Firestore operations.
     */
    public interface FollowingCallback {
        /**
         * Called when the operation succeeds.
         *
         * @param result The result object.
         */
        void onSuccess(Object result);

        /**
         * Called when the operation fails.
         *
         * @param e The exception encountered.
         */
        void onFailure(Exception e);
    }

    /**
     * Constructs a new FirestoreFollowing instance.
     *
     * @param firestore The FirebaseFirestore instance.
     */
    public FirestoreFollowing(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Sends a friend request from one user to another.
     *
     * @param fromUserId The sender's user ID.
     * @param toUserId   The recipient's user ID.
     * @param callback   Callback to handle the result.
     */
    public void sendFriendRequest(String fromUserId, String toUserId, FollowingCallback callback) {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("fromUserId", fromUserId);
        requestData.put("toUserId", toUserId);
        requestData.put("timestamp", FieldValue.serverTimestamp());
        requestData.put("status", "pending");

        firestore.collection(FRIEND_REQUESTS_COLLECTION)
                .add(requestData)
                .addOnSuccessListener(documentReference -> callback.onSuccess(documentReference.getId()))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Accepts a friend request and updates both users' following lists.
     *
     * @param requestId The unique identifier of the friend request.
     * @param callback  Callback to handle the result.
     */
    public void acceptFriendRequest(String requestId, FollowingCallback callback) {
        firestore.collection(FRIEND_REQUESTS_COLLECTION)
                .document(requestId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fromUserId = documentSnapshot.getString("fromUserId");
                        String toUserId = documentSnapshot.getString("toUserId");
                        firestore.collection(FRIEND_REQUESTS_COLLECTION)
                                .document(requestId)
                                .update("status", "accepted")
                                .addOnSuccessListener(aVoid -> {
                                    updateUserFollowing(fromUserId, toUserId, new FollowingCallback() {
                                        @Override
                                        public void onSuccess(Object result) {
                                            updateUserFollowing(toUserId, fromUserId, callback);
                                        }
                                        @Override
                                        public void onFailure(Exception e) {
                                            callback.onFailure(e);
                                        }
                                    });
                                })
                                .addOnFailureListener(callback::onFailure);
                    } else {
                        callback.onFailure(new Exception("Friend request not found"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Declines a friend request.
     *
     * @param requestId The unique identifier of the friend request.
     * @param callback  Callback to handle the result.
     */
    public void declineFriendRequest(String requestId, FollowingCallback callback) {
        firestore.collection(FRIEND_REQUESTS_COLLECTION)
                .document(requestId)
                .update("status", "declined")
                .addOnSuccessListener(aVoid -> callback.onSuccess("declined"))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Listens for incoming pending friend requests for the current user.
     *
     * @param currentUserId The current user's ID.
     * @param callback      Callback to handle incoming requests.
     */
    public void listenForFriendRequests(String currentUserId, FollowingCallback callback) {
        firestore.collection(FRIEND_REQUESTS_COLLECTION)
                .whereEqualTo("toUserId", currentUserId)
                .whereEqualTo("status", "pending")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    /**
                     * Called when there is an update in the friend requests.
                     *
                     * @param querySnapshot The snapshot of the query.
                     * @param error         The error encountered, if any.
                     */
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            callback.onFailure(error);
                            return;
                        }
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                Map<String, Object> data = doc.getData();
                                data.put("requestId", doc.getId());
                                callback.onSuccess(data);
                                break;
                            }
                        }
                    }
                });
    }

    /**
     * Removes a following relationship (unfollow) between two users.
     *
     * @param userId         The current user's ID.
     * @param unfollowUserId The user ID to unfollow.
     * @param callback       Callback to handle the result.
     */
    public void removeFollowing(String userId, String unfollowUserId, FollowingCallback callback) {
        firestore.collection(USERS_COLLECTION)
                .document(userId)
                .update("following", FieldValue.arrayRemove(unfollowUserId))
                .addOnSuccessListener(aVoid -> {
                    firestore.collection(USERS_COLLECTION)
                            .document(unfollowUserId)
                            .update("following", FieldValue.arrayRemove(userId))
                            .addOnSuccessListener(aVoid2 -> callback.onSuccess("removed"))
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Helper method to update a user's following list by adding another user.
     *
     * @param userId       The user whose following list is to be updated.
     * @param followUserId The user to be added.
     * @param callback     Callback to handle the result.
     */
    private void updateUserFollowing(String userId, String followUserId, FollowingCallback callback) {
        firestore.collection(USERS_COLLECTION)
                .document(userId)
                .update("following", FieldValue.arrayUnion(followUserId))
                .addOnSuccessListener(aVoid -> callback.onSuccess("updated"))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Retrieves outgoing friend requests for the current user.
     *
     * @param currentUserId The current user's ID.
     * @param callback      Callback to handle the result.
     */
    public void getOutgoingFriendRequests(String currentUserId, FollowingCallback callback) {
        firestore.collection(FRIEND_REQUESTS_COLLECTION)
                .whereEqualTo("fromUserId", currentUserId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    ArrayList<String> pendingUserIds = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()){
                        String toUserId = doc.getString("toUserId");
                        if (toUserId != null) {
                            pendingUserIds.add(toUserId);
                        }
                    }
                    callback.onSuccess(pendingUserIds);
                })
                .addOnFailureListener(callback::onFailure);
    }
}
