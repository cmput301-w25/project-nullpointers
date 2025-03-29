/**
 * FriendRequestActionReceiver.java
 *
 * BroadcastReceiver to process friend request actions (Accept/Decline) triggered from system-level or notification actions.
 * It uses FirestoreHelper to update the friend request status in Firestore and provides user feedback via Toasts.
 *
 * Outstanding Issues: None
 */

package com.hamidat.nullpointersapp.utils.notificationUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreFollowing;

/**
 * BroadcastReceiver to handle friend request actions such as Accept or Decline.
 */
public class FriendRequestActionReceiver extends BroadcastReceiver {
    /**
     * Handles the received broadcast for friend request actions.
     *
     * @param context The application context.
     * @param intent  The intent containing the action and related data.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String requestId = intent.getStringExtra("request_id");
        String currentUserId = intent.getStringExtra("current_user_id");
        FirestoreHelper firestoreHelper = new FirestoreHelper();
        if (action != null && requestId != null && currentUserId != null) {
            if (action.equals(NotificationHelper.ACTION_ACCEPT)) {
                firestoreHelper.acceptFriendRequest(requestId, new FirestoreFollowing.FollowingCallback() {
                    /**
                     * Called when the friend request is successfully accepted.
                     *
                     * @param result The result of the operation.
                     */
                    @Override
                    public void onSuccess(Object result) {
                        Toast.makeText(context, "Friend request accepted", Toast.LENGTH_SHORT).show();
                    }
                    /**
                     * Called when there is an error accepting the friend request.
                     *
                     * @param e The exception encountered.
                     */
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(context, "Error accepting request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (action.equals(NotificationHelper.ACTION_DECLINE)) {
                firestoreHelper.declineFriendRequest(requestId, new FirestoreFollowing.FollowingCallback() {
                    /**
                     * Called when the friend request is successfully declined.
                     *
                     * @param result The result of the operation.
                     */
                    @Override
                    public void onSuccess(Object result) {
                        Toast.makeText(context, "Friend request declined", Toast.LENGTH_SHORT).show();
                    }
                    /**
                     * Called when there is an error declining the friend request.
                     *
                     * @param e The exception encountered.
                     */
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(context, "Error declining request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}
