package com.hamidat.nullpointersapp.mainFragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.hamidat.nullpointersapp.MainActivity;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.models.MoodAdapter;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreFollowing;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hamidat.nullpointersapp.models.Mood;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;

/**
 * Fragment for searching users in real time.
 * As the user types, matching users are shown.
 * When a username is tapped, a profile view is displayed with a Back button and a Follow/Unfollow button.
 */
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
    }

    private void searchUsers(String query) {
        if(query.isEmpty()){
            userList.clear();
            adapter.notifyDataSetChanged();
            return;
        }
        // Fetch all users (or consider using a limit if your dataset is large)
        firestore.collection("users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    userList.clear();
                    String lowerQuery = query.toLowerCase();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String username = doc.getString("username");
                        if (username != null && username.toLowerCase().contains(lowerQuery)) {
                            String userId = doc.getId();
                            userList.add(new User(userId, username));
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


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

        // Set the username.
        tvProfileUsername.setText(user.username);

        // Initially hide mood events.
        rvMoodEvents.setVisibility(View.GONE);

        // Check follow status and show mood events only if followed.
        firestoreHelper.getUser(currentUserId, new FirestoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                if (result instanceof Map) {
                    Map<String, Object> userData = (Map<String, Object>) result;
                    List<String> following = (List<String>) userData.get("following");
                    if (following != null && following.contains(user.userId)) {
                        btnFollowUnfollow.setText("Unfollow");
                        // Since user is followed, show and load mood events.
                        rvMoodEvents.setVisibility(View.VISIBLE);
                        loadRecentMoodEvents(user, rvMoodEvents);
                    } else {
                        btnFollowUnfollow.setText("Follow");
                        rvMoodEvents.setVisibility(View.GONE);
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
        MoodAdapter moodAdapter = new MoodAdapter(moodList);
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
