package com.hamidat.nullpointersapp.mainFragments;

import android.content.Intent;
import android.os.Bundle;
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

public class NotificationFragment extends Fragment {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private List<NotificationItem> notifications = new ArrayList<>();
    private FirestoreHelper firestoreHelper;
    private String currentUserId;

    // Each notification item now includes a type ("friend_request" or "post")
    public static class NotificationItem {
        public String id; // document id of the notification
        public String fromUserId;
        public String username;
        public long timestamp; // in milliseconds
        public String type; // "friend_request" or "post"

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
        // Assume currentUserId is passed via the Activity's intent.
        currentUserId = getActivity().getIntent().getStringExtra("USER_ID");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("notifications")
                .whereEqualTo("toUserId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((@Nullable QuerySnapshot value, @Nullable com.google.firebase.firestore.FirebaseFirestoreException error) -> {
                    if (error != null || value == null) return;
                    for (DocumentChange dc : value.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            String id = dc.getDocument().getId();
                            Map<String, Object> data = dc.getDocument().getData();
                            String type = (String) data.get("type");
                            String fromUserId = (String) data.get("fromUserId");
                            String username = (String) data.get("username");
                            Timestamp ts = dc.getDocument().getTimestamp("timestamp");
                            long time = ts != null ? ts.toDate().getTime() : System.currentTimeMillis();
                            notifications.add(new NotificationItem(id, fromUserId, username, time, type));
                            requireActivity().runOnUiThread(() -> {
                                adapter.notifyDataSetChanged();
                                ((MainActivity) getActivity()).updateNotificationIcon(!notifications.isEmpty());
                            });
                        }
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
                if ("friend_request".equals(item.type)) {
                    tvMessage.setText(item.username + " has sent you a friend request");
                    btnDecline.setVisibility(View.VISIBLE);
                    btnAccept.setVisibility(View.VISIBLE);
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
                } else if ("post".equals(item.type)) {
                    tvMessage.setText(item.username + " has posted a new mood");
                    btnDecline.setVisibility(View.GONE);
                    btnAccept.setVisibility(View.GONE);
                    itemView.setOnClickListener(v -> {
                        // Start MainActivity with extras so that MainActivity will navigate to SearchFragment
                        Intent intent = new Intent(getContext(), MainActivity.class);
                        intent.putExtra("USER_ID", currentUserId);
                        intent.putExtra("open_profile", true);
                        intent.putExtra("profile_user_id", item.fromUserId);
                        startActivity(intent);
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
    }

    private String getTimeAgo(long timeMillis) {
        long now = System.currentTimeMillis();
        long diff = now - timeMillis;
        if (diff < 60000) {
            return "Just now";
        } else if (diff < 3600000) {
            long minutes = diff / 60000;
            return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
        } else if (diff < 86400000) {
            long hours = diff / 3600000;
            return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        } else if (diff < 604800000) {
            long days = diff / 86400000;
            return days + " day" + (days == 1 ? "" : "s") + " ago";
        } else {
            long weeks = diff / 604800000;
            return weeks + " week" + (weeks == 1 ? "" : "s") + " ago";
        }
    }
}
