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

    @NonNull
    @Override
    public LatLng getPosition() { return position; }

    @Override
    public String getTitle() { return title; }

    @Override
    public String getSnippet() { return snippet; }

    public String getEmotion() { return emotion; }

    public String getDate() { return date; }

    public String getTime() { return time; }

    public String getDescription() { return description; }

    public String getSocialSituation() { return socialSituation; }

    public String getImageBase64() { return imageBase64; }

    public String getUserId() { return userId; }
}
