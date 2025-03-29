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
    /**
     * Gets the edited status of the mood.
     * @return edited status
     */
    public boolean isEdited() {
        return edited;
    }

    /**
     * Sets the edited status of the mood.
     * @param edited the new edited status
     */
    public void setEdited(boolean edited) {
        this.edited = edited;
    }
    /**
     * Gets the mood ID.
     * @return moodId
     */
    public String getMoodId() {
        return moodId;
    }
    /**
     * Sets the mood ID.
     * @param moodId the new moodId
     */
    public void setMoodId(String moodId) {
        this.moodId = moodId;
    }

    /**
     * Sets the user ID.
     * @param userId the new userId
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Sets the timestamp.
     * @param timestamp the new timestamp
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Sets the imageBase64.
     * @param imageBase64 the new imageBase64
     */
    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    /**
     * Sets the social situation.
     * @param socialSituation the new social situation
     */
    public void setSocialSituation(String socialSituation) {
        this.socialSituation = socialSituation;
    }

    /**
     * Sets the longitude.
     * @param longitude the new longitude
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Sets the latitude.
     * @param latitude the new latitude
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Sets the mood description.
     * @param moodDescription the new mood description
     */
    public void setMoodDescription(String moodDescription) {
        this.moodDescription = moodDescription;
    }

    /**
     * Sets the mood.
     * @param mood the new mood
     */
    public void setMood(String mood) {
        this.mood = mood;
    }

    /**
     * Sets the privacy of the mood.
     * @param isPrivate the new privacy setting
     */
    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    /**
     * Constructor for Mood object.
     * @param mood the mood
     * @param moodDescription the mood description
     * @param latitude the latitude
     * @param longitude the longitude
     * @param socialSituation the social situation
     * @param userId the user ID
     * @param isPrivate the privacy setting
     */
    public Mood(String mood, String moodDescription, double latitude, double longitude, String socialSituation, String userId, boolean isPrivate) {
        this.mood = mood;
        this.moodDescription = moodDescription;
        this.latitude = latitude;
        this.longitude = longitude;
        this.socialSituation = socialSituation;
        this.userId = userId;
        this.isPrivate = isPrivate;
    }

    /**
     * Constructor for Mood object with imageBase64.
     * @param mood the mood
     * @param moodDescription the mood description
     * @param imageBase64 the imageBase64
     * @param latitude the latitude
     * @param longitude the longitude
     * @param socialSituation the social situation
     * @param userId the user ID
     * @param isPrivate the privacy setting
     */
    public Mood(String mood, String moodDescription, String imageBase64, double latitude, double longitude, String socialSituation, String userId, boolean isPrivate) {
        this(mood, moodDescription, latitude, longitude, socialSituation, userId, isPrivate);
        this.imageBase64 = imageBase64;
    }

    /**
     * Gets the mood.
     * @return mood
     */
    public String getMood() { return mood; }
    /**
     * Gets the mood description.
     * @return mood description
     */
    public String getMoodDescription() { return moodDescription; }
    /**
     * Gets the latitude.
     * @return latitude
     */
    public double getLatitude() { return latitude; }
    /**
     * Gets the longitude.
     * @return longitude
     */
    public double getLongitude() { return longitude; }
    /**
     * Gets the social situation.
     * @return social situation
     */
    public String getSocialSituation() { return socialSituation; }
    /**
     * Gets the imageBase64.
     * @return imageBase64
     */
    public String getImageBase64() { return imageBase64; }
    /**
     * Gets the timestamp.
     * @return timestamp
     */
    public Timestamp getTimestamp() { return timestamp; }
    /**
     * Gets the user ID.
     * @return user ID
     */
    public String getUserId() { return userId; }
    /**
     * Gets the privacy setting.
     * @return privacy setting
     */
    public boolean isPrivate() {return isPrivate;}

    // My updates for the like and comment count
    /**
     * Gets the like count.
     * @return like count
     */
    public int getLikeCount() {
        return likeCount;
    }

    /**
     * Sets the like count.
     * @param likeCount the new like count
     */
    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    /**
     * Gets the comment count.
     * @return comment count
     */
    public int getCommentCount() {
        return commentCount;
    }

    /**
     * Sets the comment count.
     * @param commentCount the new comment count
     */
    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    /**
     * Gets the list of user IDs that liked the mood.
     * @return list of user IDs
     */
    public List<String> getLikedByUserIds() {
        if (likedByUserIds == null) likedByUserIds = new ArrayList<>();
        return likedByUserIds;
    }

    /**
     * Sets the list of user IDs that liked the mood.
     * @param likedByUserIds the new list of user IDs
     */
    public void setLikedByUserIds(List<String> likedByUserIds) {
        this.likedByUserIds = (likedByUserIds != null) ? likedByUserIds : new ArrayList<>();
    }

    /**
     * Checks if the mood is liked by a specific user.
     * @param userId the user ID to check
     * @return true if liked, false otherwise
     */
    public boolean isLikedBy(String userId) {
        return getLikedByUserIds().contains(userId);
    }

}