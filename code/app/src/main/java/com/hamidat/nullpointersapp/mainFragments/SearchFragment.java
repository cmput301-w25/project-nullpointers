package com.hamidat.nullpointersapp.mainFragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hamidat.nullpointersapp.MainActivity;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.models.MoodAdapter;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreFollowing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchFragment extends Fragment {
    private FirestoreHelper firestoreHelper;
    private EditText etSearch;
    private RecyclerView rvResults;
    private SearchAdapter adapter;
    private FirebaseFirestore firestore;
    private List<User> userList = new ArrayList<>();

    // Layouts for search main view and profile view
    private View searchMainLayout;
    private View searchProfileView;
    private TextView tvProfileUsername;
    private Button btnFollowUnfollow;
    private ImageView ivBack;

    // The current user ID; passed from MainActivity's intent.
    private String currentUserId;

    /**
     * Simple model for a user.
     */
    public static class User {
        public String userId;
        public String username;
        public User(String userId, String username) {
            this.userId = userId;
            this.username = username;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        etSearch = view.findViewById(R.id.etSearch);
        rvResults = view.findViewById(R.id.rvResults);
        searchMainLayout = view.findViewById(R.id.searchMainLayout);
        searchProfileView = view.findViewById(R.id.searchProfileView);
        tvProfileUsername = searchProfileView.findViewById(R.id.username_text);
        btnFollowUnfollow = searchProfileView.findViewById(R.id.btnFollowUnfollow);
        ivBack = searchProfileView.findViewById(R.id.ivBack);

        rvResults.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SearchAdapter(userList);
        rvResults.setAdapter(adapter);
        firestore = FirebaseFirestore.getInstance();

        // Get currentUserId from the Activity's intent.
        currentUserId = getActivity().getIntent().getStringExtra("USER_ID");
        firestoreHelper = ((MainActivity) getActivity()).getFirestoreHelper();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString());
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        adapter.setOnItemClickListener(user -> showUserProfile(user));

        // Back button in profile view returns to search list.
        ivBack.setOnClickListener(v -> {
            searchProfileView.setVisibility(View.GONE);
            searchMainLayout.setVisibility(View.VISIBLE);
        });

        // ***** NEW: Check if the Activity's intent requests opening a profile *****
        boolean openProfileFlag = getActivity().getIntent().getBooleanExtra("open_profile", false);
        String profileUserId = getActivity().getIntent().getStringExtra("profile_user_id");
        if (openProfileFlag && profileUserId != null && !profileUserId.isEmpty()) {
            // Query Firestore for the user's details and then show the profile overlay.
            firestoreHelper.getUser(profileUserId, new FirestoreHelper.FirestoreCallback() {
                @Override
                public void onSuccess(Object result) {
                    String username = "Unknown";
                    if (result instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> data = (Map<String, Object>) result;
                        if(data.get("username") != null) {
                            username = (String) data.get("username");
                        }
                    }
                    // Construct a User object and call showUserProfile.
                    User user = new User(profileUserId, username);
                    requireActivity().runOnUiThread(() -> showUserProfile(user));
                }
                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void searchUsers(String query) {
        if(query.isEmpty()){
            userList.clear();
            adapter.notifyDataSetChanged();
            return;
        }
        firestore.collection("users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    userList.clear();
                    String lowerQuery = query.toLowerCase();
                    int count = 0;
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String username = doc.getString("username");
                        String userId = doc.getId();
                        // Exclude the current user from the search results.
                        if (userId.equals(currentUserId)) {
                            continue;
                        }
                        if (username != null && username.toLowerCase().contains(lowerQuery)) {
                            userList.add(new User(userId, username));
                            count++;
                            if (count >= 6) break;  // Limit to top 6 results
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Displays the selected user's profile as an overlay using layout_search_profile.xml.
     */
    private void showUserProfile(User user) {
        // Hide the main search layout and inflate the profile view.
        ViewGroup root = (ViewGroup) getView();
        if (root == null) return;

        final View profileView = LayoutInflater.from(getContext())
                .inflate(R.layout.layout_search_profile, root, false);
        profileView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        profileView.setElevation(100); // Ensure it appears on top

        // Bind profile view elements.
        TextView tvProfileUsername = profileView.findViewById(R.id.username_text);
        Button btnFollowUnfollow = profileView.findViewById(R.id.btnFollowUnfollow);
        ImageView ivBack = profileView.findViewById(R.id.ivBack);
        ImageView ivProfilePicture = profileView.findViewById(R.id.profile_icon);
        RecyclerView rvMoodEvents = profileView.findViewById(R.id.rvMoodEvents);
        TextView tvFriendCount = profileView.findViewById(R.id.tvFriendCount);
        TextView statusBubble = profileView.findViewById(R.id.user_status_bubble);

        // Set the username.
        tvProfileUsername.setText(user.username);

        // Initially hide mood events.
        rvMoodEvents.setVisibility(View.GONE);

        // Load the selected user's profile picture.
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
        MoodAdapter moodAdapter = new MoodAdapter(moodList, currentUserId);
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

    // Adapter for search results.
    private static class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {
        private final List<User> users;
        private OnItemClickListener listener;
        interface OnItemClickListener {
            void onItemClick(User user);
        }
        public void setOnItemClickListener(OnItemClickListener listener) {
            this.listener = listener;
        }
        SearchAdapter(List<User> users) {
            this.users = users;
        }
        @NonNull
        @Override
        public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new SearchViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
            User user = users.get(position);
            holder.textView.setText(user.username);
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(user);
                }
            });
        }
        @Override
        public int getItemCount() {
            return users.size();
        }
        static class SearchViewHolder extends RecyclerView.ViewHolder {
            private final TextView textView;
            SearchViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}
