package com.hamidat.nullpointersapp.mainFragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.hamidat.nullpointersapp.MainActivity;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreFollowing;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fragment for managing following and friend requests.
 * Displays pending friend requests and the list of users you are following.
 * Tapping on a user in "My Following" opens their profile using layout_search_profile.xml
 * as an overlay (instead of an AlertDialog) so that the user can unfollow (or follow) them.
 */
public class FollowingFragment extends Fragment {

    /**
     * Model representing a user.
     */
    public static class User {
        public String userId;
        public String username;

        public User(String userId, String username) {
            if (userId == null || username == null)
                throw new NullPointerException("userId and username cannot be null");
            this.userId = userId;
            this.username = username;
        }

        @Override
        public String toString() {
            return username;
        }

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

        public PendingRequest(String requestId, User sender) {
            if (requestId == null || sender == null) {
                throw new NullPointerException("requestId or sender cannot be null");
            }
            this.requestId = requestId;
            this.sender = sender;
        }

        @Override
        public String toString() {
            return sender.username;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof PendingRequest) {
                return this.requestId.equals(((PendingRequest) obj).requestId);
            }
            return false;
        }
    }

    private TextView tvCurrentUser;
    private ListView lvAccepted, lvPending;

    private ArrayAdapter<User> acceptedAdapter;
    private ArrayAdapter<PendingRequest> pendingAdapter;

    private ArrayList<User> acceptedList = new ArrayList<>();
    private ArrayList<PendingRequest> pendingList = new ArrayList<>();

    private String currentUsername;
    private String currentUserId;

    private FirestoreHelper firestoreHelper;

    // To track pending requests if needed.
    private String currentPendingRequestId = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Use the updated fragment_following.xml that no longer includes available users.
        return inflater.inflate(R.layout.fragment_following, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        tvCurrentUser = view.findViewById(R.id.tvCurrentUser);
        lvAccepted = view.findViewById(R.id.lvAccepted);
        lvPending = view.findViewById(R.id.lvPending);

        firestoreHelper = ((MainActivity) getActivity()).getFirestoreHelper();
        currentUserId = ((MainActivity) getActivity()).getCurrentUserId();

        // Fetch current user's username.
        firestoreHelper.getUser(currentUserId, new FirestoreHelper.FirestoreCallback() {
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
            @Override
            public void onFailure(Exception e) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            tvCurrentUser.setText("Current User: Unknown"));
                }
            }
        });

        acceptedAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, acceptedList);
        pendingAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, pendingList);

        lvAccepted.setAdapter(acceptedAdapter);
        lvPending.setAdapter(pendingAdapter);

        // Listen for changes to the current user's following list.
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(currentUserId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null || !snapshot.exists()) return;
                    ArrayList<String> followingIds = (ArrayList<String>) snapshot.get("following");
                    if (followingIds == null) {
                        followingIds = new ArrayList<>();
                    }
                    requireActivity().runOnUiThread(() -> {
                        acceptedList.clear();
                        acceptedAdapter.notifyDataSetChanged();
                    });
                    for (String followUserId : followingIds) {
                        firestoreHelper.getUser(followUserId, new FirestoreHelper.FirestoreCallback() {
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
                            @Override
                            public void onFailure(Exception e) { }
                        });
                    }
                });

        // Listen for incoming friend requests.
        firestoreHelper.listenForFriendRequests(currentUserId, new FirestoreFollowing.FollowingCallback() {
            @Override
            public void onSuccess(Object result) {
                if (!isAdded()) return;
                Map<String, Object> requestData = (Map<String, Object>) result;
                final String requestId = (String) requestData.get("requestId");
                final String fromUserId = (String) requestData.get("fromUserId");
                boolean alreadyPending = false;
                for (PendingRequest pr : pendingList) {
                    if (pr.sender.userId.equals(fromUserId)) {
                        alreadyPending = true;
                        break;
                    }
                }
                if (alreadyPending) return;
                firestoreHelper.getUser(fromUserId, new FirestoreHelper.FirestoreCallback() {
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
                    @Override
                    public void onFailure(Exception e) {
                        if (!isAdded()) return;
                        PendingRequest pr = new PendingRequest(requestId, new User(fromUserId, fromUserId));
                        pendingList.add(pr);
                        requireActivity().runOnUiThread(() -> pendingAdapter.notifyDataSetChanged());
                    }
                });
            }
            @Override
            public void onFailure(Exception e) { }
        });

        // When an accepted user is tapped, show their profile.
        lvAccepted.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view1, int position, long id) {
                User selectedUser = acceptedList.get(position);
                showUserProfile(selectedUser);
            }
        });

        // When a pending request is tapped, show the friend request dialog.
        lvPending.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view1, int position, long id) {
                PendingRequest pr = pendingList.get(position);
                showFriendRequestDialog(pr.sender.username, pr.requestId, pr.sender.userId);
            }
        });

        // Long-click on an accepted user to unfollow.
        lvAccepted.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view1, int position, long id) {
                User removedUser = acceptedList.get(position);
                firestoreHelper.removeFollowing(currentUserId, removedUser.userId, new FirestoreFollowing.FollowingCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Unfollowed " + removedUser.username, Toast.LENGTH_SHORT).show();
                                acceptedList.remove(removedUser);
                                acceptedAdapter.notifyDataSetChanged();
                            });
                        }
                    }
                    @Override
                    public void onFailure(Exception e) {
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "Error unfollowing: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    }
                });
                return true;
            }
        });
    }

    /**
     * Displays the selected user's profile using the layout_search_profile.xml layout.
     * The profile view is added as an overlay to the current fragment's root view.
     *
     * @param user The selected user.
     */
    private void showUserProfile(User user) {
        // Get the fragment's root view.
        ViewGroup root = (ViewGroup) getView();
        if (root == null) return;

        // Inflate the profile layout.
        View profileView = LayoutInflater.from(getContext()).inflate(R.layout.layout_search_profile, root, false);
        // Set layout parameters to cover the entire area.
        profileView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        // Optionally set a high elevation
        profileView.setElevation(100);

        // Bind profile view elements.
        TextView tvProfileUsername = profileView.findViewById(R.id.username_text);
        Button btnFollowUnfollow = profileView.findViewById(R.id.btnFollowUnfollow);
        ImageView ivBack = profileView.findViewById(R.id.ivBack);

        // Set the username.
        tvProfileUsername.setText(user.username);

        // Check if the current user is already following the selected user.
        firestoreHelper.getUser(currentUserId, new FirestoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                if (result instanceof Map) {
                    Map<String, Object> userData = (Map<String, Object>) result;
                    List<String> following = (List<String>) userData.get("following");
                    if (following != null && following.contains(user.userId)) {
                        btnFollowUnfollow.setText("Unfollow");
                    } else {
                        btnFollowUnfollow.setText("Follow");
                    }
                }
            }
            @Override
            public void onFailure(Exception e) {
                // Optionally handle error.
            }
        });

        // Set follow/unfollow button behavior.
        btnFollowUnfollow.setOnClickListener(v -> {
            String currentText = btnFollowUnfollow.getText().toString();
            FirestoreHelper helper = new FirestoreHelper();
            if (currentText.equalsIgnoreCase("Follow")) {
                helper.sendFriendRequest(currentUserId, user.userId, new FirestoreFollowing.FollowingCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        btnFollowUnfollow.setText("Pending");
                        Toast.makeText(getContext(), "Follow request sent", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (currentText.equalsIgnoreCase("Unfollow")) {
                helper.removeFollowing(currentUserId, user.userId, new FirestoreFollowing.FollowingCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        btnFollowUnfollow.setText("Follow");
                        Toast.makeText(getContext(), "Unfollowed", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Set the back button to remove the profile overlay.
        ivBack.setOnClickListener(v -> {
            root.removeView(profileView);
        });

        // Add the profile view as an overlay and bring it to the front.
        root.addView(profileView);
        profileView.bringToFront();
        profileView.invalidate();
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
        new AlertDialog.Builder(getActivity())
                .setTitle("Friend Request")
                .setMessage(senderUsername + " has sent you an add request!")
                .setPositiveButton("Accept", (dialog, which) -> {
                    firestoreHelper.acceptFriendRequest(requestId, new FirestoreFollowing.FollowingCallback() {
                        @Override
                        public void onSuccess(Object result) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "Friend request accepted", Toast.LENGTH_SHORT).show());
                            removePendingRequest(requestId);
                        }
                        @Override
                        public void onFailure(Exception e) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "Error accepting request: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton("Decline", (dialog, which) -> {
                    firestoreHelper.declineFriendRequest(requestId, new FirestoreFollowing.FollowingCallback() {
                        @Override
                        public void onSuccess(Object result) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "Friend request declined", Toast.LENGTH_SHORT).show());
                            removePendingRequest(requestId);
                        }
                        @Override
                        public void onFailure(Exception e) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "Error declining request: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
        requireActivity().runOnUiThread(() -> pendingAdapter.notifyDataSetChanged());
    }
}
