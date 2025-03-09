package com.hamidat.nullpointersapp.mainFragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.hamidat.nullpointersapp.utils.notificationUtils.NotificationHelper;

import java.util.ArrayList;
import java.util.Map;

/**
 * Fragment for managing following and friend requests.
 * It displays accepted friends, available users, and pending friend requests.
 */
public class FollowingFragment extends Fragment {

    /**
     * Model representing a user.
     */
    public static class User {
        public String userId;
        public String username;

        /**
         * Constructs a new User.
         *
         * @param userId   The unique identifier for the user.
         * @param username The username of the user.
         */
        public User(String userId, String username) {
            if (userId == null || username == null)
                throw new NullPointerException("userId and username cannot be null");
            this.userId = userId;
            this.username = username;
        }

        /**
         * Returns the username.
         *
         * @return The username.
         */
        @Override
        public String toString() {
            return username;
        }

        /**
         * Checks equality based on userId.
         *
         * @param obj The object to compare.
         * @return True if the userIds are equal, false otherwise.
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof User) {
                return this.userId.equals(((User) obj).userId);
            }
            return false;
        }
    }

    /**
     * Model representing a pending friend request.
     */
    public static class PendingRequest {
        public String requestId;
        public User sender;

        /**
         * Constructs a new PendingRequest.
         *
         * @param requestId The unique identifier for the friend request.
         * @param sender    The user who sent the request.
         */
        public PendingRequest(String requestId, User sender) {
            if (requestId == null || sender == null) {
                throw new NullPointerException("requestId or sender cannot be null");
            }
            this.requestId = requestId;
            this.sender = sender;
        }

        /**
         * Returns the sender's username.
         *
         * @return The sender's username.
         */
        @Override
        public String toString() {
            return sender.username;
        }

        /**
         * Checks equality based on requestId.
         *
         * @param obj The object to compare.
         * @return True if the requestIds are equal, false otherwise.
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof PendingRequest) {
                return this.requestId.equals(((PendingRequest) obj).requestId);
            }
            return false;
        }
    }

    private TextView tvCurrentUser;
    private ListView lvAccepted, lvAvailable, lvPending;

    private ArrayAdapter<User> acceptedAdapter;
    private ArrayAdapter<User> availableAdapter;
    private ArrayAdapter<PendingRequest> pendingAdapter;

    private ArrayList<User> acceptedList = new ArrayList<>();
    private ArrayList<User> availableList = new ArrayList<>();
    private ArrayList<PendingRequest> pendingList = new ArrayList<>();

    private String currentUsername;
    private String currentUserId;

    // Track the currently selected pending request.
    private String currentPendingRequestId = null;
    private User currentPendingSender = null;

    private FirestoreHelper firestoreHelper;

    /**
     * Inflates the fragment view.
     *
     * @param inflater           LayoutInflater to inflate the view.
     * @param container          Parent view group.
     * @param savedInstanceState Saved instance state bundle.
     * @return The inflated view.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_following, container, false);
    }

    /**
     * Called when the view has been created. Initializes UI components and sets up listeners.
     *
     * @param view               The fragment's root view.
     * @param savedInstanceState Saved instance state bundle.
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        tvCurrentUser = view.findViewById(R.id.tvCurrentUser);
        lvAccepted = view.findViewById(R.id.lvAccepted);
        lvAvailable = view.findViewById(R.id.lvAvailable);
        lvPending = view.findViewById(R.id.lvPending);

        firestoreHelper = ((MainActivity) getActivity()).getFirestoreHelper();
        currentUserId = ((MainActivity) getActivity()).getCurrentUserId();

        // Fetch current user's username.
        firestoreHelper.getUser(currentUserId, new FirestoreHelper.FirestoreCallback() {
            /**
             * Called when the username is successfully fetched.
             *
             * @param result A map containing user data.
             */
            @Override
            public void onSuccess(Object result) {
                if (isAdded() && result instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> userData = (Map<String, Object>) result;
                    currentUsername = (String) userData.get("username");
                    requireActivity().runOnUiThread(() ->
                            tvCurrentUser.setText("Current User: " + currentUsername));
                }
            }
            /**
             * Called when there is a failure in fetching user data.
             *
             * @param e The exception encountered.
             */
            @Override
            public void onFailure(Exception e) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            tvCurrentUser.setText("Current User: Unknown"));
                }
            }
        });

        acceptedAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, acceptedList);
        availableAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, availableList);
        pendingAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, pendingList);

        lvAccepted.setAdapter(acceptedAdapter);
        lvAvailable.setAdapter(availableAdapter);
        lvPending.setAdapter(pendingAdapter);

        // Populate available users.
        firestoreHelper.getAllUsers(new FirestoreHelper.FirestoreCallback() {
            /**
             * Called when the list of users is successfully fetched.
             *
             * @param result A list of maps containing user data.
             */
            @Override
            public void onSuccess(Object result) {
                if (!isAdded()) return;
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
            /**
             * Called when there is a failure fetching users.
             *
             * @param e The exception encountered.
             */
            @Override
            public void onFailure(Exception e) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Error fetching users: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }
        });

        // Send friend request when an available user is tapped.
        lvAvailable.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * Handles tap events on available users.
             *
             * @param parent   The AdapterView where the click happened.
             * @param view1    The view within the AdapterView that was clicked.
             * @param position The position of the view in the adapter.
             * @param id       The row id of the item.
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view1, int position, long id) {
                User selectedUser = availableList.get(position);
                firestoreHelper.sendFriendRequest(currentUserId, selectedUser.userId, new FirestoreFollowing.FollowingCallback() {
                    /**
                     * Called when the friend request is successfully sent.
                     *
                     * @param result The result of the operation.
                     */
                    @Override
                    public void onSuccess(Object result) {
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "Friend request sent to " + selectedUser.username, Toast.LENGTH_SHORT).show());
                        }
                    }
                    /**
                     * Called when there is an error sending the friend request.
                     *
                     * @param e The exception encountered.
                     */
                    @Override
                    public void onFailure(Exception e) {
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "Error sending request: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    }
                });
            }
        });

        // Listen for changes to the current user's following list.
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(currentUserId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null || !snapshot.exists()) return;
                    ArrayList<String> followingIds = (ArrayList<String>) snapshot.get("following");
                    if (followingIds == null) {
                        followingIds = new ArrayList<>();
                    }
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            acceptedList.clear();
                            acceptedAdapter.notifyDataSetChanged();
                        });
                    }
                    for (String followUserId : followingIds) {
                        firestoreHelper.getUser(followUserId, new FirestoreHelper.FirestoreCallback() {
                            /**
                             * Called when a followed user's data is successfully fetched.
                             *
                             * @param result A map containing user data.
                             */
                            @Override
                            public void onSuccess(Object result) {
                                if (!isAdded()) return;
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
                            /**
                             * Called when there is an error fetching the followed user's data.
                             *
                             * @param e The exception encountered.
                             */
                            @Override
                            public void onFailure(Exception e) { }
                        });
                    }
                    refreshAvailableUsers();
                });

        // Listen for incoming friend requests.
        firestoreHelper.listenForFriendRequests(currentUserId, new FirestoreFollowing.FollowingCallback() {
            /**
             * Called when a friend request is received.
             *
             * @param result A map containing the friend request data.
             */
            @Override
            public void onSuccess(Object result) {
                if (!isAdded()) return;
                Map<String, Object> requestData = (Map<String, Object>) result;
                final String requestId = (String) requestData.get("requestId");
                final String fromUserId = (String) requestData.get("fromUserId");
                // Check if request already exists.
                boolean alreadyPending = false;
                for (PendingRequest pr : pendingList) {
                    if (pr.sender.userId.equals(fromUserId)) {
                        alreadyPending = true;
                        break;
                    }
                }
                if (alreadyPending) return;
                // Fetch sender's username.
                firestoreHelper.getUser(fromUserId, new FirestoreHelper.FirestoreCallback() {
                    /**
                     * Called when sender's data is successfully fetched.
                     *
                     * @param result A map containing sender's data.
                     */
                    @Override
                    public void onSuccess(Object result) {
                        if (!isAdded()) return;
                        String senderUsername = fromUserId;
                        if (result instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> userData = (Map<String, Object>) result;
                            if (userData.get("username") != null) {
                                senderUsername = (String) userData.get("username");
                            }
                        }
                        PendingRequest pr = new PendingRequest(requestId, new User(fromUserId, senderUsername));
                        pendingList.add(pr);
                        requireActivity().runOnUiThread(() -> pendingAdapter.notifyDataSetChanged());
                    }
                    /**
                     * Called when there is an error fetching sender's data.
                     *
                     * @param e The exception encountered.
                     */
                    @Override
                    public void onFailure(Exception e) {
                        if (!isAdded()) return;
                        PendingRequest pr = new PendingRequest(requestId, new User(fromUserId, fromUserId));
                        pendingList.add(pr);
                        requireActivity().runOnUiThread(() -> pendingAdapter.notifyDataSetChanged());
                    }
                });
            }
            /**
             * Called when there is an error listening for friend requests.
             *
             * @param e The exception encountered.
             */
            @Override
            public void onFailure(Exception e) { }
        });

        // Show dialog when a pending request is tapped.
        lvPending.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * Handles tap on a pending friend request.
             *
             * @param parent   The AdapterView where the click happened.
             * @param view1    The view that was clicked.
             * @param position The position of the item in the adapter.
             * @param id       The row id of the item.
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view1, int position, long id) {
                PendingRequest pr = pendingList.get(position);
                showFriendRequestDialog(pr.sender.username, pr.requestId, pr.sender.userId);
            }
        });

        // Handle long-click on an accepted user to unfollow.
        lvAccepted.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            /**
             * Handles long-click to remove a following relationship.
             *
             * @param parent   The AdapterView where the click happened.
             * @param view1    The view that was clicked.
             * @param position The position of the item in the adapter.
             * @param id       The row id of the item.
             * @return true if the long-click was handled.
             */
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view1, int position, long id) {
                User removedUser = acceptedList.get(position);
                firestoreHelper.removeFollowing(currentUserId, removedUser.userId, new FirestoreFollowing.FollowingCallback() {
                    /**
                     * Called when unfollowing is successful.
                     *
                     * @param result The result of the operation.
                     */
                    @Override
                    public void onSuccess(Object result) {
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Unfollowed " + removedUser.username, Toast.LENGTH_SHORT).show();
                                acceptedList.remove(removedUser);
                                acceptedAdapter.notifyDataSetChanged();
                                refreshAvailableUsers();
                            });
                        }
                    }
                    /**
                     * Called when there is an error unfollowing.
                     *
                     * @param e The exception encountered.
                     */
                    @Override
                    public void onFailure(Exception e) {
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "Error unfollowing: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    }
                });
                return true;
            }
        });
    }

    /**
     * Refreshes the available users list.
     */
    private void refreshAvailableUsers() {
        firestoreHelper.getAllUsers(new FirestoreHelper.FirestoreCallback() {
            /**
             * Called when the user list is successfully fetched.
             *
             * @param result A list of maps containing user data.
             */
            @Override
            public void onSuccess(Object result) {
                ArrayList<Map<String, Object>> users = (ArrayList<Map<String, Object>>) result;
                ArrayList<User> updatedAvailable = new ArrayList<>();
                ArrayList<String> acceptedIds = new ArrayList<>();
                for (User u : acceptedList) {
                    acceptedIds.add(u.userId);
                }
                for (Map<String, Object> userData : users) {
                    String username = (String) userData.get("username");
                    String userId = (String) userData.get("userId");
                    if (userId != null && !userId.equals(currentUserId) && !acceptedIds.contains(userId)) {
                        updatedAvailable.add(new User(userId, username));
                    }
                }
                availableList.clear();
                availableList.addAll(updatedAvailable);
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> availableAdapter.notifyDataSetChanged());
                }
            }
            /**
             * Called when there is an error refreshing the available users.
             *
             * @param e The exception encountered.
             */
            @Override
            public void onFailure(Exception e) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Error refreshing available users: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    /**
     * Displays an AlertDialog for accepting or declining a friend request.
     *
     * @param senderUsername The username of the request sender.
     * @param requestId      The unique request identifier.
     * @param fromUserId     The sender's user ID.
     */
    private void showFriendRequestDialog(String senderUsername, String requestId, String fromUserId) {
        if (getActivity() == null) return;
        currentPendingRequestId = requestId;
        currentPendingSender = new User(fromUserId, senderUsername);
        new AlertDialog.Builder(getActivity())
                .setTitle("Friend Request")
                .setMessage(senderUsername + " has sent you an add request!")
                .setPositiveButton("Accept", (dialog, which) -> {
                    firestoreHelper.acceptFriendRequest(requestId, new FirestoreFollowing.FollowingCallback() {
                        /**
                         * Called when the friend request is successfully accepted.
                         *
                         * @param result The result of the operation.
                         */
                        @Override
                        public void onSuccess(Object result) {
                            if (isAdded()) {
                                requireActivity().runOnUiThread(() ->
                                        Toast.makeText(getContext(), "Friend request accepted", Toast.LENGTH_SHORT).show());
                            }
                            removePendingRequest(requestId);
                        }
                        /**
                         * Called when there is an error accepting the friend request.
                         *
                         * @param e The exception encountered.
                         */
                        @Override
                        public void onFailure(Exception e) {
                            if (isAdded()) {
                                requireActivity().runOnUiThread(() ->
                                        Toast.makeText(getContext(), "Error accepting request: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            }
                        }
                    });
                })
                .setNegativeButton("Decline", (dialog, which) -> {
                    firestoreHelper.declineFriendRequest(requestId, new FirestoreFollowing.FollowingCallback() {
                        /**
                         * Called when the friend request is successfully declined.
                         *
                         * @param result The result of the operation.
                         */
                        @Override
                        public void onSuccess(Object result) {
                            if (isAdded()) {
                                requireActivity().runOnUiThread(() ->
                                        Toast.makeText(getContext(), "Friend request declined", Toast.LENGTH_SHORT).show());
                            }
                            removePendingRequest(requestId);
                        }
                        /**
                         * Called when there is an error declining the friend request.
                         *
                         * @param e The exception encountered.
                         */
                        @Override
                        public void onFailure(Exception e) {
                            if (isAdded()) {
                                requireActivity().runOnUiThread(() ->
                                        Toast.makeText(getContext(), "Error declining request: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            }
                        }
                    });
                })
                .show();
    }

    /**
     * Removes a pending friend request from the list.
     *
     * @param requestId The unique identifier of the friend request.
     */
    private void removePendingRequest(String requestId) {
        for (int i = 0; i < pendingList.size(); i++) {
            if (pendingList.get(i).requestId.equals(requestId)) {
                pendingList.remove(i);
                break;
            }
        }
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> pendingAdapter.notifyDataSetChanged());
        }
    }
}
