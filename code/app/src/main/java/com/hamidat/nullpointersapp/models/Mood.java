package com.hamidat.nullpointersapp.models;

import com.google.firebase.Timestamp;
import java.io.Serializable;
import java.util.Date;

public class Mood implements Serializable {
    private String mood;
    private String moodDescription;
    private Timestamp timestamp;
    private String imageBase64;
    private double latitude;
    private double longitude;
    private String socialSituation;

    // Constructor with image
    public Mood(String mood, String moodDescription, String imageBase64,
                double latitude, double longitude, String socialSituation) {
        this.mood = mood;
        this.moodDescription = moodDescription;
        this.imageBase64 = imageBase64;
        this.latitude = latitude;
        this.longitude = longitude;
        this.socialSituation = socialSituation;
        this.timestamp = new Timestamp(new Date());
    }

    // Constructor without image
    public Mood(String mood, String moodDescription,
                double latitude, double longitude, String socialSituation) {
        this(mood, moodDescription, null, latitude, longitude, socialSituation);
    }

    // Empty constructor required for Firestore
    public Mood() {}

    // Getters and setters
    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }

    public String getMoodDescription() { return moodDescription; }
    public void setMoodDescription(String moodDescription) { this.moodDescription = moodDescription; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getSocialSituation() { return socialSituation; }
    public void setSocialSituation(String socialSituation) { this.socialSituation = socialSituation; }
}