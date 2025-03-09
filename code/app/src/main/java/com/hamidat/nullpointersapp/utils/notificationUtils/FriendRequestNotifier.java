package com.hamidat.nullpointersapp.utils.notificationUtils;

import android.content.Context;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreFollowing;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import androidx.annotation.Nullable;

/**
 * Singleton class for listening and notifying friend requests.
 */
public class FriendRequestNotifier {
    private static FriendRequestNotifier instance;
    private Set<String> notifiedRequestIds = new HashSet<>();
    private long startListeningTime = 0;
    private boolean isListening = false;

    /**
     * Returns the singleton instance of FriendRequestNotifier.
     *
     * @return The instance of FriendRequestNotifier.
     */
    public static synchronized FriendRequestNotifier getInstance() {
        if (instance == null) {
            instance = new FriendRequestNotifier();
        }
        return instance;
    }

    /**
     * Starts listening for incoming friend requests for the current user.
     * Prevents duplicate notifications using notifiedRequestIds.
     *
     * @param context         The application context.
     * @param currentUserId   The current user's ID.
     * @param firestoreHelper The FirestoreHelper instance.
     */
    public void startListeningIncomingRequests(final Context context, final String currentUserId, final FirestoreHelper firestoreHelper) {
        if (isListening) return;
        isListening = true;
        startListeningTime = System.currentTimeMillis();
        firestoreHelper.listenForFriendRequests(currentUserId, new FirestoreFollowing.FollowingCallback() {
            /**
             * Called when a friend request is received.
             *
             * @param result A map containing friend request data.
             */
            @Override
            public void onSuccess(Object result) {
                if (result instanceof Map) {
                    Map<String, Object> requestData = (Map<String, Object>) result;
                    String requestId = (String) requestData.get("requestId");
                    if (requestId == null || notifiedRequestIds.contains(requestId)) {
                        return;
                    }
                    Object tsObj = requestData.get("timestamp");
                    if (tsObj instanceof Timestamp) {
                        long requestTime = ((Timestamp) tsObj).toDate().getTime();
                        if (requestTime < startListeningTime) {
                            notifiedRequestIds.add(requestId);
                            return;
                        }
                    }
                    notifiedRequestIds.add(requestId);
                    String fromUserId = (String) requestData.get("fromUserId");
                    firestoreHelper.getUser(fromUserId, new FirestoreHelper.FirestoreCallback() {
                        /**
                         * Called when the sender's user data is successfully fetched.
                         *
                         * @param result A map containing the sender's user data.
                         */
                        @Override
                        public void onSuccess(Object result) {
                            String senderUsername = fromUserId;
                            if (result instanceof Map) {
                                Map<String, Object> userData = (Map<String, Object>) result;
                                if (userData.get("username") != null) {
                                    senderUsername = (String) userData.get("username");
                                }
                            }
                            NotificationHelper.sendFriendRequestNotification(context, senderUsername, currentUserId, requestId);
                        }
                        /**
                         * Called when there is an error fetching the sender's user data.
                         *
                         * @param e The exception encountered.
                         */
                        @Override
                        public void onFailure(Exception e) {
                            NotificationHelper.sendFriendRequestNotification(context, fromUserId, currentUserId, requestId);
                        }
                    });
                }
            }
            /**
             * Called when there is an error listening for friend requests.
             *
             * @param e The exception encountered.
             */
            @Override
            public void onFailure(Exception e) {
                // Optionally log errors.
            }
        });
    }

    /**
     * Starts listening for accepted friend requests on the sender's device.
     * Notifies the sender when a friend request is accepted.
     *
     * @param context       The application context.
     * @param currentUserId The current user's ID.
     */
    public void startListeningAcceptedRequests(final Context context, final String currentUserId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("friendRequests")
                .whereEqualTo("fromUserId", currentUserId)
                .whereEqualTo("status", "accepted")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    /**
                     * Called when there is a change in the friendRequests collection.
                     *
                     * @param value QuerySnapshot containing document changes.
                     * @param error The exception encountered, if any.
                     */
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null || value == null || value.isEmpty()) return;
                        for (DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                String requestId = dc.getDocument().getId();
                                if (requestId == null || notifiedRequestIds.contains(requestId)) continue;
                                notifiedRequestIds.add(requestId);
                                Map<String, Object> acceptedData = dc.getDocument().getData();
                                String accepterUsername = (String) acceptedData.get("accepterUsername");
                                if (accepterUsername == null) {
                                    accepterUsername = "A friend";
                                }
                                NotificationHelper.sendFriendAcceptedNotification(context, accepterUsername, currentUserId);
                            }
                        }
                    }
                });
    }

    /**
     * Clears the set of notified friend request IDs.
     */
    public void clearNotifiedRequests() {
        notifiedRequestIds.clear();
    }

    /**
     * Stops listening for friend requests.
     */
    public void stopListening() {
        isListening = false;
    }
}
