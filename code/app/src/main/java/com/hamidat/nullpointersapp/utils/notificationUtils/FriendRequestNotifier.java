package com.hamidat.nullpointersapp.utils.notificationUtils;

import android.content.Context;
import com.google.firebase.Timestamp;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreFollowing;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FriendRequestNotifier {
    private static FriendRequestNotifier instance;
    private Set<String> notifiedRequestIds = new HashSet<>();
    private long startListeningTime = 0;
    private boolean isListening = false;

    public static synchronized FriendRequestNotifier getInstance() {
        if (instance == null) {
            instance = new FriendRequestNotifier();
        }
        return instance;
    }

    public void startListening(Context context, String currentUserId, FirestoreHelper firestoreHelper) {
        if (isListening) return;
        isListening = true;
        startListeningTime = System.currentTimeMillis();
        // Listen for friend requests globally.
        firestoreHelper.listenForFriendRequests(currentUserId, new FirestoreFollowing.FollowingCallback() {
            @Override
            public void onSuccess(Object result) {
                if (result instanceof Map) {
                    Map<String, Object> requestData = (Map<String, Object>) result;
                    String requestId = (String) requestData.get("requestId");
                    if (requestId == null || notifiedRequestIds.contains(requestId)) {
                        return;
                    }
                    // Check timestamp if available.
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
                        @Override
                        public void onSuccess(Object result) {
                            String senderUsername = fromUserId;
                            if (result instanceof Map) {
                                Map<String, Object> userData = (Map<String, Object>) result;
                                if (userData.get("username") != null) {
                                    senderUsername = (String) userData.get("username");
                                }
                            }
                            // Send a notification with the request details.
                            NotificationHelper.sendFriendRequestNotification(context, senderUsername, currentUserId, requestId);
                        }
                        @Override
                        public void onFailure(Exception e) {
                            NotificationHelper.sendFriendRequestNotification(context, fromUserId, currentUserId, requestId);
                        }
                    });
                }
            }
            @Override
            public void onFailure(Exception e) {
                // Optionally log errors.
            }
        });
    }

    // Call this when you know you don't want duplicate notifications (e.g., when FollowingFragment is active).
    public void clearNotifiedRequests() {
        notifiedRequestIds.clear();
    }

    // Stop listening (if you want to suspend global notifications)
    public void stopListening() {
        isListening = false;
    }
}
