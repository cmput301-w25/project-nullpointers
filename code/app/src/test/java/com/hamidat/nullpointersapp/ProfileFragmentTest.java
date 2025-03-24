package com.hamidat.nullpointersapp;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit tests for helper logic used in ProfileFragment via {@link TestHelper}.
 */
public class ProfileFragmentTest {

    /**
     * Tests that a given username is correctly formatted for display.
     */
    @Test
    public void testFormatUsernameDisplay() {
        String username = "maatez";
        String expected = "My Username: maatez";
        assertEquals(expected, TestHelper.formatUsernameDisplay(username));
    }

    /**
     * Tests that decoding an invalid Base64 string returns null.
     */
    @Test
    public void testDecodeBase64InvalidString() {
        String invalid = "NotBase64!!";
        byte[] decoded = TestHelper.decodeBase64(invalid);
        assertNull(decoded);
    }
}
