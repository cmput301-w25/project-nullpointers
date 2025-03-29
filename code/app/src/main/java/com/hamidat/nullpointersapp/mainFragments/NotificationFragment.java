/**
 * NotificationFragment.java
 *
 * This fragment displays incoming friend requests for the current user.
 * Each request includes the sender's username, a timestamp ("x time ago"), and two buttons: Accept and Decline.
 * Upon action, the request is removed and Firestore is updated accordingly.
 *
 * <p><b>Outstanding issues:</b> None.</p>
 */

package com.hamidat.nullpointersapp.mainFragments;

import static com.hamidat.nullpointersapp.utils.notificationUtils.NotificationHelper.CHANNEL_ID;
import static com.hamidat.nullpointersapp.utils.notificationUtils.NotificationHelper.CHANNEL_NAME;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.hamidat.nullpointersapp.MainActivity;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreFollowing;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Fragment to display pending friend requests.
 * Each item shows a message (e.g. "Username has sent you a friend request"),
 * a "time ago" label, and two buttons: Decline and Accept.
 */
public class NotificationFragment extends Fragment {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private List<NotificationItem> notifications = new ArrayList<>();
    private FirestoreHelper firestoreHelper;
    private String currentUserId;

    public static class NotificationItem {
        public String requestId;
        public String fromUserId;
        public String username;
        public long timestamp; // milliseconds

        public NotificationItem(String requestId, String fromUserId, String username, long timestamp) {
            this.requestId = requestId;
            this.fromUserId = fromUserId;
            this.username = username;
            this.timestamp = timestamp;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rvNotifications = view.findViewById(R.id.rvNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter(notifications);
        rvNotifications.setAdapter(adapter);

        firestoreHelper = new FirestoreHelper();
        currentUserId = getActivity().getIntent().getStringExtra("USER_ID");

        // Listen for pending friend requests.
        firestoreHelper.listenForFriendRequests(currentUserId, new FirestoreFollowing.FollowingCallback() {
            @Override
            public void onSuccess(Object result) {
                if (result instanceof Map) {
                    Map<String, Object> requestData = (Map<String, Object>) result;
                    String requestId = (String) requestData.get("requestId");
                    String fromUserId = (String) requestData.get("fromUserId");
                    long ts = System.currentTimeMillis(); // fallback
                    if (requestData.get("timestamp") != null) {
                        Timestamp firebaseTs = (Timestamp) requestData.get("timestamp");
                        ts = firebaseTs.toDate().getTime();
                    }
                    // Check if the notification already exists.
                    boolean exists = false;
                    for (NotificationItem item : notifications) {
                        if (item.requestId.equals(requestId)) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        long finalTs = ts;
                        firestoreHelper.getUser(fromUserId, new FirestoreHelper.FirestoreCallback() {
                            @Override
                            public void onSuccess(Object result) {
                                String username = fromUserId;
                                if (result instanceof Map) {
                                    Map<String, Object> userData = (Map<String, Object>) result;
                                    if (userData.get("username") != null) {
                                        username = (String) userData.get("username");
                                    }
                                }
                                notifications.add(new NotificationItem(requestId, fromUserId, username, finalTs));
                                requireActivity().runOnUiThread(() -> {
                                    adapter.notifyDataSetChanged();
                                    // Update icon based on notification list
                                    ((MainActivity) getActivity()).updateNotificationIcon(!notifications.isEmpty());
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                // Optionally log error.
            }
        });
    }

    private class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
        private final List<NotificationItem> items;

        NotificationAdapter(List<NotificationItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item, parent, false);
            return new NotificationViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
            NotificationItem item = items.get(position);
            holder.bind(item);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class NotificationViewHolder extends RecyclerView.ViewHolder {
            TextView tvMessage;
            TextView tvTimestamp;
            Button btnDecline, btnAccept;

            public NotificationViewHolder(@NonNull View itemView) {
                super(itemView);
                tvMessage = itemView.findViewById(R.id.tvNotificationMessage);
                tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
                btnDecline = itemView.findViewById(R.id.btnDecline);
                btnAccept = itemView.findViewById(R.id.btnAccept);
            }

            void bind(NotificationItem item) {
                tvMessage.setText(item.username + " has sent you a friend request");
                tvTimestamp.setText(getTimeAgo(item.timestamp));
                btnDecline.setOnClickListener(v -> {
                    firestoreHelper.declineFriendRequest(item.requestId, new FirestoreFollowing.FollowingCallback() {
                        @Override
                        public void onSuccess(Object result) {
                            Toast.makeText(getContext(), "Friend request declined", Toast.LENGTH_SHORT).show();
                            removeNotification(item);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                });
                btnAccept.setOnClickListener(v -> {
                    firestoreHelper.acceptFriendRequest(item.requestId, new FirestoreFollowing.FollowingCallback() {
                        @Override
                        public void onSuccess(Object result) {
                            Toast.makeText(getContext(), "Friend request accepted", Toast.LENGTH_SHORT).show();
                            removeNotification(item);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            }
        }
    }

    private void removeNotification(NotificationItem item) {
        notifications.remove(item);
        requireActivity().runOnUiThread(() -> {
            adapter.notifyDataSetChanged();
            ((MainActivity) getActivity()).updateNotificationIcon(!notifications.isEmpty());
        });
    }

    /**
     * Computes a "time ago" string.
     * Returns "Just now" if less than a minute,
     * "x minutes ago" if less than an hour,
     * "x hours ago" if less than a day,
     * "x days ago" if less than a week,
     * "x weeks ago" if less than 4 weeks.
     */
    private String getTimeAgo(long timeMillis) {
        long now = System.currentTimeMillis();
        long diff = now - timeMillis;
        if (diff < TimeUnit.MINUTES.toMillis(1)) {
            return "Just now";
        } else if (diff < TimeUnit.HOURS.toMillis(1)) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
        } else if (diff < TimeUnit.DAYS.toMillis(1)) {
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        } else if (diff < TimeUnit.DAYS.toMillis(7)) {
            long days = TimeUnit.MILLISECONDS.toDays(diff);
            return days + " day" + (days == 1 ? "" : "s") + " ago";
        } else {
            long weeks = TimeUnit.MILLISECONDS.toDays(diff) / 7;
            return weeks + " week" + (weeks == 1 ? "" : "s") + " ago";
        }
    }


}
