package com.hamidat.nullpointersapp;

import java.time.LocalDateTime;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Mood {

//    Values for the mood (demo variables)
    protected String mood;
    protected String moodDescription;
    protected Date date;

//    Constructor for creating the mood
    public Mood(String mood, String moodDescription) {
        this.mood = mood;
        this.moodDescription = moodDescription;
        this.date =  new Date();
    }

    public Mood() {
        // Required empty constructor for Firestore
    }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
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
