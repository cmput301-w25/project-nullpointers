package com.hamidat.nullpointersapp;

import java.time.LocalDateTime;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.google.firebase.Timestamp;


public class Mood {

//    Values for the mood (demo variables)
    protected String mood;
    protected String moodDescription;
    protected Timestamp timestamp;

//    Constructor for creating the mood
    public Mood(String mood, String moodDescription) {
        this.mood = mood;
        this.moodDescription = moodDescription;
        this.timestamp = new Timestamp(new Date());
    }

    public Mood() {
        // Required empty constructor for Firestore
    }

    public Timestamp getTimeStamp() {
        return this.timestamp;
    }

//    Getters and setters for the mood
    public String getMood() {
        return this.mood;
    }
    public String getMoodDescription() {
        return this.moodDescription;
    }
    public Date date() {
        return this.date;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }
    public void setMoodDescription(String moodDescription) {
        this.moodDescription = moodDescription;
    }
    public void setDate(Date date) {
        this.date = date;
    }


}
