package com.hamidat.nullpointersapp;

import androidx.annotation.NonNull;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Represents a mood event marker on the map.
 * <p>
 * Implements ClusterItem for use with Google Maps clustering utility.
 * Contains location data and display information for a mood event.
 * </p>
 */
public class MoodClusterItem implements ClusterItem {
    private final LatLng position;
    private final String title;
    private final String snippet;
    private final String letter;

    /**
     * Constructs a new MoodClusterItem.
     *
     * @param position Geographic coordinates of the event
     * @param letter   Display character for the marker
     */
    public MoodClusterItem(LatLng position, String letter) {
        this.position = position;
        this.letter = letter;
        this.title = "Mood Event: " + letter;
        this.snippet = "Location: " + position.toString();
    }

    /**
     * Gets the geographic position of the event.
     *
     * @return LatLng coordinates of the event
     */
    @NonNull
    @Override
    public LatLng getPosition() { return position; }

    /**
     * Gets the title text for the marker.
     *
     * @return Title string for the event
     */
    @Override
    public String getTitle() { return title; }

    /**
     * Gets the description text for the marker.
     *
     * @return Description string for the event
     */
    @Override
    public String getSnippet() { return snippet; }

    /**
     * Gets the display character for the marker.
     *
     * @return Single-character string for marker display
     */
    public String getLetter() { return letter; }
}