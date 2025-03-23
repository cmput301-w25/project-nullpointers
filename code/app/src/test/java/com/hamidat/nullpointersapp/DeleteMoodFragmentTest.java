package com.hamidat.nullpointersapp;

import static org.junit.Assert.*;

import com.hamidat.nullpointersapp.models.Mood;

import org.junit.Test;

public class DeleteMoodFragmentTest {

    @Test
    public void testValidMoodForDeletion() {
        Mood mood = new Mood();
        mood.setMoodId("mood123");

        boolean result = TestHelper.isValidMoodForDeletion(mood);
        assertTrue(result);
    }

    @Test
    public void testInvalidMoodForDeletion_NullMood() {
        Mood mood = null;
        boolean result = TestHelper.isValidMoodForDeletion(mood);
        assertFalse(result);
    }

    @Test
    public void testInvalidMoodForDeletion_EmptyId() {
        Mood mood = new Mood();
        mood.setMoodId("");
        boolean result = TestHelper.isValidMoodForDeletion(mood);
        assertFalse(result);
    }

    @Test
    public void testInvalidMoodForDeletion_NullId() {
        Mood mood = new Mood();
        mood.setMoodId(null);
        boolean result = TestHelper.isValidMoodForDeletion(mood);
        assertFalse(result);
    }
}
