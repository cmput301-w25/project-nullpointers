package com.hamidat.nullpointersapp;

import java.util.ArrayList;

public class moodHistory {
    protected String userName;
    protected String userID;

    protected ArrayList<Mood> moodArray;

    public moodHistory(String userID) {
        this.userID = userID;
//        this.userName = userName;
        this.moodArray = new ArrayList<>();
    }
//    Required no-arg constructor.
    public moodHistory() {
        this.moodArray = new ArrayList<>();
    }

//    Getters and setters

    public String getUserName() {
        return this.userName;
    }

    public String getUserID() {
        return this.userID;
    }
    public ArrayList<Mood> getMoodArray() {
        return this.moodArray;
    }

    public void setUserName(String name) {
        this.userName = name;
    }

//    Functionality for moodHistory

//    Method for adding data. deleting and editing to the moodHistory array.
    public void addMood(Mood mood) {
        this.moodArray.add(mood);
    }

//    public ArrayList<Mood> sortByRecent() {
//    }

//    public ArrayList<Mood> filterByText() {
//
//    }





}
