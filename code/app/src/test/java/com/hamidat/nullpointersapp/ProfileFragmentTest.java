package com.hamidat.nullpointersapp;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit tests for helper logic in ProfileFragment.
 */
public class ProfileFragmentTest {

    @Test
    public void testFormatUsernameDisplay() {
        String username = "maatez";
        String expected = "My Username: maatez";
        assertEquals(expected, TestHelper.formatUsernameDisplay(username));
    }

    @Test
    public void testDecodeBase64InvalidString() {
        String invalid = "NotBase64!!";
        byte[] decoded = TestHelper.decodeBase64(invalid);
        assertNull(decoded);
    }
}
