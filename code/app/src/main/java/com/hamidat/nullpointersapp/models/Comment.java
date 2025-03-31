/**
 * Comment.java
 *
 * This model represents a comment made by a user on a mood event.
 * It stores the user ID, comment text, and the timestamp of when the comment was posted.
 * This class is used in Firestore operations and displayed in comment UIs.
 *
 * Outstanding Issues: None
 */

package com.hamidat.nullpointersapp.models;

import com.google.firebase.Timestamp;

/**
 * Represents a single comment made by a user.
 */
public class Comment {

    private String userId;
    private String commentText;
    private Timestamp timestamp;

    /** Required for Firestore deserialization. */
    public Comment() { }

    /**
     * Constructs a Comment object.
     *
     * @param userId       ID of the user who made the comment.
     * @param commentText  The text content of the comment.
     * @param timestamp    Timestamp of when the comment was posted.
     */
    public Comment(String userId, String commentText, Timestamp timestamp) {
        this.userId = userId;
        this.commentText = commentText;
        this.timestamp = timestamp;
    }

    /**
     * Gets the user ID of the comment author.
     *
     * @return The user ID.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Gets the text content of the comment.
     *
     * @return The comment text.
     */
    public String getCommentText() {
        return commentText;
    }

    /**
     * Gets the timestamp of when the comment was posted.
     *
     * @return The timestamp.
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the user ID of the comment author.
     *
     * @param userId The user ID to set.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Sets the text content of the comment.
     *
     * @param commentText The comment text to set.
     */
    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    /**
     * Sets the timestamp of when the comment was posted.
     *
     * @param timestamp The timestamp to set.
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}