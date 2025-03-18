package com.hamidat.nullpointersapp.models;

import com.google.firebase.Timestamp;
import java.io.Serializable;
import java.util.Date;

/**
 * Model representing a mood event.
 * Contains details such as mood state, description, location, social situation, image, timestamp, and user ID.
 */
public class Mood implements Serializable {
    // Add a moodId field to capture the document ID from Firestore, if desired.
    private String moodId;

    private String mood;
    private String moodDescription;
    private double latitude;
    private double longitude;
    private String socialSituation;
    private String imageBase64;
    private Timestamp timestamp;
    private String userId;

    // privacy
    private boolean isPrivate;

    /**
     * No-argument constructor for Firestore.
     */
    public Mood() { }

    /**
     * Sets the user ID.
     *
     * @param userId The unique identifier for the user.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Sets the mood ID.
     *
     * @param moodId The unique identifier for the user.
     */
    public void setMoodId(String moodId) {
        this.moodId = moodId;
    }

    /**
     * Sets the timestamp.
     *
     * @param timestamp The Firebase Timestamp.
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Sets the image in Base64 format.
     *
     * @param imageBase64 The Base64 encoded image.
     */
    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    /**
     * Sets the social situation.
     *
     * @param socialSituation The social context.
     */
    public void setSocialSituation(String socialSituation) {
        this.socialSituation = socialSituation;
    }

    /**
     * Sets the longitude.
     *
     * @param longitude The longitude value.
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Sets the latitude.
     *
     * @param latitude The latitude value.
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Sets the mood description.
     *
     * @param moodDescription The description of the mood.
     */
    public void setMoodDescription(String moodDescription) {
        this.moodDescription = moodDescription;
    }

    /**
     * Sets the mood.
     *
     * @param mood The mood state.
     */
    public void setMood(String mood) {
        this.mood = mood;
    }

    /**
     * Sets private/public status
     *
     * @param isPrivate The private status.
     */
    public void setPrivate(boolean isPrivate) {this.isPrivate = isPrivate;}

    /**
     * Constructs a new Mood without an image.
     *
     * @param mood            The mood state.
     * @param moodDescription The mood description.
     * @param latitude        The latitude value.
     * @param longitude       The longitude value.
     * @param socialSituation The social situation.
     * @param userId          The user ID.
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
     * Constructs a new Mood with an image.
     *
     * @param mood            The mood state.
     * @param moodDescription The mood description.
     * @param imageBase64     The Base64 encoded image.
     * @param latitude        The latitude value.
     * @param longitude       The longitude value.
     * @param socialSituation The social situation.
     * @param userId          The user ID.
     */
    public Mood(String mood, String moodDescription, String imageBase64, double latitude, double longitude, String socialSituation, String userId, boolean isPrivate) {
        this(mood, moodDescription, latitude, longitude, socialSituation, userId, isPrivate);
        this.imageBase64 = imageBase64;
    }

    /**
     * Returns the mood state.
     *
     * @return The mood.
     */
    public String getMood() { return mood; }

    /**
     * Returns the mood description.
     *
     * @return The mood description.
     */
    public String getMoodDescription() { return moodDescription; }

    /**
     * Returns the latitude.
     *
     * @return The latitude value.
     */
    public double getLatitude() { return latitude; }

    /**
     * Returns the longitude.
     *
     * @return The longitude value.
     */
    public double getLongitude() { return longitude; }

    /**
     * Returns the social situation.
     *
     * @return The social situation.
     */
    public String getSocialSituation() { return socialSituation; }

    /**
     * Returns the Base64 encoded image.
     *
     * @return The image in Base64.
     */
    public String getImageBase64() { return imageBase64; }
    public Timestamp getTimestamp() { return timestamp; }
    public String getUserId() { return userId; }
    public String getMoodId() {return moodId; }

    /**
     * Returns the user ID.
     *
     * @return The user ID.
     */
    public boolean isPrivate() {return isPrivate;}

}
