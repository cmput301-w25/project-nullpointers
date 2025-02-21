package com.hamidat.nullpointersapp;

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
    private final String description;

    /**
     * Constructs a mood cluster item with location and emotional metadata.
     *
     * @param position    Geographic coordinates of the event
     * @param emotion     Emotional state (e.g., "Happy", "Sad")
     * @param date        Event date in yyyy-MM-dd format
     * @param description Descriptive text about the event
     */
    public MoodClusterItem(LatLng position, String emotion, String date, String description) {
        this.position = position;
        this.emotion = emotion;
        this.date = date;
        this.description = description;
        this.title = emotion;
        this.snippet = "Date: " + date
                + "\nLocation: " + position.latitude + ", " + position.longitude
                + "\nDescription: " + description;
    }
    /**
     * Gets the geographic position of the event.
     * @return LatLng coordinates of the event
     */
    @NonNull
    @Override
    public LatLng getPosition() { return position; }
    /**
     * Gets the emotion label for display.
     * @return Emotion string (e.g., "Happy")
     */
    @Override
    public String getTitle() { return title; }
    /**
     * Gets formatted metadata for marker info windows.
     * @return Multiline string with date, location, and description
     */
    @Override
    public String getSnippet() { return snippet; }
    /**
     * Retrieves the emotion type.
     * @return Emotion string
     */
    public String getEmotion() { return emotion; }
    /**
     * Gets the event date.
     * @return Date string in yyyy-MM-dd format
     */
    public String getDate() {
        return date;
    }
    /**
     * Gets the event description.
     * @return Descriptive text
     */
    public String getDescription() {
        return description;
    }
}