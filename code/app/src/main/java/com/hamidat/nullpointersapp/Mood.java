package com.hamidat.nullpointersapp;

import java.io.Serializable;

public class Mood implements Serializable {
    private String moodName;
    private String date;
    private String description;

    public Mood(String moodName, String date, String description) {
        this.moodName = moodName;
        this.date = date;
        this.description = description;
    }

    public String getMoodName() {
        return moodName;
    }

    public void setMoodName(String moodName) {
        this.moodName = moodName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
