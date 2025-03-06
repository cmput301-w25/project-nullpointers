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
    private final String description;
    private final String socialSituation;
    private final String userId;  // new field

    public MoodClusterItem(LatLng position, String emotion, String date, String description, String socialSituation, String userId) {
        this.position = position;
        this.socialSituation = socialSituation;
        this.emotion = emotion;
        this.date = date;
        this.description = description;
        this.userId = userId;
        this.title = emotion;
        this.snippet = "Date: " + date
                + "\nLocation: " + position.latitude + ", " + position.longitude
                + "\nDescription: " + description;
    }

    @Override
    public LatLng getPosition() { return position; }
    @Override
    public String getTitle() { return title; }
    @Override
    public String getSnippet() { return snippet; }
    public String getEmotion() { return emotion; }
    public String getDate() { return date; }
    public String getDescription() { return description; }
    public String getSocialSituation() { return socialSituation; }
    public String getUserId() { return userId; }
}
