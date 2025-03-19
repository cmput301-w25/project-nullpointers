package com.hamidat.nullpointersapp.mainFragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.hamidat.nullpointersapp.MainActivity;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.models.MoodAdapter;
import com.hamidat.nullpointersapp.models.moodHistory;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;
import com.hamidat.nullpointersapp.utils.homeFeedUtils.CommentsBottomSheetFragment;
import com.hamidat.nullpointersapp.models.moodHistory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class HomeFeedFragment extends Fragment {

    private RecyclerView rvMoodList;

    private Button buttonFollowing;
    private MoodAdapter moodAdapter;
    private ArrayList<Mood> allMoods = new ArrayList<>();
    private FirestoreHelper firestoreHelper;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate your home feed layout
        View view = inflater.inflate(R.layout.full_mood_event, container, false);
        rvMoodList = view.findViewById(R.id.rvMoodList);
        rvMoodList.setLayoutManager(new LinearLayoutManager(getContext()));

        buttonFollowing = view.findViewById(R.id.tvFollowing);


        if(getActivity() instanceof MainActivity){
            MainActivity mainActivity = (MainActivity)getActivity();
            firestoreHelper = mainActivity.getFirestoreHelper();
            currentUserId = mainActivity.getCurrentUserId();
        }

        if (currentUserId != null) {
            moodAdapter = new MoodAdapter(allMoods, currentUserId);
            rvMoodList.setAdapter(moodAdapter);
        } else {
            Toast.makeText(getContext(), "Error: User ID is null. Restart app.", Toast.LENGTH_SHORT).show();
        }


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Get Firestore helper and current user ID from MainActivity.
        if(getActivity() instanceof MainActivity){
            MainActivity mainActivity = (MainActivity)getActivity();
            firestoreHelper = mainActivity.getFirestoreHelper();
            currentUserId = mainActivity.getCurrentUserId();
        }

        // Query mood events from current user and their followed users.
        fetchMoodData();

        // Attach listener to each child view to handle Comments button clicks.
        rvMoodList.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {
                Button btnComment = view.findViewById(R.id.btnComment);
                if(btnComment != null) {
                    btnComment.setOnClickListener(v -> {
                        int pos = rvMoodList.getChildAdapterPosition(view);
                        if(pos != RecyclerView.NO_POSITION) {
                            Mood mood = allMoods.get(pos);
                            openCommentsDialog(mood);
                        }
                    });
                }
            }
            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) { }
        });

        // buttonFollowing is for filtering the moods
        buttonFollowing.setOnClickListener(v -> {
            HomeFilterHistoryFragment filterFragment = new HomeFilterHistoryFragment(currentUserId, firestoreHelper);
            filterFragment.show(getChildFragmentManager(), "FilterMoodsSheet");
        });
    }

    private void fetchMoodData() {
        if (currentUserId == null || firestoreHelper == null) return;

        firestoreHelper.getUser(currentUserId, new FirestoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                if (result instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> userData = (Map<String, Object>) result;
                    ArrayList<String> followingIds = (ArrayList<String>) userData.get("following");
                    if (followingIds == null) {
                        followingIds = new ArrayList<>();
                    }
                    // Include current user to see their own moods
                    if (!followingIds.contains(currentUserId)) {
                        followingIds.add(currentUserId);
                    }
                    // Query mood events for these user IDs.
                    firestoreHelper.firebaseToMoodHistory(followingIds, new FirestoreHelper.FirestoreCallback() {
                        @Override
                        public void onSuccess(Object result) {
                            moodHistory history = (moodHistory) result;
                            allMoods.clear();

                            // Filter moods: show public moods and user's own private moods
                            for (Mood mood : history.getMoodArray()) {
                                if (!mood.isPrivate() || mood.getUserId().equals(currentUserId)) {
                                    allMoods.add(mood);
                                }
                            }

                            // Sort moods by newest first
                            java.util.Collections.sort(allMoods, (m1, m2) -> {
                                if (m1.getTimestamp() == null || m2.getTimestamp() == null) {
                                    return 0;
                                }
                                return m2.getTimestamp().compareTo(m1.getTimestamp());
                            });

                            requireActivity().runOnUiThread(() -> moodAdapter.notifyDataSetChanged());
                        }
                        @Override
                        public void onFailure(Exception e) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "Error loading moods: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    });
                }
            }
            @Override
            public void onFailure(Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    /**
     * Opens a dialog for viewing and adding comments for the given mood event.
     */
    private void openCommentsDialog(Mood mood) {
        if (mood.getMoodId() == null) {
            Toast.makeText(getContext(), "Cannot load comments: mood id is missing", Toast.LENGTH_SHORT).show();
            return;
        }
        CommentsBottomSheetFragment bottomSheet = CommentsBottomSheetFragment.newInstance(mood.getMoodId(), currentUserId);
        bottomSheet.show(getChildFragmentManager(), "CommentsBottomSheet");
    }


    // Simple Comment model.
    public static class Comment {
        private String userId;
        private String commentText;
        private Timestamp timestamp;

        public Comment() { }

        public Comment(String userId, String commentText, Timestamp timestamp) {
            this.userId = userId;
            this.commentText = commentText;
            this.timestamp = timestamp;
        }
        public String getUserId() { return userId; }
        public String getCommentText() { return commentText; }
        public Timestamp getTimestamp() { return timestamp; }
        public void setUserId(String userId) { this.userId = userId; }
        public void setCommentText(String commentText) { this.commentText = commentText; }
        public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
    }

    // Simple adapter for comments.
    public static class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {
        private List<Comment> comments;
        public CommentsAdapter(List<Comment> comments) {
            this.comments = comments;
        }
        @NonNull
        @Override
        public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new CommentViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
            Comment comment = comments.get(position);
            holder.text1.setText(comment.getCommentText());
            // For simplicity, display userId in text2. You might replace this with the actual username.
            holder.text2.setText(comment.getUserId());
        }
        @Override
        public int getItemCount() {
            return comments.size();
        }
        public static class CommentViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;
            public CommentViewHolder(@NonNull View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}
