/**
 * MoodClusterItem.java
 *
 * Represents a mood event on the map with metadata including emotion, timestamp, location,
 * image (Base64), description, and social context. Implements ClusterItem for map clustering.
 *
 * Outstanding Issues: None
 */

package com.hamidat.nullpointersapp.utils.mapUtils;

import androidx.annotation.NonNull;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Represents a mood event marker on the map with associated metadata.
 */
public class MoodClusterItem implements ClusterItem {
    private final LatLng position;
    private final String title;
    private final String snippet;
    private final String emotion;
    private final String date;
    private final String time;
    private final String description;
    private final String socialSituation;
    private final String imageBase64;
    private final String userId;

    /**
     * Constructs a mood cluster item with location, timestamp, image, and emotional metadata.
     *
     * @param position        Geographic coordinates of the event.
     * @param emotion         Emotional state (e.g., "Happy", "Sad").
     * @param date            Event date in yyyy-MM-dd format.
     * @param time            Event time in hh:mm a format.
     * @param description     Descriptive text about the event.
     * @param socialSituation Social context information.
     * @param imageBase64     Image associated with the mood in Base64 format.
     * @param userId          The user ID of the mood owner.
     */
    public MoodClusterItem(LatLng position, String emotion, String date, String time, String description, String socialSituation, String imageBase64, String userId) {
        this.position = position;
        this.emotion = emotion;
        this.date = date;
        this.time = time;
        this.description = description;
        this.socialSituation = socialSituation;
        this.imageBase64 = imageBase64;
        this.userId = userId;
        this.title = emotion;
        this.snippet = "Date: " + date + " " + time + "\nLocation: " + position.latitude + ", " + position.longitude + "\nDescription: " + description;
    }

    /**
     * Returns the geographic location of the mood event.
     *
     * @return The LatLng position of the event.
     */
    @NonNull
    @Override
    public LatLng getPosition() { return position; }

    /**
     * Returns the title of the mood event, which is the emotion.
     *
     * @return The emotion associated with the event.
     */
    @Override
    public String getTitle() { return title; }

    /**
     * Returns a snippet of information about the mood event.
     *
     * @return A formatted string with date, time, location, and description.
     */
    @Override
    public String getSnippet() { return snippet; }

    /**
     * Returns the emotion associated with the mood event.
     *
     * @return The emotion string.
     */
    public String getEmotion() { return emotion; }

    /**
     * Returns the date of the mood event.
     *
     * @return The date string.
     */
    public String getDate() { return date; }

    /**
     * Returns the time of the mood event.
     *
     * @return The time string.
     */
    public String getTime() { return time; }

    /**
     * Returns the description of the mood event.
     *
     * @return The description string.
     */
    public String getDescription() { return description; }

    /**
     * Returns the social situation associated with the mood event.
     *
     * @return The social situation string.
     */
    public String getSocialSituation() { return socialSituation; }

    /**
     * Returns the Base64 encoded image associated with the mood event.
     *
     * @return The Base64 image string.
     */
    public String getImageBase64() { return imageBase64; }

    /**
     * Returns the user ID of the mood event owner.
     *
     * @return The user ID string.
     */
    public String getUserId() { return userId; }
}