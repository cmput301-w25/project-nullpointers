package com.hamidat.nullpointersapp.mainFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.hamidat.nullpointersapp.MainActivity;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreFollowing;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;

import java.util.ArrayList;
import java.util.Map;

public class FollowingFragment extends Fragment {

    // Simple User model storing userId and username.
    public static class User {
        public String userId;
        public String username;

        public User(String userId, String username) {
            this.userId = userId;
            this.username = username;
        }

        @Override
        public String toString() {
            return username; // For display in the list.
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof User) {
                return this.userId.equals(((User) obj).userId);
            }
            return false;
        }
    }

    private TextView tvCurrentUser;
    private ListView lvAccepted, lvAvailable;
    private LinearLayout llPendingRequest;
    private TextView tvPendingMessage;
    private Button btnAccept, btnDecline;

    private ArrayAdapter<User> acceptedAdapter;
    private ArrayAdapter<User> availableAdapter;

    private ArrayList<User> acceptedList = new ArrayList<>();
    private ArrayList<User> availableList = new ArrayList<>();

    // For pending friend request (received by the current user)
    private String pendingRequestDocId = null;
    private User pendingRequestUser = null;

    private String currentUsername;
    private String currentUserId;

    private FirestoreHelper firestoreHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_following, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        tvCurrentUser = view.findViewById(R.id.tvCurrentUser);
        lvAccepted = view.findViewById(R.id.lvAccepted);
        lvAvailable = view.findViewById(R.id.lvAvailable);
        llPendingRequest = view.findViewById(R.id.llPendingRequest);
        tvPendingMessage = view.findViewById(R.id.tvPendingMessage);
        btnAccept = view.findViewById(R.id.btnAccept);
        btnDecline = view.findViewById(R.id.btnDecline);

        // Get FirestoreHelper and current user id from MainActivity.
        firestoreHelper = ((MainActivity) getActivity()).getFirestoreHelper();
        currentUserId = ((MainActivity) getActivity()).getCurrentUserId();

        // Fetch current username (using same logic as in ProfileFragment)
        firestoreHelper.getUser(currentUserId, new FirestoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                if (result instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> userData = (Map<String, Object>) result;
                    currentUsername = (String) userData.get("username");
                    requireActivity().runOnUiThread(() ->
                            tvCurrentUser.setText("Current User: " + currentUsername));
                }
            }
            @Override
            public void onFailure(Exception e) {
                requireActivity().runOnUiThread(() ->
                        tvCurrentUser.setText("Current User: Unknown"));
            }
        });

        // Initialize adapters using our User model.
        acceptedAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, acceptedList);
        availableAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, availableList);
        lvAccepted.setAdapter(acceptedAdapter);
        lvAvailable.setAdapter(availableAdapter);

        // Fetch all users from Firestore and populate availableList (excluding current user).
        firestoreHelper.getAllUsers(new FirestoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                ArrayList<Map<String, Object>> users = (ArrayList<Map<String, Object>>) result;
                availableList.clear();
                for (Map<String, Object> userData : users) {
                    String username = (String) userData.get("username");
                    String userId = (String) userData.get("userId");
                    if (userId != null && !userId.equals(currentUserId)) {
                        availableList.add(new User(userId, username));
                    }
                }
                requireActivity().runOnUiThread(() -> availableAdapter.notifyDataSetChanged());
            }
            @Override
            public void onFailure(Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Error fetching users: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

        // When an available user is tapped, send a friend request and remove them from available list.
        lvAvailable.setOnItemClickListener((parent, view1, position, id) -> {
            User selectedUser = availableList.get(position);
            firestoreHelper.sendFriendRequest(currentUserId, selectedUser.userId, new FirestoreFollowing.FollowingCallback() {
                @Override
                public void onSuccess(Object result) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Friend request sent to " + selectedUser.username, Toast.LENGTH_SHORT).show();
                        // Remove the user from available list so they don't show up again.
                        availableList.remove(selectedUser);
                        availableAdapter.notifyDataSetChanged();
                    });
                }
                @Override
                public void onFailure(Exception e) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Error sending request: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });
        });

        // Listen for real-time updates to the current user's following list.
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(currentUserId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null || !snapshot.exists()) return;
                    ArrayList<String> followingIds = (ArrayList<String>) snapshot.get("following");
                    if (followingIds == null) followingIds = new ArrayList<>();

                    requireActivity().runOnUiThread(() -> {
                        acceptedList.clear();
                        acceptedAdapter.notifyDataSetChanged();
                    });

                    for (String followUserId : followingIds) {
                        firestoreHelper.getUser(followUserId, new FirestoreHelper.FirestoreCallback() {
                            @Override
                            public void onSuccess(Object result) {
                                if (result instanceof Map) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> data = (Map<String, Object>) result;
                                    String username = (String) data.get("username");
                                    User user = new User(followUserId, username);
                                    requireActivity().runOnUiThread(() -> {
                                        if (!acceptedList.contains(user)) {
                                            acceptedList.add(user);
                                            acceptedAdapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }
                            @Override
                            public void onFailure(Exception e) { }
                        });
                    }
                    // After updating acceptedList, refresh available users.
                    refreshAvailableUsers();
                });

        // Listen for incoming friend requests for the current user in real time.
        firestoreHelper.listenForFriendRequests(currentUserId, new FirestoreFollowing.FollowingCallback() {
            @Override
            public void onSuccess(Object result) {
                Map<String, Object> requestData = (Map<String, Object>) result;
                pendingRequestDocId = (String) requestData.get("requestId");
                String fromUserId = (String) requestData.get("fromUserId");
                firestoreHelper.getUser(fromUserId, new FirestoreHelper.FirestoreCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        if (result instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> userData = (Map<String, Object>) result;
                            String senderUsername = (String) userData.get("username");
                            pendingRequestUser = new User(fromUserId, senderUsername);
                            requireActivity().runOnUiThread(() -> {
                                tvPendingMessage.setText("Friend request from " + pendingRequestUser.username);
                                llPendingRequest.setVisibility(View.VISIBLE);
                            });
                        }
                    }
                    @Override
                    public void onFailure(Exception e) {
                        requireActivity().runOnUiThread(() -> {
                            tvPendingMessage.setText("Friend request from " + fromUserId);
                            llPendingRequest.setVisibility(View.VISIBLE);
                        });
                    }
                });
            }
            @Override
            public void onFailure(Exception e) { }
        });

        // Accept friend request: update both users' following lists.
        btnAccept.setOnClickListener(v -> {
            if (pendingRequestDocId != null && pendingRequestUser != null) {
                firestoreHelper.acceptFriendRequest(pendingRequestDocId, new FirestoreFollowing.FollowingCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Friend request accepted", Toast.LENGTH_SHORT).show();
                            llPendingRequest.setVisibility(View.GONE);
                        });
                        pendingRequestDocId = null;
                        pendingRequestUser = null;
                    }
                    @Override
                    public void onFailure(Exception e) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Error accepting request: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                });
            }
        });

        // Decline friend request.
        btnDecline.setOnClickListener(v -> {
            if (pendingRequestDocId != null) {
                firestoreHelper.declineFriendRequest(pendingRequestDocId, new FirestoreFollowing.FollowingCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Friend request declined", Toast.LENGTH_SHORT).show();
                            llPendingRequest.setVisibility(View.GONE);
                        });
                        pendingRequestDocId = null;
                        pendingRequestUser = null;
                    }
                    @Override
                    public void onFailure(Exception e) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Error declining request: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                });
            }
        });

        // Long-click on an accepted user to unfollow (remove relationship from both sides).
        lvAccepted.setOnItemLongClickListener((parent, view12, position, id) -> {
            User removedUser = acceptedList.get(position);
            firestoreHelper.removeFollowing(currentUserId, removedUser.userId, new FirestoreFollowing.FollowingCallback() {
                @Override
                public void onSuccess(Object result) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Unfollowed " + removedUser.username, Toast.LENGTH_SHORT).show();
                        refreshAvailableUsers();
                    });
                }
                @Override
                public void onFailure(Exception e) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Error unfollowing: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });
            return true;
        });
    }

    // Helper method to refresh the available users list.
    private void refreshAvailableUsers() {
        firestoreHelper.getAllUsers(new FirestoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                ArrayList<Map<String, Object>> users = (ArrayList<Map<String, Object>>) result;
                // Get the list of pending outgoing requests.
                firestoreHelper.getOutgoingFriendRequests(currentUserId, new FirestoreFollowing.FollowingCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        ArrayList<String> pendingOutgoing = (ArrayList<String>) result; // user IDs with pending requests
                        ArrayList<User> updatedAvailable = new ArrayList<>();
                        ArrayList<String> acceptedIds = new ArrayList<>();
                        for (User u : acceptedList) {
                            acceptedIds.add(u.userId);
                        }
                        for (Map<String, Object> userData : users) {
                            String username = (String) userData.get("username");
                            String userId = (String) userData.get("userId");
                            // Exclude the current user, accepted users, and those with pending outgoing requests.
                            if (userId != null
                                    && !userId.equals(currentUserId)
                                    && !acceptedIds.contains(userId)
                                    && !pendingOutgoing.contains(userId)) {
                                updatedAvailable.add(new User(userId, username));
                            }
                        }
                        availableList.clear();
                        availableList.addAll(updatedAvailable);
                        requireActivity().runOnUiThread(() -> availableAdapter.notifyDataSetChanged());
                    }
                    @Override
                    public void onFailure(Exception e) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Error fetching pending requests: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                });
            }
            @Override
            public void onFailure(Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Error refreshing available users: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

}
