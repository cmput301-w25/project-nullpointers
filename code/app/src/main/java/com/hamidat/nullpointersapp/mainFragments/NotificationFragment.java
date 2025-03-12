package com.hamidat.nullpointersapp.mainFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreFollowing;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fragment to display pending friend requests.
 * Each item shows "Username has sent you a friend request" with Decline and Accept buttons.
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
        public NotificationItem(String requestId, String fromUserId, String username) {
            this.requestId = requestId;
            this.fromUserId = fromUserId;
            this.username = username;
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
        // Assume currentUserId is passed via Activity's intent
        currentUserId = getActivity().getIntent().getStringExtra("USER_ID");

        // Listen for pending friend requests.
        firestoreHelper.listenForFriendRequests(currentUserId, new FirestoreFollowing.FollowingCallback() {
            @Override
            public void onSuccess(Object result) {
                if (result instanceof Map) {
                    Map<String, Object> requestData = (Map<String, Object>) result;
                    String requestId = (String) requestData.get("requestId");
                    String fromUserId = (String) requestData.get("fromUserId");
                    // Get the sender's username.
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
                            // Add the notification item if not already added.
                            boolean exists = false;
                            for (NotificationItem item : notifications) {
                                if (item.requestId.equals(requestId)) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                notifications.add(new NotificationItem(requestId, fromUserId, username));
                                requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                            }
                        }
                        @Override
                        public void onFailure(Exception e) {
                            // Handle error if needed.
                        }
                    });
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
            Button btnDecline, btnAccept;
            public NotificationViewHolder(@NonNull View itemView) {
                super(itemView);
                tvMessage = itemView.findViewById(R.id.tvNotificationMessage);
                btnDecline = itemView.findViewById(R.id.btnDecline);
                btnAccept = itemView.findViewById(R.id.btnAccept);
            }
            void bind(NotificationItem item) {
                tvMessage.setText(item.username + " has sent you a friend request");
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
        requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
    }
}
