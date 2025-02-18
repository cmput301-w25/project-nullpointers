package com.hamidat.nullpointersapp;

import java.util.ArrayList;

public class moodHistory {
    protected String userName;
    protected ArrayList<Mood> moodArray;

    public moodHistory(String userName) {
        this.userName = userName;
        this.moodArray = new ArrayList<>();
    }
//    Required no-arg constructor
    public moodHistory() {}

//    Method for adding data. deleting and editing to the moodHistory array.
    public void addMood(Mood mood) {
        this.moodArray.add(mood);
    }
    public void deleteMood(Mood mood) {
        this.moodArray.remove(mood);
    }

    public void editMood(Mood oldMood, Mood updatedMood) {
        int index = this.moodArray.indexOf(oldMood);
        if (index != -1) {
            this.moodArray.set(index, updatedMood);
        }
    }

//    Getters
    public String getUserName() {
        return this.userName;
    }
    public ArrayList<Mood> getMoodArray() {
        return this.moodArray;
    }

}
