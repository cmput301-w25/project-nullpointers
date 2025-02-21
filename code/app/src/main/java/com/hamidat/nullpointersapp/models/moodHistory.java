package com.hamidat.nullpointersapp.models;

import java.util.ArrayList;

/**
 * Represents the history of mood entries for a user.
 *
 * <p>This class stores a list of Mood objects and provides methods to add moods,
 * retrieve the mood history, and filter moods based on certain criteria such as text contained in their description.
 * It is used in conjunction with Firebase queries to populate the history.</p>
 *
 * @author Arden Monaghan
 * @version 1.0
 */
public class moodHistory {
    protected String userName;

    /**
     * The ID of the user to whom this mood history belongs.
     */
    protected String userID;

    /**
     * The list of Mood objects representing the user's mood history.
     */
    protected ArrayList<Mood> moodArray;

    public moodHistory(String userID) {
//        this.userName = userName;
        this.moodArray = new ArrayList<>();
    }

    /**
     * Default constructor. initialize ArrayList
     */
    public moodHistory() {
        this.moodArray = new ArrayList<>();
    }
//    Getters and setters

    public String getUserName() {
        return this.userName;
    }


    /**
     * Returns the user ID associated with this mood history.
     *
     * @return the user's ID.
     */
    public String getUserID() {
        return this.userID;
    }

    /**
     * Retrieves the complete list of mood entries.
     *
     * @return an ArrayList of Mood objects.
     */
    public ArrayList<Mood> getMoodArray() {
        return this.moodArray;
    }

    public void setUserName(String name) {
        this.userName = name;
    }

    /**
     * Sets the user ID for this mood history.
     * userID the unique identifier of the user.
     */
    public void setUserID(String userID) {
        this.userID = userID;
    }

    /**
     * Adds a Mood object to the mood history.
     * @param mood the mood entry to add.
     */
    public void addMood(Mood mood) {
        this.moodArray.add(mood);
    }


    /**
     * Filters the mood history to return only those mood entries whose description contains
     * the specified text.
     *
     * <p>This method performs a case-insensitive search of the mood description.</p>
     *
     * queryText: the text to search for within each mood's description.
     * @return an ArrayList of Mood objects that contain the query text in their description.
     */
    public ArrayList<Mood> filterByText(String queryText) {

        ArrayList<Mood> filteredMoods = new ArrayList<>();

        for (Mood mood : this.moodArray) {
            if (mood.getMoodDescription() != null && mood.getMoodDescription().toLowerCase().contains(queryText.toLowerCase())) {
                filteredMoods.add(mood);
            }
        }
        return filteredMoods;
    }

}
