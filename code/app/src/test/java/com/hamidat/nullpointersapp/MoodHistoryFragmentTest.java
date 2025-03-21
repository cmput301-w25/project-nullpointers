package com.hamidat.nullpointersapp;

import static org.junit.Assert.*;

import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.utils.testUtils.TestHelper;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for the mood history filtering logic using TestHelper.
 */
public class MoodHistoryFragmentTest {

    /**
     * Generates a sample list of Mood objects with different mood types.
     *
     * @return A list of sample Mood objects.
     */
    private List<Mood> getSampleMoods() {
        List<Mood> moods = new ArrayList<>();
        moods.add(new Mood("Happy", "desc", 0, 0, "Alone", "user", false));
        moods.add(new Mood("Sad", "desc", 0, 0, "Alone", "user", false));
        moods.add(new Mood("Angry", "desc", 0, 0, "Alone", "user", false));
        moods.add(new Mood("Chill", "desc", 0, 0, "Alone", "user", false));
        return moods;
    }

    /**
     * Tests that all moods are returned when "Show All" is true.
     */
    @Test
    public void testFilterShowAll() {
        List<Mood> moods = getSampleMoods();
        List<Mood> result = TestHelper.filterMoods(moods, true, false, false, false, false);
        assertEquals(4, result.size());
    }

    /**
     * Tests that only "Happy" moods are returned when selected.
     */
    @Test
    public void testFilterHappyOnly() {
        List<Mood> moods = getSampleMoods();
        List<Mood> result = TestHelper.filterMoods(moods, false, true, false, false, false);
        assertEquals(1, result.size());
        assertEquals("Happy", result.get(0).getMood());
    }

    /**
     * Tests that multiple selected moods (e.g. Happy, Angry) are filtered correctly.
     */
    @Test
    public void testFilterMultiple() {
        List<Mood> moods = getSampleMoods();
        List<Mood> result = TestHelper.filterMoods(moods, false, true, false, true, false);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(m -> m.getMood().equals("Happy")));
        assertTrue(result.stream().anyMatch(m -> m.getMood().equals("Angry")));
    }

    /**
     * Tests that no moods are returned when no filter is selected and "Show All" is false.
     */
    @Test
    public void testFilterNoneSelected() {
        List<Mood> moods = getSampleMoods();
        List<Mood> result = TestHelper.filterMoods(moods, false, false, false, false, false);
        assertEquals(0, result.size());
    }
}

