package com.hamidat.nullpointersapp.utils.homeFeedUtils;

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

    // Simple Comment model.
    public static class Comment {
        private String userId;
        private String username;
        private String commentText;
        private Timestamp timestamp;

        public Comment() { }

        public Comment(String userId, String username, String commentText, Timestamp timestamp) {
            this.userId = userId;
            this.username = username;
            this.commentText = commentText;
            this.timestamp = timestamp;
        }

        public String getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getCommentText() { return commentText; }
        public Timestamp getTimestamp() { return timestamp; }
        public void setUserId(String userId) { this.userId = userId; }
        public void setUsername(String username) { this.username = username; }
        public void setCommentText(String commentText) { this.commentText = commentText; }
        public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
    }

    // Adapter for comments.
    public static class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {
        private List<Comment> comments;
        private FirestoreHelper firestoreHelper;

        public CommentsAdapter(List<Comment> comments) {
            this.comments = comments;
            firestoreHelper = new FirestoreHelper();
        }

        @NonNull
        @Override
        public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
            return new CommentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
            Comment comment = comments.get(position);
            holder.tvCommentUsername.setText(comment.getUsername());
            holder.tvCommentText.setText(comment.getCommentText());
            // Load the commenter's profile picture.
            firestoreHelper.getUser(comment.getUserId(), new FirestoreHelper.FirestoreCallback() {
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

                @Override
                public void onFailure(Exception e) {
                    holder.ivUserAvatar.post(() -> {
                        holder.ivUserAvatar.setImageResource(R.drawable.default_user_icon);
                        holder.ivUserAvatar.clearColorFilter();
                    });
                }
            });
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        public static class CommentViewHolder extends RecyclerView.ViewHolder {
            TextView tvCommentUsername, tvCommentText;
            ShapeableImageView ivUserAvatar;

            public CommentViewHolder(@NonNull View itemView) {
                super(itemView);
                tvCommentUsername = itemView.findViewById(R.id.tvCommentUsername);
                tvCommentText = itemView.findViewById(R.id.tvCommentText);
                ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            }
        }
    }

    public static CommentsBottomSheetFragment newInstance(String moodId, String currentUserId) {
        CommentsBottomSheetFragment fragment = new CommentsBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString("moodId", moodId);
        args.putString("currentUserId", currentUserId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_comments, container, false);
    }

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
            @Override
            public void onSuccess(Object result) {
                if (result instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) result;
                    currentUsername = (String) data.get("username");
                }
            }
            @Override
            public void onFailure(Exception e) {
                currentUsername = "Unknown";
            }
        });

        loadComments();

        // Show the send button only when there is valid (non-whitespace) text.
        etComment.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    btnPostComment.setVisibility(View.GONE);
                } else {
                    btnPostComment.setVisibility(View.VISIBLE);
                }
            }
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
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error loading comments: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
