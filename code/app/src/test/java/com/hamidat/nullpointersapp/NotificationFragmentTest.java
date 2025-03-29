package com.hamidat.nullpointersapp;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit tests for time formatting logic used in NotificationFragment via {@link TestHelper#getTimeAgo(long)}.
 * These tests ensure time is converted into human-readable "time ago" strings.
 */
public class NotificationFragmentTest {

    /**
     * Tests that a very recent timestamp returns "Just now".
     */
    @Test
    public void testGetTimeAgo_JustNow() {
        long now = System.currentTimeMillis();
        assertEquals("Just now", TestHelper.getTimeAgo(now - 10));
    }

    /**
     * Tests conversion of a timestamp from 2 minutes ago.
     */
    @Test
    public void testGetTimeAgo_MinutesAgo() {
        long now = System.currentTimeMillis();
        String result = TestHelper.getTimeAgo(now - (2 * 60 * 1000)); // 2 minutes ago
        assertEquals("2 minutes ago", result);
    }

    /**
     * Tests conversion of a timestamp from 3 hours ago.
     */
    @Test
    public void testGetTimeAgo_HoursAgo() {
        long now = System.currentTimeMillis();
        String result = TestHelper.getTimeAgo(now - (3 * 60 * 60 * 1000)); // 3 hours ago
        assertEquals("3 hours ago", result);
    }

    /**
     * Tests conversion of a timestamp from 2 days ago.
     */
    @Test
    public void testGetTimeAgo_DaysAgo() {
        long now = System.currentTimeMillis();
        String result = TestHelper.getTimeAgo(now - (2 * 24 * 60 * 60 * 1000)); // 2 days ago
        assertEquals("2 days ago", result);
    }

    /**
     * Tests conversion of a timestamp from 2 weeks ago.
     */
    @Test
    public void testGetTimeAgo_WeeksAgo() {
        long now = System.currentTimeMillis();
        String result = TestHelper.getTimeAgo(now - (14L * 24 * 60 * 60 * 1000)); // 2 weeks ago
        assertEquals("2 weeks ago", result);
    }
}
