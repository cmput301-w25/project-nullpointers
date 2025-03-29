/**
 * NotificationFragment.java
 *
 * This fragment displays both friend request notifications and post notifications.
 * Friend request notifications are loaded using firestoreHelper.listenForFriendRequests (old logic).
 * Post notifications are loaded by querying the "notifications" collection (filtered by type "post").
 */
package com.hamidat.nullpointersapp.mainFragments;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.hamidat.nullpointersapp.MainActivity;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreFollowing;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class NotificationFragment extends Fragment {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    // We'll use a single list that holds both types of notifications.
    private final List<NotificationItem> notifications = new ArrayList<>();
    private FirestoreHelper firestoreHelper;
    private String currentUserId;

    // NotificationItem now holds a type field. For friend requests, we use requestId; for posts, we use notificationId.
    public static class NotificationItem {
        public String id; // For friend requests, this is requestId; for posts, this is the notification document ID.
        public String fromUserId;
        public String username;
        public long timestamp; // in milliseconds
        public String type;    // "friend_request" or "post"

        public NotificationItem(String id, String fromUserId, String username, long timestamp, String type) {
            this.id = id;
            this.fromUserId = fromUserId;
            this.username = username;
            this.timestamp = timestamp;
            this.type = type;
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

        // Set up friend request listener (old logic).
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
                    // Check if this friend request notification already exists.
                    boolean exists = false;
                    for (NotificationItem item : notifications) {
                        if ("friend_request".equals(item.type) && item.id.equals(requestId)) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        final long finalTs = ts;
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
                                notifications.add(new NotificationItem(requestId, fromUserId, username, finalTs, "friend_request"));
                                requireActivity().runOnUiThread(() -> {
                                    adapter.notifyDataSetChanged();
                                    ((MainActivity) getActivity()).updateNotificationIcon(!notifications.isEmpty());
                                });
                            }
                            @Override
                            public void onFailure(Exception e) { }
                        });
                    }
                }
            }
            @Override
            public void onFailure(Exception e) { }
        });

        // Set up post notification listener.
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("notifications")
                .whereEqualTo("toUserId", currentUserId)
                // No time filtering here (or use one if desired), so that all notifications remain.
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((@Nullable QuerySnapshot value, @Nullable com.google.firebase.firestore.FirebaseFirestoreException error) -> {
                    if (error != null || value == null) return;
                    for (DocumentChange dc : value.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            // Process only post notifications here.
                            Map<String, Object> data = dc.getDocument().getData();
                            String type = (String) data.get("type");
                            if ("post".equals(type)) {
                                String id = dc.getDocument().getId();
                                String fromUserId = (String) data.get("fromUserId");
                                String username = (String) data.get("username");
                                com.google.firebase.Timestamp ts = dc.getDocument().getTimestamp("timestamp");
                                long time = ts != null ? ts.toDate().getTime() : System.currentTimeMillis();
                                // Check if this post notification is already in our list.
                                boolean exists = false;
                                for (NotificationItem item : notifications) {
                                    if ("post".equals(item.type) && item.id.equals(id)) {
                                        exists = true;
                                        break;
                                    }
                                }
                                if (!exists) {
                                    notifications.add(new NotificationItem(id, fromUserId, username, time, "post"));
                                    requireActivity().runOnUiThread(() -> {
                                        adapter.notifyDataSetChanged();
                                        ((MainActivity) getActivity()).updateNotificationIcon(!notifications.isEmpty());
                                    });
                                }
                            }
                        }
                    }
                });

        // Bind Clear All button.
        Button btnClearAll = view.findViewById(R.id.btnClearAll);
        btnClearAll.setOnClickListener(v -> clearAllNotifications());
    }

    private void clearAllNotifications() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("notifications")
                .whereEqualTo("toUserId", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // Create a WriteBatch instance.
                    com.google.firebase.firestore.WriteBatch batch = db.batch();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        batch.delete(doc.getReference());
                    }
                    batch.commit().addOnSuccessListener(aVoid -> {
                        notifications.clear();
                        adapter.notifyDataSetChanged();
                        ((MainActivity)getActivity()).updateNotificationIcon(false);
                        NotificationManager nm = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                        if (nm != null) {
                            nm.cancelAll();
                        }
                        Toast.makeText(getContext(), "Notifications cleared", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to clear notifications: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to clear notifications: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
            // For friend requests, use notification_item.xml;
            // For posts, if you want a separate layout, you could check item type,
            // but here we assume both types are displayed in the same adapter.
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
            // We'll assume notification_item.xml uses an AppCompatImageView for friend requests,
            // and for post notifications we can use a ShapeableImageView from a separate layout if desired.
            // For simplicity, here we cast to generic ImageView.
            // If you want to support both, you can check the type.
            android.widget.ImageView ivNotificationIcon;

            public NotificationViewHolder(@NonNull View itemView) {
                super(itemView);
                tvMessage = itemView.findViewById(R.id.tvNotificationMessage);
                tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
                btnDecline = itemView.findViewById(R.id.btnDecline);
                btnAccept = itemView.findViewById(R.id.btnAccept);
                ivNotificationIcon = itemView.findViewById(R.id.ivNotificationIcon);
            }

            void bind(NotificationItem item) {
                if ("friend_request".equals(item.type)) {
                    tvMessage.setText(item.username + " has sent you a friend request");
                    if (btnDecline != null) btnDecline.setVisibility(View.VISIBLE);
                    if (btnAccept != null) btnAccept.setVisibility(View.VISIBLE);
                    itemView.setOnClickListener(null);
                    if (btnDecline != null) {
                        btnDecline.setOnClickListener(v -> {
                            firestoreHelper.declineFriendRequest(item.id, new FirestoreFollowing.FollowingCallback() {
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
                    }
                    if (btnAccept != null) {
                        btnAccept.setOnClickListener(v -> {
                            firestoreHelper.acceptFriendRequest(item.id, new FirestoreFollowing.FollowingCallback() {
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
                    // Load default icon for friend requests.
                    ivNotificationIcon.setImageResource(R.drawable.ic_notification);
                } else if ("post".equals(item.type)) {
                    firestoreHelper.getUser(item.fromUserId, new FirestoreHelper.FirestoreCallback() {
                        @Override
                        public void onSuccess(Object result) {
                            String actualUsername = "Someone";
                            if (result instanceof Map) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> userData = (Map<String, Object>) result;
                                if (userData.get("username") != null) {
                                    actualUsername = (String) userData.get("username");
                                }
                                final String finalUsername = actualUsername;
                                getActivity().runOnUiThread(() -> tvMessage.setText(finalUsername + " has posted a new mood"));

                                String profilePicBase64 = (String) userData.get("profilePicture");
                                if (profilePicBase64 != null && !profilePicBase64.isEmpty()) {
                                    try {
                                        byte[] decodedBytes = Base64.decode(profilePicBase64, Base64.DEFAULT);
                                        final android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                                        android.util.Log.d("NotificationAdapter", "Decoded bitmap: " + bitmap);
                                        getActivity().runOnUiThread(() -> {
                                            ivNotificationIcon.setImageBitmap(bitmap);
                                            ivNotificationIcon.clearColorFilter();
                                            ivNotificationIcon.setImageTintList(null);
                                            ivNotificationIcon.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                                        });
                                    } catch (Exception e) {
                                        getActivity().runOnUiThread(() -> ivNotificationIcon.setImageResource(R.drawable.default_user_icon));
                                    }
                                } else {
                                    getActivity().runOnUiThread(() -> ivNotificationIcon.setImageResource(R.drawable.default_user_icon));
                                }
                            }
                        }
                        @Override
                        public void onFailure(Exception e) {
                            getActivity().runOnUiThread(() -> {
                                tvMessage.setText("Someone has posted a new mood");
                                ivNotificationIcon.setImageResource(R.drawable.default_user_icon);
                            });
                        }
                    });
                    if (btnDecline != null) btnDecline.setVisibility(View.GONE);
                    if (btnAccept != null) btnAccept.setVisibility(View.GONE);
                    itemView.setOnClickListener(v -> {
                        Intent intent = new Intent(getContext(), MainActivity.class);
                        intent.putExtra("USER_ID", currentUserId);
                        intent.putExtra("open_profile", true);
                        intent.putExtra("profile_user_id", item.fromUserId);
                        startActivity(intent);
                        NotificationFragment.this.removeNotification(item);
                    });

                }

                tvTimestamp.setText(getTimeAgo(item.timestamp));
            }
        }
    }

    private void removeNotification(NotificationItem item) {
        notifications.remove(item);
        requireActivity().runOnUiThread(() -> {
            adapter.notifyDataSetChanged();
            ((MainActivity) getActivity()).updateNotificationIcon(!notifications.isEmpty());
        });
        FirebaseFirestore.getInstance().collection("notifications").document(item.id).delete();
    }

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
