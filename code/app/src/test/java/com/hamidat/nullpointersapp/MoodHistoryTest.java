package com.hamidat.nullpointersapp;

import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.models.moodHistory;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Unit tests for the {@link moodHistory} class, which stores and filters mood events.
 */
public class MoodHistoryTest {

    private moodHistory history;

    /**
     * Sets up a sample mood history before each test.
     * Adds a few moods to test filtering and retrieval.
     */
    @Before
    public void setUp() {
        history = new moodHistory();
        history.setUserID("user123");
        history.setUserName("TestUser");
        history.addMood(new Mood("Happy", "Had a great day", 1, 1, "Alone", "user123", false));
        history.addMood(new Mood("Sad", "Feeling blue", 1, 1, "With Friends", "user123", false));
        history.addMood(new Mood("Angry", "Someone annoyed me", 1, 1, "Outside", "user123", false));
    }

    /**
     * Tests that the user ID and username are correctly stored and retrieved.
     */
    @Test
    public void testUserIDAndUserName() {
        assertEquals("user123", history.getUserID());
        assertEquals("TestUser", history.getUserName());
    }

    /**
     * Tests adding a new mood to the history.
     */
    @Test
    public void testAddMood() {
        Mood mood = new Mood("Chill", "Relaxing", 1, 1, "Home", "user123", false);
        history.addMood(mood);
        assertTrue(history.getMoodArray().contains(mood));
    }

    /**
     * Tests setting and retrieving the mood list.
     */
    @Test
    public void testSetAndGetMoodArray() {
        ArrayList<Mood> newList = new ArrayList<>();
        Mood mood = new Mood("Confused", "Don’t know what's going on", 1, 1, "Work", "user123", false);
        newList.add(mood);
        history.setMoodArray(newList);
        assertEquals(1, history.getMoodArray().size());
        assertEquals("Confused", history.getMoodArray().get(0).getMood());
    }

    /**
     * Tests filtering moods by a keyword that exists in one description.
     */
    @Test
    public void testFilterByText_MatchExists() {
        ArrayList<Mood> result = history.filterByText("great");
        assertEquals(1, result.size());
        assertEquals("Had a great day", result.get(0).getMoodDescription());
    }

    /**
     * Tests filtering moods when no matching description exists.
     */
    @Test
    public void testFilterByText_NoMatch() {
        ArrayList<Mood> result = history.filterByText("pizza");
        assertEquals(0, result.size());
    }

    /**
     * Tests case-insensitive filtering of mood descriptions.
     */
    @Test
    public void testFilterByText_CaseInsensitive() {
        ArrayList<Mood> result = history.filterByText("BLUE");
        assertEquals(1, result.size());
        assertEquals("Feeling blue", result.get(0).getMoodDescription());
    }
}
