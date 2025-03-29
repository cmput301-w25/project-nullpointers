package com.hamidat.nullpointersapp.mainFragments;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
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

    // NotificationItem supports both friend requests and post notifications.
    public static class NotificationItem {
        public String id; // Document ID
        public String fromUserId;
        public String username;
        public long timestamp; // milliseconds
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
        // Assume currentUserId is passed via MainActivity's intent.
        currentUserId = getActivity().getIntent().getStringExtra("USER_ID");
        AppCompatButton btnClearAll = view.findViewById(R.id.btnClearAll);
        btnClearAll.setOnClickListener(v -> clearAllNotifications());

        // Define a cutoff time (e.g., notifications from the last 24 hours)
        long cutoffTime = System.currentTimeMillis() - 24 * 60 * 60 * 1000;
        com.google.firebase.Timestamp cutoffTimestamp = new com.google.firebase.Timestamp(new java.util.Date(cutoffTime));

// Query notifications where the timestamp is newer than cutoffTimestamp
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("notifications")
                .whereEqualTo("toUserId", currentUserId)
                .whereGreaterThan("timestamp", cutoffTimestamp)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20)  // Only get the latest 20 notifications
                .addSnapshotListener((@Nullable QuerySnapshot value, @Nullable com.google.firebase.firestore.FirebaseFirestoreException error) -> {
                    if (error != null || value == null) return;
                    for (DocumentChange dc : value.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            String id = dc.getDocument().getId();
                            Map<String, Object> data = dc.getDocument().getData();
                            String type = (String) data.get("type");
                            String fromUserId = (String) data.get("fromUserId");
                            String username = (String) data.get("username");
                            com.google.firebase.Timestamp ts = dc.getDocument().getTimestamp("timestamp");
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

        @Override
        public int getItemViewType(int position) {
            // Return 1 for "post" notifications, 0 for "friend_request"
            NotificationItem item = items.get(position);
            return "post".equals(item.type) ? 1 : 0;
        }

        @NonNull
        @Override
        public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            if (viewType == 1) {
                // Inflate post notification layout for "post" type notifications.
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_notification_item, parent, false);
            } else {
                // Inflate friend request (or generic) layout.
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item, parent, false);
            }
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
            // Use ShapeableImageView if available; if the view is from notification_item.xml,
            // it may be an ImageView. We cast safely if possible.
            View iconView;

            public NotificationViewHolder(@NonNull View itemView) {
                super(itemView);
                tvMessage = itemView.findViewById(R.id.tvNotificationMessage);
                tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
                btnDecline = itemView.findViewById(R.id.btnDecline);
                btnAccept = itemView.findViewById(R.id.btnAccept);
                iconView = itemView.findViewById(R.id.ivNotificationIcon);
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
                                    NotificationFragment.this.removeNotification(item);
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
                                    NotificationFragment.this.removeNotification(item);
                                }
                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        });
                    }
                    // For friend requests, load a default icon.
                    if (iconView != null) {
                        if (iconView instanceof com.google.android.material.imageview.ShapeableImageView) {
                            ((com.google.android.material.imageview.ShapeableImageView) iconView)
                                    .setImageResource(R.drawable.ic_notification);
                        } else if (iconView instanceof ImageView) {
                            ((ImageView) iconView).setImageResource(R.drawable.ic_notification);
                        }
                    }
                } else if ("post".equals(item.type)) {
                    // For post notifications, fetch the actual user data.
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
                                tvMessage.setText(actualUsername + " has posted a new mood");

                                String profilePicBase64 = (String) userData.get("profilePicture");
                                if (profilePicBase64 != null && !profilePicBase64.isEmpty()) {
                                    try {
                                        byte[] decodedBytes = Base64.decode(profilePicBase64, Base64.DEFAULT);
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                                        if (iconView != null) {
                                            if (iconView instanceof com.google.android.material.imageview.ShapeableImageView) {
                                                ((com.google.android.material.imageview.ShapeableImageView) iconView).post(() ->
                                                        ((com.google.android.material.imageview.ShapeableImageView) iconView).setImageBitmap(bitmap));
                                            } else if (iconView instanceof ImageView) {
                                                ((ImageView) iconView).post(() -> ((ImageView) iconView).setImageBitmap(bitmap));
                                            }
                                        }
                                    } catch (Exception e) {
                                        if (iconView != null) {
                                            if (iconView instanceof com.google.android.material.imageview.ShapeableImageView) {
                                                ((com.google.android.material.imageview.ShapeableImageView) iconView).post(() ->
                                                        ((com.google.android.material.imageview.ShapeableImageView) iconView).setImageResource(R.drawable.default_user_icon));
                                            } else if (iconView instanceof ImageView) {
                                                ((ImageView) iconView).post(() ->
                                                        ((ImageView) iconView).setImageResource(R.drawable.default_user_icon));
                                            }
                                        }
                                    }
                                } else {
                                    if (iconView != null) {
                                        if (iconView instanceof com.google.android.material.imageview.ShapeableImageView) {
                                            ((com.google.android.material.imageview.ShapeableImageView) iconView).post(() ->
                                                    ((com.google.android.material.imageview.ShapeableImageView) iconView).setImageResource(R.drawable.default_user_icon));
                                        } else if (iconView instanceof ImageView) {
                                            ((ImageView) iconView).post(() ->
                                                    ((ImageView) iconView).setImageResource(R.drawable.default_user_icon));
                                        }
                                    }
                                }
                            }
                        }
                        @Override
                        public void onFailure(Exception e) {
                            tvMessage.setText("Someone has posted a new mood");
                            if (iconView != null) {
                                if (iconView instanceof com.google.android.material.imageview.ShapeableImageView) {
                                    ((com.google.android.material.imageview.ShapeableImageView) iconView).post(() ->
                                            ((com.google.android.material.imageview.ShapeableImageView) iconView).setImageResource(R.drawable.default_user_icon));
                                } else if (iconView instanceof ImageView) {
                                    ((ImageView) iconView).post(() ->
                                            ((ImageView) iconView).setImageResource(R.drawable.default_user_icon));
                                }
                            }
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



    private void clearAllNotifications() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("notifications")
                .whereEqualTo("toUserId", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // Use a batch to delete all documents.
                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        batch.delete(doc.getReference());
                    }
                    batch.commit().addOnSuccessListener(aVoid -> {
                        // Clear local list and update UI.
                        notifications.clear();
                        adapter.notifyDataSetChanged();
                        ((MainActivity)getActivity()).updateNotificationIcon(false);
                        // Also cancel system notifications.
                        NotificationManager nm = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                        if (nm != null) {
                            nm.cancelAll();
                        }
                        Toast.makeText(getContext(), "Notifications cleared", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to clear notifications: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
