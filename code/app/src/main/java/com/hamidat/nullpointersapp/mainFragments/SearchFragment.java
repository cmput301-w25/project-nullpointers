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
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreFollowing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fragment for searching users in real time.
 * As the user types, matching users are shown.
 * When a username is tapped, a profile view is displayed with a Back button and a Follow/Unfollow button.
 */
public class SearchFragment extends Fragment {

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
        // Hide the main search layout and show the profile view.
        searchMainLayout.setVisibility(View.GONE);
        searchProfileView.setVisibility(View.VISIBLE);

        // Set the username.
        tvProfileUsername.setText(user.username);

        // Check if the current user is already following the selected user.
        firestore.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> following = (List<String>) documentSnapshot.get("following");
                        if (following != null && following.contains(user.userId)) {
                            btnFollowUnfollow.setText("Unfollow");
                        } else {
                            btnFollowUnfollow.setText("Follow");
                        }
                    }
                });

        // Set click listener for the Follow/Unfollow button.
        btnFollowUnfollow.setOnClickListener(v -> {
            String buttonText = btnFollowUnfollow.getText().toString();
            FirestoreHelper firestoreHelper = new FirestoreHelper();
            if (buttonText.equalsIgnoreCase("Follow")) {
                // Send friend request.
                firestoreHelper.sendFriendRequest(currentUserId, user.userId, new FirestoreFollowing.FollowingCallback() {
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
            } else if (buttonText.equalsIgnoreCase("Unfollow")) {
                firestoreHelper.removeFollowing(currentUserId, user.userId, new FirestoreFollowing.FollowingCallback() {
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
