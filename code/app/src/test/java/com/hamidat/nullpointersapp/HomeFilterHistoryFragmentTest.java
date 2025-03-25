package com.hamidat.nullpointersapp;

import static org.junit.Assert.*;

import com.hamidat.nullpointersapp.models.Mood;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Unit tests for {@link TestHelper#filterMoodsByDescription(ArrayList, String)}.
 * These tests verify the filtering of mood objects based on description keywords.
 */
public class HomeFilterHistoryFragmentTest {

    private ArrayList<Mood> moodList;

    /**
     * Sets up a sample mood list before each test.
     * Includes moods with and without matching descriptions.
     */
    @Before
    public void setUp() {
        moodList = new ArrayList<>();
        moodList.add(new Mood("Happy", "I feel great today", 10.0, 20.0, "Alone", "user1", false));
        moodList.add(new Mood("Sad", "It's a gloomy and anxious day", 10.0, 20.0, "Group", "user2", false));
        moodList.add(new Mood("Angry", "Feeling frustrated at work", 10.0, 20.0, "Alone", "user3", false));
        moodList.add(new Mood("Chill", "", 10.0, 20.0, "Group", "user4", false));
    }

    /**
     * Tests filtering when a keyword matches one mood description.
     * Expects a single result containing the matching mood.
     */
    @Test
    public void testFilterMoodsByDescription_MatchFound() {
        ArrayList<Mood> filtered = TestHelper.filterMoodsByDescription(moodList, "anxious");
        assertEquals(1, filtered.size());
        assertEquals("Sad", filtered.get(0).getMood());
    }

    /**
     * Tests filtering when an empty keyword is provided.
     * Expects the entire list to be returned without filtering.
     */
    @Test
    public void testFilterMoodsByDescription_EmptyKeyword() {
        ArrayList<Mood> filtered = TestHelper.filterMoodsByDescription(moodList, "");
        assertEquals(moodList.size(), filtered.size()); // Should return all
    }

    /**
     * Tests filtering when the keyword doesn't match any mood description.
     * Expects an empty result.
     */
    @Test
    public void testFilterMoodsByDescription_NoMatch() {
        ArrayList<Mood> filtered = TestHelper.filterMoodsByDescription(moodList, "vacation");
        assertEquals(0, filtered.size()); // No mood matches "vacation"
    }
}
