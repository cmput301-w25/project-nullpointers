package com.hamidat.nullpointersapp.mainFragments;

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
 * Fragment for managing following.
 * Displays the current user's accepted following.
 * Tapping on a user in "My Following" opens their profile as an overlay using layout_search_profile.xml.
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

    private TextView tvCurrentUser;
    private ListView lvAccepted;

    private ArrayAdapter<User> acceptedAdapter;
    private ArrayList<User> acceptedList = new ArrayList<>();

    private String currentUsername;
    private String currentUserId;

    private FirestoreHelper firestoreHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the updated XML that does not include pending requests.
        return inflater.inflate(R.layout.fragment_following, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        tvCurrentUser = view.findViewById(R.id.tvCurrentUser);
        lvAccepted = view.findViewById(R.id.lvAccepted);

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
        lvAccepted.setAdapter(acceptedAdapter);

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

        // When an accepted user is tapped, open their profile overlay.
        lvAccepted.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view1, int position, long id) {
                User selectedUser = acceptedList.get(position);
                showUserProfile(selectedUser);
            }
        });
    }

    /**
     * Displays the selected user's profile as an overlay using layout_search_profile.xml.
     * The profile view covers the entire fragment and is brought to the front.
     *
     * @param user The selected user.
     */
    private void showUserProfile(User user) {
        ViewGroup root = (ViewGroup) getView();
        if (root == null) return;

        // Inflate the profile view from layout_search_profile.xml.
        View profileView = LayoutInflater.from(getContext()).inflate(R.layout.layout_search_profile, root, false);
        profileView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        // Set a high elevation so it appears on top.
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

        root.addView(profileView);
        profileView.bringToFront();
        profileView.invalidate();
    }
}
