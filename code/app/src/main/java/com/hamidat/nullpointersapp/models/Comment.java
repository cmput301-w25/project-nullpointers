package com.hamidat.nullpointersapp.models;

import com.google.firebase.Timestamp;

public class Comment {
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