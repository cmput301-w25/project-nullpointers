package com.hamidat.nullpointersapp;

import static org.junit.Assert.*;

import com.hamidat.nullpointersapp.models.Mood;

import org.junit.Test;

/**
 * Unit tests for Mood creation logic using TestHelper.
 */
public class AddMoodFragmentTest {

    /**
     * Verifies that a Mood object is created correctly when no image is provided.
     * Ensures all fields are set accurately and image is null.
     */
    @Test
    public void testCreateMoodWithoutImage() {
        String reasonText = "Feeling great!";
        String moodType = "Happy";
        String socialSituation = "Alone";
        double latitude = 12.34;
        double longitude = 56.78;
        String currentUserId = "user123";
        boolean isPrivate = false;
        String base64Image = null;

        Mood mood = TestHelper.createMood(reasonText, moodType, socialSituation,
                latitude, longitude, currentUserId, isPrivate, base64Image);

        assertNotNull(mood);
        assertEquals(reasonText, mood.getMoodDescription());
        assertEquals(moodType, mood.getMood());
        assertEquals(socialSituation, mood.getSocialSituation());
        assertEquals(latitude, mood.getLatitude(), 0.001);
        assertEquals(longitude, mood.getLongitude(), 0.001);
        assertEquals(currentUserId, mood.getUserId());
        assertEquals(isPrivate, mood.isPrivate());
        assertNull(mood.getImageBase64());
    }

    /**
     * Verifies that a Mood object is created correctly when an image is provided.
     * Ensures all fields, including the Base64 image string, are set accurately.
     */
    @Test
    public void testCreateMoodWithImage() {
        String reasonText = "Feeling artistic!";
        String moodType = "Creative";
        String socialSituation = "With Friends";
        double latitude = 98.76;
        double longitude = 54.32;
        String currentUserId = "user456";
        boolean isPrivate = true;
        String base64Image = "dummyImageData";

        Mood mood = TestHelper.createMood(reasonText, moodType, socialSituation,
                latitude, longitude, currentUserId, isPrivate, base64Image);

        assertNotNull(mood);
        assertEquals(reasonText, mood.getMoodDescription());
        assertEquals(moodType, mood.getMood());
        assertEquals(socialSituation, mood.getSocialSituation());
        assertEquals(latitude, mood.getLatitude(), 0.001);
        assertEquals(longitude, mood.getLongitude(), 0.001);
        assertEquals(currentUserId, mood.getUserId());
        assertEquals(isPrivate, mood.isPrivate());
        assertEquals(base64Image, mood.getImageBase64());
    }
}
