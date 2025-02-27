package com.hamidat.nullpointersapp;

import java.io.Serializable;

public class Mood implements Serializable {
    private String moodName;
    private String date; // Expected format: YYYY-MM-DD HH:MM
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

    public int getYear() {
        return Integer.parseInt(date.substring(0, 4));
    }

    public int getMonth() {
        return Integer.parseInt(date.substring(5, 7));
    }

    public int getDay() {
        return Integer.parseInt(date.substring(8, 10));
    }

    public int getHour() {
        if (date.length() >= 13) {
            return Integer.parseInt(date.substring(11, 13));
        }
        return 0; // Default to 0 if hour is missing
    }

    public int getMinute() {
        if (date.length() >= 16) {
            return Integer.parseInt(date.substring(14, 16));
        }
        return 0; // Default to 0 if minute is missing
    }

}