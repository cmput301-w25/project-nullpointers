package com.hamidat.nullpointersapp.moodClasses;

import android.util.Log;

import java.util.ArrayList;

public class moodHistory {
    protected String userName;
    protected String userID;
    protected ArrayList<Mood> moodArray;

    public moodHistory(String userID) {
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

    public void setUserID(String userID) {
        this.userID = userID;
    }

//    Functionality for moodHistory

//    Method for adding data. deleting and editing to the moodHistory array.
    public void addMood(Mood mood) {
        this.moodArray.add(mood);
    }

    public ArrayList<Mood> filterByText(String queryText) {
//        filterByText works with moodHistory and a firebase query.
//        First we need to query the DB and then call this function on the moods added to mood history to see if they contain the specific query text.
//        Args: String queryText

//        Need to inquire if capital text should be accounted for or not.

        ArrayList<Mood> filteredMoods = new ArrayList<>();



        for (Mood mood : this.moodArray) {
            Log.d("FilterTest", "Mood Description: " + mood.getMoodDescription());
            if (mood.getMoodDescription() != null && mood.getMoodDescription().toLowerCase().contains(queryText.toLowerCase())) {
                filteredMoods.add(mood);
//                Log.d("FilterTest", "This was run");
            }
        }
        return filteredMoods;
    }

}
