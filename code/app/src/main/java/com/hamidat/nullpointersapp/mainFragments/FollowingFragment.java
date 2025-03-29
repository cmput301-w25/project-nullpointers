/**
 * FollowingFragment.java
 *
 * Handles the UI and logic for managing followed users.
 * Includes real-time updates to following list and overlay profile view of selected users.
 *
 * <p><b>Outstanding issues:</b> None.</p>
 */

package com.hamidat.nullpointersapp.mainFragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.hamidat.nullpointersapp.MainActivity;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.models.MoodAdapter;
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
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            acceptedList.clear();
                            acceptedAdapter.notifyDataSetChanged();
                        });
                    }
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
        // Get the fragment's root view.
        ViewGroup root = (ViewGroup) getView();
        if (root == null) return;

        // Inflate the profile view from layout_search_profile.xml.
        View profileView = LayoutInflater.from(getContext())
                .inflate(R.layout.layout_search_profile, root, false);
        profileView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        profileView.setElevation(100); // Ensure it appears on top

        // Bind profile view elements.
        TextView tvProfileUsername = profileView.findViewById(R.id.username_text);
        Button btnFollowUnfollow = profileView.findViewById(R.id.btnFollowUnfollow);
        ImageView ivBack = profileView.findViewById(R.id.ivBack);
        RecyclerView rvMoodEvents = profileView.findViewById(R.id.rvMoodEvents);
        TextView tvFriendCount = profileView.findViewById(R.id.tvFriendCount);
        TextView statusBubble = profileView.findViewById(R.id.user_status_bubble);
        ImageView ivProfilePicture = profileView.findViewById(R.id.profile_icon);

        // Set the username.
        tvProfileUsername.setText(user.username);

        // Initially hide mood events.
        rvMoodEvents.setVisibility(View.GONE);

        // Load profile picture
        firestoreHelper.getUser(user.userId, new FirestoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                if (result instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> userData = (Map<String, Object>) result;
                    String profilePicBase64 = (String) userData.get("profilePicture");
                    if (profilePicBase64 != null && !profilePicBase64.isEmpty()) {
                        try {
                            byte[] decodedBytes = Base64.decode(profilePicBase64, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                            ivProfilePicture.post(() -> ivProfilePicture.setImageBitmap(bitmap));
                        } catch (Exception e) {
                            ivProfilePicture.post(() -> ivProfilePicture.setImageResource(R.drawable.default_user_icon));
                        }
                    } else {
                        ivProfilePicture.post(() -> ivProfilePicture.setImageResource(R.drawable.default_user_icon));
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                ivProfilePicture.post(() -> ivProfilePicture.setImageResource(R.drawable.default_user_icon));
            }
        });

        // Check if the current user follows the target user.
        firestoreHelper.getUser(currentUserId, new FirestoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                if (result instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> currentUserData = (Map<String, Object>) result;
                    List<String> following = (List<String>) currentUserData.get("following");
                    if (following != null && following.contains(user.userId)) {
                        btnFollowUnfollow.setText("Unfollow");
                        rvMoodEvents.setVisibility(View.VISIBLE);
                        loadRecentMoodEvents(user, rvMoodEvents);

                        // Also load the target user's status for display.
                        firestoreHelper.getUser(user.userId, new FirestoreHelper.FirestoreCallback() {
                            @Override
                            public void onSuccess(Object result) {
                                if (result instanceof Map) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> targetUserData = (Map<String, Object>) result;
                                    String targetStatus = (String) targetUserData.get("status");
                                    if (targetStatus != null && !targetStatus.trim().isEmpty()) {
                                        statusBubble.setText(targetStatus);
                                        statusBubble.setVisibility(View.VISIBLE);
                                    } else {
                                        statusBubble.setVisibility(View.GONE);
                                    }
                                }
                            }
                            @Override
                            public void onFailure(Exception e) {
                                statusBubble.setVisibility(View.GONE);
                            }
                        });

                        // Get their friend count to display.
                        firestoreHelper.getUser(user.userId, new FirestoreHelper.FirestoreCallback() {
                            @Override
                            public void onSuccess(Object result) {
                                if (result instanceof Map) {
                                    Map<String, Object> theirData = (Map<String, Object>) result;
                                    List<String> theirFollowing = (List<String>) theirData.get("following");
                                    int friendCount = theirFollowing != null ? theirFollowing.size() : 0;
                                    tvFriendCount.setText("Friends: " + friendCount);
                                    tvFriendCount.setVisibility(View.VISIBLE);
                                }
                            }
                            @Override
                            public void onFailure(Exception e) {
                                tvFriendCount.setVisibility(View.GONE);
                            }
                        });

                    } else {
                        // If not following, hide target's status and mood events.
                        statusBubble.setVisibility(View.GONE);
                        btnFollowUnfollow.setText("Follow");
                        rvMoodEvents.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void onFailure(Exception e) { }
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
                        // Hide mood events when unfollowing.
                        rvMoodEvents.setVisibility(View.GONE);
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
        ivBack.setOnClickListener(v -> root.removeView(profileView));

        // Add the profile view as an overlay.
        root.addView(profileView);
        profileView.bringToFront();
        profileView.invalidate();
    }

    /**
     * Queries Firestore for the three most recent mood events of the selected user
     * and binds them to the given RecyclerView using MoodAdapter.
     */
    private void loadRecentMoodEvents(User user, RecyclerView rvMoodEvents) {
        List<Mood> moodList = new ArrayList<>();
        MoodAdapter moodAdapter = new MoodAdapter(moodList, currentUserId,(AppCompatActivity) getActivity());

        rvMoodEvents.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rvMoodEvents.setAdapter(moodAdapter);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("moods")
                .whereEqualTo("userId", user.userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(3)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    moodList.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Mood mood = doc.toObject(Mood.class);
                        if (mood != null) {
                            moodList.add(mood);
                        }
                    }
                    requireActivity().runOnUiThread(() -> moodAdapter.notifyDataSetChanged());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading mood events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
