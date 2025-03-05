package com.hamidat.nullpointersapp.utils.notificationUtils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.hamidat.nullpointersapp.MainActivity;
import com.hamidat.nullpointersapp.R;

public class NotificationHelper {
    public static final String CHANNEL_ID = "friend_request_channel";
    public static final String CHANNEL_NAME = "Friend Requests";
    public static final String ACTION_ACCEPT = "com.hamidat.nullpointersapp.ACTION_ACCEPT";
    public static final String ACTION_DECLINE = "com.hamidat.nullpointersapp.ACTION_DECLINE";

    public static void sendFriendRequestNotification(Context context, String senderUsername, String currentUserId, String requestId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications for friend requests");
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        // Intent for tapping the notification (opens MainActivity, which will navigate to FollowingFragment)
        Intent tapIntent = new Intent(context, MainActivity.class);
        tapIntent.putExtra("open_following", true);
        tapIntent.putExtra("USER_ID", currentUserId);
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent tapPendingIntent = PendingIntent.getActivity(
                context,
                0,
                tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        // Create action intents for Accept and Decline
        Intent acceptIntent = new Intent(context, FriendRequestActionReceiver.class);
        acceptIntent.setAction(ACTION_ACCEPT);
        acceptIntent.putExtra("request_id", requestId);
        acceptIntent.putExtra("current_user_id", currentUserId);
        PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                acceptIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        Intent declineIntent = new Intent(context, FriendRequestActionReceiver.class);
        declineIntent.setAction(ACTION_DECLINE);
        declineIntent.putExtra("request_id", requestId);
        declineIntent.putExtra("current_user_id", currentUserId);
        PendingIntent declinePendingIntent = PendingIntent.getBroadcast(
                context,
                2,
                declineIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(senderUsername + " has sent you an add request!")
                .setContentText("Tap to view and respond.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(tapPendingIntent);
//                .addAction(R.drawable.ic_notification, "Accept", acceptPendingIntent)
//                .addAction(R.drawable.ic_notification, "Decline", declinePendingIntent);

        if (notificationManager != null) {
            notificationManager.notify(1001, builder.build());
        }
    }
}
