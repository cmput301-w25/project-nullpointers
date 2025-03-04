package com.hamidat.nullpointersapp.models;

import java.io.Serializable;
import java.util.Date;
import com.google.firebase.Timestamp;

/**
 * Represents a mood event in the system.
 * This class holds information about the user's emotional state and
 * a short description.
 *
 */

public class Mood implements Serializable {

    //Values for the mood (demo variables)
    protected String mood;
    protected String moodDescription;
    protected Timestamp timestamp;
    protected String imageBase64;

    // Constructor for creating the mood with an optional image
    public Mood(String mood, String moodDescription, String imageBase64) {
        this.mood = mood;
        this.moodDescription = moodDescription;
        this.timestamp = new Timestamp(new Date());
        this.imageBase64 = imageBase64;
    }

    // Constructor for creating the mood
    public Mood(String mood, String moodDescription) {
        this.mood = mood;
        this.moodDescription = moodDescription;
        this.timestamp = new Timestamp(new Date());
    }

    // Empty constructor
    public Mood() {
    }

    // Getters and setters for the mood
    public String getMood() {
        return this.mood;
    }
    public String getMoodDescription() {
        return this.moodDescription;
    }
    public Timestamp getTimestamp() {
        return this.timestamp;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getImageBase64() {
        return this.imageBase64;
    }
    public void setMoodDescription(String moodDescription) {
        this.moodDescription = moodDescription;
    }
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

}
