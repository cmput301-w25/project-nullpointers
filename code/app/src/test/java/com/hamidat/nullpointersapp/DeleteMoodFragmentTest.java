package com.hamidat.nullpointersapp;

import static org.junit.Assert.*;

import com.hamidat.nullpointersapp.models.Mood;

import org.junit.Test;

/**
 * Unit tests for verifying the behavior of {@link TestHelper#isValidMoodForDeletion(Mood)}.
 * These tests check whether the provided Mood object is valid for deletion based on its ID.
 */
public class DeleteMoodFragmentTest {

    /**
     * Tests that a Mood with a non-null, non-empty ID is considered valid for deletion.
     */
    @Test
    public void testValidMoodForDeletion() {
        Mood mood = new Mood();
        mood.setMoodId("mood123");
        boolean result = TestHelper.isValidMoodForDeletion(mood);
        assertTrue(result);
    }

    /**
     * Tests that a null Mood object is considered invalid for deletion.
     */
    @Test
    public void testInvalidMoodForDeletion_NullMood() {
        Mood mood = null;
        boolean result = TestHelper.isValidMoodForDeletion(mood);
        assertFalse(result);
    }

    /**
     * Tests that a Mood with an empty moodId string is considered invalid for deletion.
     */
    @Test
    public void testInvalidMoodForDeletion_EmptyId() {
        Mood mood = new Mood();
        mood.setMoodId("");
        boolean result = TestHelper.isValidMoodForDeletion(mood);
        assertFalse(result);
    }

    /**
     * Tests that a Mood with a null moodId is considered invalid for deletion.
     */
    @Test
    public void testInvalidMoodForDeletion_NullId() {
        Mood mood = new Mood();
        mood.setMoodId(null);
        boolean result = TestHelper.isValidMoodForDeletion(mood);
        assertFalse(result);
    }
}
