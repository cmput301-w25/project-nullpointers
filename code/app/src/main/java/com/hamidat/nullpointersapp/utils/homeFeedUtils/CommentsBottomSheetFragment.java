/**
 * CommentsBottomSheetFragment.java
 *
 * Bottom sheet dialog fragment that displays and allows posting of comments on a mood.
 * Handles displaying user avatars, posting new comments, and real-time updating of the comment list.
 *
 * Outstanding Issues: None
 */

package com.hamidat.nullpointersapp.utils.homeFeedUtils;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;
import com.google.android.material.imageview.ShapeableImageView;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import android.text.format.DateUtils;

/**
 * A bottom sheet dialog fragment that displays and allows posting of comments on a mood.
 */
public class CommentsBottomSheetFragment extends BottomSheetDialogFragment {

    private String moodId;
    private String currentUserId;
    private String currentUsername;  // Current user's username
    private RecyclerView rvComments;
    private EditText etComment;
    private ImageButton btnPostComment;
    private List<Comment> commentList = new ArrayList<>();
    private CommentsAdapter commentsAdapter;
    private FirebaseFirestore firestore;
    private Runnable onDismissListener;


    /**
     * Represents a comment on a mood.
     */
    public static class Comment {
        private String userId;
        private String username;
        private String commentText;
        private Timestamp timestamp;

        /**
         * Default constructor required for Firestore.
         */
        public Comment() { }

        /**
         * Constructs a new Comment.
         *
         * @param userId The ID of the user who posted the comment.
         * @param username The username of the user who posted the comment.
         * @param commentText The text of the comment.
         * @param timestamp The timestamp of when the comment was posted.
         */
        public Comment(String userId, String username, String commentText, Timestamp timestamp) {
            this.userId = userId;
            this.username = username;
            this.commentText = commentText;
            this.timestamp = timestamp;
        }

        /**
         * Gets the ID of the user who posted the comment.
         *
         * @return The user ID.
         */
        public String getUserId() { return userId; }

        /**
         * Gets the username of the user who posted the comment.
         *
         * @return The username.
         */
        public String getUsername() { return username; }

        /**
         * Gets the text of the comment.
         *
         * @return The comment text.
         */
        public String getCommentText() { return commentText; }

        /**
         * Gets the timestamp of when the comment was posted.
         *
         * @return The timestamp.
         */
        public Timestamp getTimestamp() { return timestamp; }

        /**
         * Sets the ID of the user who posted the comment.
         *
         * @param userId The user ID.
         */
        public void setUserId(String userId) { this.userId = userId; }

        /**
         * Sets the username of the user who posted the comment.
         *
         * @param username The username.
         */
        public void setUsername(String username) { this.username = username; }

        /**
         * Sets the text of the comment.
         *
         * @param commentText The comment text.
         */
        public void setCommentText(String commentText) { this.commentText = commentText; }

        /**
         * Sets the timestamp of when the comment was posted.
         *
         * @param timestamp The timestamp.
         */
        public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
    }

    /**
     * Adapter for displaying comments in a RecyclerView.
     */
    public static class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {
        private List<Comment> comments;
        private FirestoreHelper firestoreHelper;

        /**
         * Constructs a new CommentsAdapter with the given list of comments.
         *
         * @param comments The list of comments to display.
         */
        public CommentsAdapter(List<Comment> comments) {
            this.comments = comments;
            firestoreHelper = new FirestoreHelper();
        }


        /**
         * Creates a new ViewHolder for a comment item.
         *
         * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
         * @param viewType The view type of the new View.
         * @return A new CommentViewHolder that holds a View of the given view type.
         */
        @NonNull
        @Override
        public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
            return new CommentViewHolder(view);
        }

        /**
         * Binds the comment data to the ViewHolder at the specified position.
         *
         * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
         * @param position The position of the item within the adapter's data set.
         */
        @Override
        public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
            Comment comment = comments.get(position);
            holder.tvCommentUsername.setText(comment.getUsername());
            holder.tvCommentText.setText(comment.getCommentText());

            Timestamp timestamp = comment.getTimestamp();
            if (timestamp != null) {
                long timeInMillis = timestamp.toDate().getTime();
                CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
                        timeInMillis,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                );
                holder.tvCommentTimestamp.setText(relativeTime);
            }

            // Load the commenter's profile picture.
            firestoreHelper.getUser(comment.getUserId(), new FirestoreHelper.FirestoreCallback() {
                /**
                 * Called when the user data is successfully retrieved from Firestore.
                 * Sets the user's profile picture in the ImageView.
                 *
                 * @param result The user data retrieved from Firestore.
                 */
                @Override
                public void onSuccess(Object result) {
                    if (result instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> userData = (Map<String, Object>) result;
                        String profilePicBase64 = (String) userData.get("profilePicture");
                        if (profilePicBase64 != null && !profilePicBase64.isEmpty()) {
                            byte[] decodedBytes = Base64.decode(profilePicBase64, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                            holder.ivUserAvatar.post(() -> {
                                holder.ivUserAvatar.setImageBitmap(bitmap);
                                holder.ivUserAvatar.clearColorFilter();
                            });
                        } else {
                            holder.ivUserAvatar.post(() -> {
                                holder.ivUserAvatar.setImageResource(R.drawable.default_user_icon);
                                holder.ivUserAvatar.clearColorFilter();
                            });
                        }
                    } else {
                        holder.ivUserAvatar.post(() -> {
                            holder.ivUserAvatar.setImageResource(R.drawable.default_user_icon);
                            holder.ivUserAvatar.clearColorFilter();
                        });
                    }
                }

                /**
                 * Called when the user data retrieval fails.
                 * Sets the default user icon.
                 *
                 * @param e The exception that occurred during the failure.
                 */
                @Override
                public void onFailure(Exception e) {
                    holder.ivUserAvatar.post(() -> {
                        holder.ivUserAvatar.setImageResource(R.drawable.default_user_icon);
                        holder.ivUserAvatar.clearColorFilter();
                    });
                }
            });
        }

        /**
         * Returns the total number of comments in the data set held by the adapter.
         *
         * @return The size of the comments list.
         */
        @Override
        public int getItemCount() {
            return comments.size();
        }

        /**
         * ViewHolder for a comment item.
         */
        public static class CommentViewHolder extends RecyclerView.ViewHolder {
            TextView tvCommentUsername, tvCommentText, tvCommentTimestamp;
            ShapeableImageView ivUserAvatar;

            /**
             * Constructs a new CommentViewHolder.
             *
             * @param itemView The View representing a comment item.
             */
            public CommentViewHolder(@NonNull View itemView) {
                super(itemView);
                tvCommentUsername = itemView.findViewById(R.id.tvCommentUsername);
                tvCommentText = itemView.findViewById(R.id.tvCommentText);
                ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
                tvCommentTimestamp = itemView.findViewById(R.id.tvCommentTimestamp);
            }
        }
    }

    /**
     * Creates a new instance of CommentsBottomSheetFragment with the given moodId and currentUserId.
     *
     * @param moodId The ID of the mood to load comments for.
     * @param currentUserId The ID of the current user.
     * @return A new instance of CommentsBottomSheetFragment.
     */
    public static CommentsBottomSheetFragment newInstance(String moodId, String currentUserId) {
        CommentsBottomSheetFragment fragment = new CommentsBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString("moodId", moodId);
        args.putString("currentUserId", currentUserId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Inflates the layout for this fragment.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_comments, container, false);
    }

    /**
     * Called immediately after onCreateView(LayoutInflater, ViewGroup, Bundle) has returned, but before any saved state has been restored in to the view.
     * Initializes the views and sets up the RecyclerView and Firestore.
     *
     * @param view The View returned by onCreateView(LayoutInflater, ViewGroup, Bundle).
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rvComments = view.findViewById(R.id.rvComments);
        etComment = view.findViewById(R.id.etComment);
        btnPostComment = view.findViewById(R.id.btnPostComment);

        rvComments.setLayoutManager(new LinearLayoutManager(getContext()));
        commentsAdapter = new CommentsAdapter(commentList);
        rvComments.setAdapter(commentsAdapter);

        firestore = FirebaseFirestore.getInstance();
        if (getArguments() != null) {
            moodId = getArguments().getString("moodId");
            currentUserId = getArguments().getString("currentUserId");
        }

        // Retrieve current user's username from Firestore.
        FirestoreHelper helper = new FirestoreHelper();
        helper.getUser(currentUserId, new FirestoreHelper.FirestoreCallback() {
            /**
             * Called when the user data is successfully retrieved.
             * Sets the currentUsername variable.
             *
             * @param result The user data retrieved from Firestore.
             */
            @Override
            public void onSuccess(Object result) {
                if (result instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) result;
                    currentUsername = (String) data.get("username");
                }
            }

            /**
             * Called when the user data retrieval fails.
             * Sets the currentUsername to "Unknown".
             *
             * @param e The exception that occurred during the failure.
             */
            @Override
            public void onFailure(Exception e) {
                currentUsername = "Unknown";
            }
        });

        loadComments();

        // Show the send button only when there is valid (non-whitespace) text.
        etComment.addTextChangedListener(new TextWatcher() {
            /**
             * Called before the text is changed.
             *
             * @param s The CharSequence before the change.
             * @param start The index of the start of the changed text.
             * @param count The number of characters being replaced.
             * @param after The number of characters being added.
             */
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            /**
             * Called when the text is changed.
             *
             * @param s The CharSequence after the change.
             * @param start The index of the start of the changed text.
             * @param before The number of characters being replaced.
             * @param count The number of characters being added.
             */
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    btnPostComment.setVisibility(View.GONE);
                } else {
                    btnPostComment.setVisibility(View.VISIBLE);
                }
            }

            /**
             * Called after the text is changed.
             *
             * @param s The Editable after the change.
             */
            @Override public void afterTextChanged(Editable s) { }
        });
        btnPostComment.setVisibility(View.GONE);

        btnPostComment.setOnClickListener(v -> {
            String text = etComment.getText().toString().trim();
            if (TextUtils.isEmpty(text)) {
                Toast.makeText(getContext(), "Please enter a comment", Toast.LENGTH_SHORT).show();
                return;
            }
            String usernameToUse = (currentUsername != null) ? currentUsername : "Unknown";
            Comment newComment = new Comment(currentUserId, usernameToUse, text, new Timestamp(new Date()));
            firestore.collection("moods").document(moodId)
                    .collection("comments")
                    .add(newComment)
                    .addOnSuccessListener(docRef -> {
                        Toast.makeText(getContext(), "Comment posted", Toast.LENGTH_SHORT).show();
                        etComment.setText("");
                        loadComments();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Error posting comment: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    /**
     * Loads comments from Firestore and updates the RecyclerView.
     */
    private void loadComments() {
        firestore.collection("moods").document(moodId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    commentList.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Comment comment = doc.toObject(Comment.class);
                        if (comment != null) {
                            commentList.add(comment);
                        }
                    }
                    commentsAdapter.notifyDataSetChanged();

                    firestore.collection("moods").document(moodId)
                            .update("commentCount", commentList.size());

                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error loading comments: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Sets a listener to be called when the dialog is dismissed.
     *
     * @param listener The Runnable to execute when the dialog is dismissed.
     */
    public void setOnDismissListener(Runnable listener) {
        this.onDismissListener = listener;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.run();
        }
    }
}