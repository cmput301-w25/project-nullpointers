package com.hamidat.nullpointersapp;

import static org.junit.Assert.*;

import com.google.firebase.Timestamp;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.utils.testUtils.TestHelper;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for filtering and sorting logic used in HomeFeedFragment.
 * <p>
 * These tests validate that:
 * - Private moods are only visible to their owners.
 * - Public moods are visible to all users.
 * - Moods are sorted by timestamp in descending order.
 */
public class HomeFeedFragmentTest {

    private List<Mood> testMoods;
    private final String currentUserId = "user123";

    /**
     * Sets up a sample list of Mood objects with various visibility and timestamps.
     * - One public mood from another user
     * - One private mood from the current user
     * - One private mood from another user (should be filtered out)
     */
    @Before
    public void setUp() {
        testMoods = new ArrayList<>();

        // Create moods with timestamps
        testMoods.add(new Mood("Happy", "Public Mood", 10.0, 20.0, "Alone", "user456", false));
        testMoods.add(new Mood("Sad", "Private Mood (Current User)", 30.0, 40.0, "Group", currentUserId, true));
        testMoods.add(new Mood("Angry", "Private Mood (Other User)", 50.0, 60.0, "With Friends", "user789", true));

        // Set timestamps (oldest first)
        testMoods.get(0).setTimestamp(new Timestamp(1700000000, 0)); // Public
        testMoods.get(1).setTimestamp(new Timestamp(1800000000, 0)); // Private (current user)
        testMoods.get(2).setTimestamp(new Timestamp(1600000000, 0)); // Private (other user)
    }

    /**
     * Tests that the filter method correctly includes:
     * - Public moods from any user.
     * - Private moods only if they belong to the current user.
     */
    @Test
    public void testFilterMoods() {
        List<Mood> filteredMoods = TestHelper.filterMoods(testMoods, currentUserId);

        // The filtered list should contain only:
        // - Public mood (user456)
        // - Private mood of the current user (user123)
        assertEquals(2, filteredMoods.size());
        assertEquals("Public Mood", filteredMoods.get(0).getMoodDescription());
        assertEquals("Private Mood (Current User)", filteredMoods.get(1).getMoodDescription());
    }

    /**
     * Tests that moods are sorted by timestamp in descending order (newest first).
     */
    @Test
    public void testSortMoodsByTimestamp() {
        TestHelper.sortMoodsByTimestamp(testMoods);

        // The first item should be the one with the newest timestamp
        assertEquals("Private Mood (Current User)", testMoods.get(0).getMoodDescription());
        assertEquals("Public Mood", testMoods.get(1).getMoodDescription());
        assertEquals("Private Mood (Other User)", testMoods.get(2).getMoodDescription());
    }
}
