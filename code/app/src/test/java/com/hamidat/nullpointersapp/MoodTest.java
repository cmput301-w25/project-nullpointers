package com.hamidat.nullpointersapp;

import com.google.firebase.Timestamp;
import com.hamidat.nullpointersapp.models.Mood;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the {@link Mood} model class.
 * Ensures proper behavior of getters and setters for all fields.
 */
public class MoodTest {

    private Mood mood;
    private Timestamp testTimestamp;

    /**
     * Sets up a Mood object with test data before each test runs.
     */
    @Before
    public void setUp() {
        testTimestamp = new Timestamp(new java.util.Date());
        mood = new Mood("Happy", "Feeling good", 53.5461, -113.4938, "Alone", "user123", false);
        mood.setMoodId("mood123");
        mood.setTimestamp(testTimestamp);
        mood.setImageBase64("base64string");
        mood.setEdited(true);
    }

    /**
     * Tests all getter methods to ensure they return correct values.
     */
    @Test
    public void testGetters() {
        assertEquals("Happy", mood.getMood());
        assertEquals("Feeling good", mood.getMoodDescription());
        assertEquals(53.5461, mood.getLatitude(), 0.0001);
        assertEquals(-113.4938, mood.getLongitude(), 0.0001);
        assertEquals("Alone", mood.getSocialSituation());
        assertEquals("user123", mood.getUserId());
        assertEquals("mood123", mood.getMoodId());
        assertEquals("base64string", mood.getImageBase64());
        assertEquals(testTimestamp, mood.getTimestamp());
        assertFalse(mood.isPrivate());
        assertTrue(mood.isEdited());
    }

    /**
     * Tests all setter methods to ensure they correctly update fields.
     */
    @Test
    public void testSetters() {
        mood.setMood("Sad");
        mood.setMoodDescription("Tired");
        mood.setLatitude(51.0447);
        mood.setLongitude(-114.0719);
        mood.setSocialSituation("With Friends");
        mood.setUserId("newUser456");
        mood.setMoodId("newMood456");
        mood.setImageBase64("newBase64");
        mood.setPrivate(true);
        mood.setEdited(false);

        assertEquals("Sad", mood.getMood());
        assertEquals("Tired", mood.getMoodDescription());
        assertEquals(51.0447, mood.getLatitude(), 0.0001);
        assertEquals(-114.0719, mood.getLongitude(), 0.0001);
        assertEquals("With Friends", mood.getSocialSituation());
        assertEquals("newUser456", mood.getUserId());
        assertEquals("newMood456", mood.getMoodId());
        assertEquals("newBase64", mood.getImageBase64());
        assertTrue(mood.isPrivate());
        assertFalse(mood.isEdited());
    }
}
