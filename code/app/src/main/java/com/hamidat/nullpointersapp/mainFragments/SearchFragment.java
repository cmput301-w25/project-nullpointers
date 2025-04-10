/**
 * SearchFragment.java
 *
 * Allows users to search for other users and view their profiles.
 * Provides functionality to follow/unfollow users and view their recent mood events.
 *
 * <p><b>Outstanding issues:</b> None.</p>
 */

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
import androidx.appcompat.app.AppCompatActivity;
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

/**
 * Fragment that allows users to search for other users and view their profiles.
 * It provides functionality to follow/unfollow users and view their recent mood events.
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
     * Simple model for a user, containing their ID and username.
     */
    public static class User {
        public String userId;
        public String username;
        public User(String userId, String username) {
            this.userId = userId;
            this.username = username;
        }
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     * UI should be attached to. The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from
     * a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    /**
     * Called immediately after onCreateView(LayoutInflater, ViewGroup, Bundle) has returned,
     * but before any saved state has been restored in to the view.
     *
     * @param view               The View returned by onCreateView(LayoutInflater, ViewGroup, Bundle).
     * @param savedInstanceState If non-null, this fragment is being re-constructed from
     * a previous saved state as given here.
     */
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
            /**
             * This method is called before the text is changed.
             *
             * @param s     The character sequence about to be changed.
             * @param start The index within s where the change is about to start.
             * @param count The number of characters about to be replaced.
             * @param after The number of characters that will replace the removed characters.
             */
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed before text change.
            }

            /**
             * This method is called during the text change. It triggers the searchUsers method
             * with the current text from the EditText.
             *
             * @param s      The character sequence that has been changed.
             * @param start  The index within s where the change started.
             * @param before The number of characters that were replaced.
             * @param count  The number of characters that were added.
             */
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString());
            }

            /**
             * This method is called after the text has been changed.
             *
             * @param s The editable character sequence after the change.
             */
            @Override
            public void afterTextChanged(Editable s) {
                // No action needed after text change.
            }
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
                /**
                 * Called when the operation succeeds.
                 *
                 * @param result The result of the operation.
                 */
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

                /**
                 * Called when the operation fails.
                 *
                 * @param e The exception that occurred.
                 */
                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Searches for users whose usernames contain the given query string.
     *
     * @param query The search query string.
     */
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
     *
     * @param user The User object representing the selected user.
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
        // Ensure the overlay consumes all touch events.
        profileView.setClickable(true);
        profileView.setFocusable(true);
        profileView.setOnTouchListener((v, event) -> true);

        // Optionally disable interaction with the underlying search layout.
        final View searchMainLayout = getView().findViewById(R.id.searchMainLayout);
        if (searchMainLayout != null) {
            searchMainLayout.setEnabled(false);
            searchMainLayout.setClickable(false);
        }

        // Bind profile overlay elements.
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
            /**
             * Called when the operation succeeds.
             *
             * @param result The result of the operation.
             */
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

            /**
             * Called when the operation fails.
             *
             * @param e The exception that occurred.
             */
            @Override
            public void onFailure(Exception e) {
                ivProfilePicture.post(() -> ivProfilePicture.setImageResource(R.drawable.default_user_icon));
            }
        });

        // Check follow status and show mood events only if the selected user is followed.
        firestoreHelper.getUser(currentUserId, new FirestoreHelper.FirestoreCallback() {
            /**
             * Called when the operation succeeds.
             *
             * @param result The result of the operation.
             */
            @Override
            public void onSuccess(Object result) {
                if (result instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> userData = (Map<String, Object>) result;
                    List<String> following = (List<String>) userData.get("following");
                    if (following != null && following.contains(user.userId)) {

                        // Show the users status as well if they have one
                        TextView statusBubble = profileView.findViewById(R.id.user_status_bubble);
                        String status = (String) userData.get("status");

                        if (status != null && !status.isEmpty()) {
                            statusBubble.setText(status);
                            statusBubble.setVisibility(View.VISIBLE);
                        } else {
                            statusBubble.setVisibility(View.GONE);
                        }

                        btnFollowUnfollow.setText("Unfollow");
                        rvMoodEvents.setVisibility(View.VISIBLE);
                        loadRecentMoodEvents(user, rvMoodEvents);

                        // Also load the target user's status for display.
                        firestoreHelper.getUser(user.userId, new FirestoreHelper.FirestoreCallback() {
                            /**
                             * Called when the operation succeeds.
                             *
                             * @param result The result of the operation.
                             */
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

                            /**
                             * Called when the operation fails.
                             *
                             * @param e The exception that occurred.
                             */
                            @Override
                            public void onFailure(Exception e) {
                                statusBubble.setVisibility(View.GONE);
                            }
                        });

                        // Get their friend count to display.
                        firestoreHelper.getUser(user.userId, new FirestoreHelper.FirestoreCallback() {
                            /**
                             * Called when the operation succeeds.
                             *
                             * @param result The result of the operation.
                             */
                            @Override
                            public void onSuccess(Object result) {
                                if (result instanceof Map) {
                                    Map<String, Object> theirData = (Map<String, Object>) result;
                                    List<String> theirFollowing = (List<String>) theirData.get("following");
                                    int friendCount = theirFollowing != null ? theirFollowing.size()-1 : 0;
                                    if (theirFollowing.size() ==1 ){
                                        friendCount = 1;
                                        tvFriendCount.setText("Friends: " + friendCount);
                                        tvFriendCount.setVisibility(View.VISIBLE);
                                    }else{
                                        tvFriendCount.setText("Friends: " + friendCount);
                                        tvFriendCount.setVisibility(View.VISIBLE);
                                    }
                                }
                            }

                            /**
                             * Called when the operation fails.
                             *
                             * @param e The exception that occurred.
                             */
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

            /**
             * Called when the operation fails.
             *
             * @param e The exception that occurred.
             */
            @Override
            public void onFailure(Exception e) { }
        });

        // Set follow/unfollow button behavior.
        btnFollowUnfollow.setOnClickListener(v -> {
            String currentText = btnFollowUnfollow.getText().toString();
            FirestoreHelper helper = new FirestoreHelper();
            if (currentText.equalsIgnoreCase("Follow")) {
                helper.sendFriendRequest(currentUserId, user.userId, new FirestoreFollowing.FollowingCallback() {
                    /**
                     * Called when the operation succeeds.
                     *
                     * @param result The result of the operation.
                     */
                    @Override
                    public void onSuccess(Object result) {
                        btnFollowUnfollow.setText("Pending");
                        Toast.makeText(getContext(), "Follow request sent", Toast.LENGTH_SHORT).show();
                    }

                    /**
                     * Called when the operation fails.
                     *
                     * @param e The exception that occurred.
                     */
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (currentText.equalsIgnoreCase("Unfollow")) {
                helper.removeFollowing(currentUserId, user.userId, new FirestoreFollowing.FollowingCallback() {
                    /**
                     * Called when the operation succeeds.
                     *
                     * @param result The result of the operation.
                     */
                    @Override
                    public void onSuccess(Object result) {
                        btnFollowUnfollow.setText("Follow");
                        rvMoodEvents.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Unfollowed", Toast.LENGTH_SHORT).show();
                    }

                    /**
                     * Called when the operation fails.
                     *
                     * @param e The exception that occurred.
                     */
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Set the back button to remove the overlay and re-enable the search view.
        ivBack.setOnClickListener(v -> {
            root.removeView(profileView);
            if (searchMainLayout != null) {
                searchMainLayout.setEnabled(true);
                searchMainLayout.setClickable(true);
            }
        });

        // Add the profile view as an overlay.
        root.addView(profileView);
        profileView.bringToFront();
        profileView.invalidate();
    }

    /**
     * Queries Firestore for the three most recent mood events of the selected user
     * and binds them to the given RecyclerView using MoodAdapter.
     *
     * @param user         The User object representing the selected user.
     * @param rvMoodEvents The RecyclerView to display the mood events.
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
                            if (!mood.isPrivate() || mood.getUserId().equals(currentUserId)) {
                                moodList.add(mood);
                            }
                        }
                    }
                    requireActivity().runOnUiThread(() -> moodAdapter.notifyDataSetChanged());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading mood events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Adapter for search results.
     */
    private static class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {
        private final List<User> users;
        private OnItemClickListener listener;

        /**
         * Interface for handling item click events.
         */
        interface OnItemClickListener {
            void onItemClick(User user);
        }

        /**
         * Sets the item click listener.
         *
         * @param listener The OnItemClickListener to set.
         */
        public void setOnItemClickListener(OnItemClickListener listener) {
            this.listener = listener;
        }

        /**
         * Constructs a new SearchAdapter.
         *
         * @param users The list of User objects to display.
         */
        SearchAdapter(List<User> users) {
            this.users = users;
        }

        /**
         * Called when RecyclerView needs a new RecyclerView.ViewHolder of the given type to represent an item.
         *
         * @param parent   The ViewGroup into which the new View will be added after it is bound to an adapter position.
         * @param viewType The view type of the new View.
         * @return A new ViewHolder that holds a View of the given view type.
         */
        @NonNull
        @Override
        public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new SearchViewHolder(view);
        }

        /**
         * Called by RecyclerView to display the data at the specified position.
         *
         * @param holder   The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
         * @param position The position of the item within the adapter's data set.
         */
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

        /**
         * Returns the total number of items in the data set held by the adapter.
         *
         * @return The total number of items in this adapter.
         */
        @Override
        public int getItemCount() {
            return users.size();
        }

        /**
         * ViewHolder for displaying a User object in the search results.
         */
        static class SearchViewHolder extends RecyclerView.ViewHolder {
            private final TextView textView;

            /**
             * Constructs a new SearchViewHolder.
             *
             * @param itemView The View to display the User object.
             */
            SearchViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}