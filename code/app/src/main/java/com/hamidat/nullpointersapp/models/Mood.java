/**
 * Mood.java
 *
 * Model representing a mood event. Contains details such as:
 * - mood state (e.g., Happy, Sad)
 * - optional description
 * - location (latitude and longitude)
 * - social situation
 * - optional image (Base64)
 * - timestamp and user ID
 * - privacy settings
 * - interaction metrics (likes and comments)
 *
 * Outstanding Issues: None
 */

package com.hamidat.nullpointersapp.models;

import com.google.firebase.Timestamp;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Model representing a mood event.
 * Contains details such as mood state, description, location, social situation, image, timestamp, and user ID.
 */
public class Mood implements Serializable {
    // Add a moodId field to capture the document ID from Firestore, if desired.
    private String moodId;
    private boolean edited;

    private String mood;
    private String moodDescription;
    private double latitude;
    private double longitude;
    private String socialSituation;
    private String imageBase64;
    private Timestamp timestamp;
    private String userId;

    private int likeCount = 0;
    private int commentCount = 0;
    private List<String> likedByUserIds = new ArrayList<>();


    // privacy
    private boolean isPrivate;

    /**
     * No-argument constructor for Firestore.
     */
    public Mood() { }

    // Getters and setters for moodId
    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }
    public String getMoodId() {
        return moodId;
    }
    public void setMoodId(String moodId) {
        this.moodId = moodId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public void setSocialSituation(String socialSituation) {
        this.socialSituation = socialSituation;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setMoodDescription(String moodDescription) {
        this.moodDescription = moodDescription;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public Mood(String mood, String moodDescription, double latitude, double longitude, String socialSituation, String userId, boolean isPrivate) {
        this.mood = mood;
        this.moodDescription = moodDescription;
        this.latitude = latitude;
        this.longitude = longitude;
        this.socialSituation = socialSituation;
        this.userId = userId;
        this.isPrivate = isPrivate;
    }

    public Mood(String mood, String moodDescription, String imageBase64, double latitude, double longitude, String socialSituation, String userId, boolean isPrivate) {
        this(mood, moodDescription, latitude, longitude, socialSituation, userId, isPrivate);
        this.imageBase64 = imageBase64;
    }

    public String getMood() { return mood; }
    public String getMoodDescription() { return moodDescription; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getSocialSituation() { return socialSituation; }
    public String getImageBase64() { return imageBase64; }
    public Timestamp getTimestamp() { return timestamp; }
    public String getUserId() { return userId; }
    public boolean isPrivate() {return isPrivate;}

    // My updates for the like and comment count
    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public List<String> getLikedByUserIds() {
        if (likedByUserIds == null) likedByUserIds = new ArrayList<>();
        return likedByUserIds;
    }

    public void setLikedByUserIds(List<String> likedByUserIds) {
        this.likedByUserIds = (likedByUserIds != null) ? likedByUserIds : new ArrayList<>();
    }

    public boolean isLikedBy(String userId) {
        return getLikedByUserIds().contains(userId);
    }

}
