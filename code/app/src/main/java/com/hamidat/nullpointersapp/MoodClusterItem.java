package com.hamidat.nullpointersapp;

import androidx.annotation.NonNull;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MoodClusterItem implements ClusterItem {
    private final LatLng position;
    private final String title;
    private final String snippet;
    private final String letter;

    public MoodClusterItem(LatLng position, String letter) {
        this.position = position;
        this.letter = letter;
        this.title = "Mood Event: " + letter;
        this.snippet = "Location: " + position.toString();
    }

    @NonNull
    @Override
    public LatLng getPosition() { return position; }

    @Override
    public String getTitle() { return title; }

    @Override
    public String getSnippet() { return snippet; }

    public String getLetter() { return letter; }
}