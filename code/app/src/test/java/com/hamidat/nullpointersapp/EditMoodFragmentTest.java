package com.hamidat.nullpointersapp;

import static org.junit.Assert.*;

import com.hamidat.nullpointersapp.mainFragments.EditMoodFragment;
import com.hamidat.nullpointersapp.models.Mood;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the update logic in EditMoodFragment.
 * <p>
 * These tests verify that the updateMoodObject method correctly updates the Mood object
 * with new values.
 */
public class EditMoodFragmentTest {

    private EditMoodFragment fragment;
    private Mood testMood;

    /**
     * Sets up the EditMoodFragment instance with a sample Mood object.
     */
    @Before
    public void setUp() {
        // Create a sample Mood object
        testMood = new Mood("Happy", "Old Description", 10.0, 20.0, "Alone", "user123", false);
        testMood.setMoodId("moodId123");
    }

    /**
     * Tests that updateMoodObject updates the Mood object correctly.
     */
    @Test
    public void testUpdateMoodObject() {
        String newDescription = "Updated Description";
        String newMoodType = "Sad";
        String newSocialSituation = "Group";
        double newLatitude = 30.0;
        double newLongitude = 40.0;
        String newImageBase64 = "dummyImageData";

        TestHelper.updateMoodObject(testMood, newDescription, newMoodType, newSocialSituation,
                newLatitude, newLongitude, newImageBase64);


        // Verify updates
        assertEquals("Updated Description", testMood.getMoodDescription());
        assertEquals("Sad", testMood.getMood());
        assertEquals("Group", testMood.getSocialSituation());
        assertEquals(newLatitude, testMood.getLatitude(), 0.001);
        assertEquals(newLongitude, testMood.getLongitude(), 0.001);
        assertEquals("dummyImageData", testMood.getImageBase64());
        assertTrue(testMood.isEdited());
    }

}
