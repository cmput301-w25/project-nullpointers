package com.hamidat.nullpointersapp.utils.notificationUtils;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreFollowing;

public class FriendRequestActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String requestId = intent.getStringExtra("request_id");
        String currentUserId = intent.getStringExtra("current_user_id");
        FirestoreHelper firestoreHelper = new FirestoreHelper();
        if (action != null && requestId != null && currentUserId != null) {
            if (action.equals(NotificationHelper.ACTION_ACCEPT)) {
                firestoreHelper.acceptFriendRequest(requestId, new FirestoreFollowing.FollowingCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        Toast.makeText(context, "Friend request accepted", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(context, "Error accepting request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (action.equals(NotificationHelper.ACTION_DECLINE)) {
                firestoreHelper.declineFriendRequest(requestId, new FirestoreFollowing.FollowingCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        Toast.makeText(context, "Friend request declined", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(context, "Error declining request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}
