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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.hamidat.nullpointersapp.MainActivity;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreFollowing;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;

import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Map;

public class FollowingFragment extends Fragment {

    private TextView tvCurrentUser;
    private ListView lvAccepted, lvAvailable;
    private LinearLayout llPendingRequest;
    private TextView tvPendingMessage;
    private Button btnAccept, btnDecline;

    private ArrayAdapter<String> acceptedAdapter;
    private ArrayAdapter<String> availableAdapter;

    private ArrayList<String> acceptedList = new ArrayList<>();
    private ArrayList<String> availableList = new ArrayList<>();

    // For pending friend request
    private String pendingRequestDocId = null;
    private String pendingRequestUsername = null;

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

        // Get FirestoreHelper from MainActivity
        firestoreHelper = ((MainActivity) getActivity()).getFirestoreHelper();
        // Assume currentUserId and currentUsername are available from MainActivity
        currentUserId = ((MainActivity) getActivity()).getCurrentUserId();
        if (currentUserId == null) currentUserId = "user1"; // fallback for testing
        if (currentUsername == null) currentUsername = "CurrentUser";

        tvCurrentUser.setText("Current User: " + currentUsername);

        // Initialize adapters
        acceptedAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, acceptedList);
        availableAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, availableList);
        lvAccepted.setAdapter(acceptedAdapter);
        lvAvailable.setAdapter(availableAdapter);

        // Fetch all users from Firestore
        firestoreHelper.getUser(currentUserId, new FirestoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                if (result instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> userData = (Map<String, Object>) result;
                    currentUsername = (String) userData.get("username");
                    requireActivity().runOnUiThread(() -> {
                        tvCurrentUser.setText("Current User: " + currentUsername);
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Error fetching users: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

        // Fetch all users from Firestore
        firestoreHelper.getAllUsers(new FirestoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                // result is an ArrayList<Map<String, Object>>
                ArrayList<Map<String, Object>> users = (ArrayList<Map<String, Object>>) result;
                availableList.clear();
                for (Map<String, Object> userData : users) {
                    String username = (String) userData.get("username");
                    String userId = (String) userData.get("userId");
                    // Log each user for debugging
                    System.out.println("Found user: " + username + " (" + userId + ")");
                    // Uncomment the following check once you're sure currentUserId is valid:
                    if (userId != null && !userId.equals(currentUserId)) {
                        availableList.add(username);
                    }
                    // For debugging, you could also try:
                    // availableList.add(username);
                }
                requireActivity().runOnUiThread(() -> availableAdapter.notifyDataSetChanged());
            }

            @Override
            public void onFailure(Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Error fetching users: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });


        // When an available user is tapped, send a friend request
        lvAvailable.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedUsername = availableList.get(position);
            // Look up the user by username to get their userId
            firestoreHelper.getUserByUsername(selectedUsername, new FirestoreHelper.FirestoreCallback() {
                @Override
                public void onSuccess(Object result) {
                    if (result instanceof Map) {
                        Map<String, Object> userData = (Map<String, Object>) result;
                        String targetUserId = (String) userData.get("userId");
                        firestoreHelper.sendFriendRequest(currentUserId, targetUserId, new FirestoreFollowing.FollowingCallback() {
                            @Override
                            public void onSuccess(Object result) {
                                requireActivity().runOnUiThread(() ->
                                        Toast.makeText(requireContext(), "Friend request sent to " + selectedUsername, Toast.LENGTH_SHORT).show());
                            }

                            @Override
                            public void onFailure(Exception e) {
                                requireActivity().runOnUiThread(() ->
                                        Toast.makeText(requireContext(), "Error sending request: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            }
                        });
                    }
                }
                @Override
                public void onFailure(Exception e) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Error finding user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });
        });

        // Listen for incoming friend requests for the current user in real time
        firestoreHelper.listenForFriendRequests(currentUserId, new FirestoreFollowing.FollowingCallback() {
            @Override
            public void onSuccess(Object result) {
                // Expecting a Map with friend request details
                Map<String, Object> requestData = (Map<String, Object>) result;
                pendingRequestDocId = (String) requestData.get("requestId");
                String fromUserId = (String) requestData.get("fromUserId");
                // For simplicity, we display the fromUserId; ideally, lookup the username
                pendingRequestUsername = fromUserId;
                requireActivity().runOnUiThread(() -> {
                    tvPendingMessage.setText("Friend request from " + pendingRequestUsername);
                    llPendingRequest.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onFailure(Exception e) {
                // Optionally handle errors
            }
        });

        // Accept friend request (this action occurs on the receiver's device)
        btnAccept.setOnClickListener(v -> {
            if (pendingRequestDocId != null) {
                firestoreHelper.acceptFriendRequest(pendingRequestDocId, new FirestoreFollowing.FollowingCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        acceptedList.add(pendingRequestUsername);
                        availableList.remove(pendingRequestUsername);
                        requireActivity().runOnUiThread(() -> {
                            acceptedAdapter.notifyDataSetChanged();
                            availableAdapter.notifyDataSetChanged();
                            llPendingRequest.setVisibility(View.GONE);
                            Toast.makeText(requireContext(), "Friend request accepted", Toast.LENGTH_SHORT).show();
                        });
                        pendingRequestDocId = null;
                        pendingRequestUsername = null;
                    }

                    @Override
                    public void onFailure(Exception e) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Error accepting request: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                });
            }
        });

        // Decline friend request
        btnDecline.setOnClickListener(v -> {
            if (pendingRequestDocId != null) {
                firestoreHelper.declineFriendRequest(pendingRequestDocId, new FirestoreFollowing.FollowingCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        requireActivity().runOnUiThread(() -> {
                            llPendingRequest.setVisibility(View.GONE);
                            Toast.makeText(requireContext(), "Friend request declined", Toast.LENGTH_SHORT).show();
                        });
                        pendingRequestDocId = null;
                        pendingRequestUsername = null;
                    }

                    @Override
                    public void onFailure(Exception e) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Error declining request: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                });
            }
        });

        // Long-click an accepted user to unfollow
        lvAccepted.setOnItemLongClickListener((parent, view12, position, id) -> {
            String removedUser = acceptedList.get(position);
            firestoreHelper.removeFollowing(currentUserId, removedUser, new FirestoreFollowing.FollowingCallback() {
                @Override
                public void onSuccess(Object result) {
                    acceptedList.remove(position);
                    availableList.add(removedUser);
                    requireActivity().runOnUiThread(() -> {
                        acceptedAdapter.notifyDataSetChanged();
                        availableAdapter.notifyDataSetChanged();
                        Toast.makeText(requireContext(), "Unfollowed " + removedUser, Toast.LENGTH_SHORT).show();
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
}
