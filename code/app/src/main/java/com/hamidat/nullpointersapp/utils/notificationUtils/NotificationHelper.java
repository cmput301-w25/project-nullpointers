/**
 * NotificationHelper.java
 *
 * Utility class for creating and managing friend request notifications.
 * Provides methods for sending notifications when a friend request is received
 * or accepted, including optional Accept/Decline actions.
 *
 * Outstanding Issues: None
 */

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

/**
 * Helper class for sending notifications related to friend requests.
 */
public class NotificationHelper {
    public static final String CHANNEL_ID = "friend_request_channel";
    public static final String CHANNEL_NAME = "Friend Requests";
    public static final String ACTION_ACCEPT = "com.hamidat.nullpointersapp.ACTION_ACCEPT";
    public static final String ACTION_DECLINE = "com.hamidat.nullpointersapp.ACTION_DECLINE";

    /**
     * Sends a notification for an incoming friend request.
     *
     * @param context        The application context.
     * @param senderUsername The username of the sender.
     * @param currentUserId  The current user's ID.
     * @param requestId      The unique identifier of the friend request.
     */
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

        // Intent for tapping the notification: now it redirects to NotificationFragment.
        Intent tapIntent = new Intent(context, MainActivity.class);
        tapIntent.putExtra("open_notification", true);
        tapIntent.putExtra("USER_ID", currentUserId);
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent tapPendingIntent = PendingIntent.getActivity(
                context,
                0,
                tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        // Create action intents for Accept and Decline (if used)
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


        if (notificationManager != null) {
            notificationManager.notify(1001, builder.build());
        }
    }

    /**
     * Sends a notification to the sender when their friend request is accepted.
     *
     * @param context         The application context.
     * @param accepterUsername The username of the user who accepted the request.
     * @param senderUserId    The sender's user ID.
     */
    public static void sendFriendAcceptedNotification(Context context, String accepterUsername, String senderUserId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications for friend request responses");
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        // Create an intent that opens MainActivity and navigates to NotificationFragment.
        Intent tapIntent = new Intent(context, MainActivity.class);
        tapIntent.putExtra("open_notification", true);
        tapIntent.putExtra("USER_ID", senderUserId);
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent tapPendingIntent = PendingIntent.getActivity(
                context,
                0,
                tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        String contentTitle = accepterUsername + " has accepted your request!";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(contentTitle)
                .setContentText("Tap to view notifications.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(tapPendingIntent);

        if (notificationManager != null) {
            notificationManager.notify(1002, builder.build());
        }
    }

    /**
     * Sends a notification for a new post from a user the current user follows.
     *
     * @param context         The context in which the notification is sent.
     * @param currentUserId   The ID of the current user.
     * @param senderUsername  The username of the user who made the post.
     * @param senderUserId    The ID of the user who made the post.
     */
    public static void sendPostNotification(Context context, String currentUserId, String senderUsername, String senderUserId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications for posts from users you follow");
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        Intent tapIntent = new Intent(context, MainActivity.class);
        tapIntent.putExtra("USER_ID", currentUserId);
        tapIntent.putExtra("open_profile", true);
        tapIntent.putExtra("profile_user_id", senderUserId);
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent tapPendingIntent = PendingIntent.getActivity(
                context,
                0,
                tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        String contentTitle = senderUsername + " has posted a new mood!";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(contentTitle)
                .setContentText("Tap to view profile.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(tapPendingIntent);

        if (notificationManager != null) {
            notificationManager.notify(2001, builder.build());
        }
    }
}