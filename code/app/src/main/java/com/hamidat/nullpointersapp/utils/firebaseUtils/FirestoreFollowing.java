package com.hamidat.nullpointersapp.utils.firebaseUtils;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;


public class FirestoreFollowing {
    private static final String FRIEND_REQUESTS_COLLECTION = "friend_requests";
    private static final String USERS_COLLECTION = "users";
    private final FirebaseFirestore firestore;

    public interface FollowingCallback {
        void onSuccess(Object result);
        void onFailure(Exception e);
    }

    public FirestoreFollowing(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    // Send a friend request from fromUserId to toUserId
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

    // Accept a friend request and update both users' "following" arrays
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

    // Decline a friend request
    public void declineFriendRequest(String requestId, FollowingCallback callback) {
        firestore.collection(FRIEND_REQUESTS_COLLECTION)
                .document(requestId)
                .update("status", "declined")
                .addOnSuccessListener(aVoid -> callback.onSuccess("declined"))
                .addOnFailureListener(callback::onFailure);
    }

    // Listen in real time for incoming friend requests for current user
    public void listenForFriendRequests(String currentUserId, FollowingCallback callback) {
        firestore.collection(FRIEND_REQUESTS_COLLECTION)
                .whereEqualTo("toUserId", currentUserId)
                .whereEqualTo("status", "pending")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            callback.onFailure(error);
                            return;
                        }
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // For demo, take the first pending request
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

    // Remove a following relationship (unfollow)
    public void removeFollowing(String userId, String unfollowUserId, FollowingCallback callback) {
        firestore.collection(USERS_COLLECTION)
                .document(userId)
                .update("following", FieldValue.arrayRemove(unfollowUserId))
                .addOnSuccessListener(aVoid -> callback.onSuccess("removed"))
                .addOnFailureListener(callback::onFailure);
    }

    // Helper: add a user to the following list of another user
    private void updateUserFollowing(String userId, String followUserId, FollowingCallback callback) {
        firestore.collection(USERS_COLLECTION)
                .document(userId)
                .update("following", FieldValue.arrayUnion(followUserId))
                .addOnSuccessListener(aVoid -> callback.onSuccess("updated"))
                .addOnFailureListener(callback::onFailure);
    }
}
