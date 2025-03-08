package com.hamidat.nullpointersapp;

import static org.junit.Assert.*;

import com.hamidat.nullpointersapp.models.UserProfile;
import org.junit.Before;
import org.junit.Test;

/*
This is for testing everything related to the UserProfile Fragment
 */
public class UserProfileTest {
    private static final int MAX_USERNAME_LENGTH = 20;
    private UserProfile userProfile;

    // testing using the guestUser Account
    @Before
    public void setUp() {
        userProfile = new UserProfile("guestUser");
    }

    // test that w
    @Test
    public void testConstructor_ValidUsername() {
        assertEquals("guestUser", userProfile.getUsername());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_TooLongUsername() {
        new UserProfile("ThisUsernameIsWayTooLong");
    }

    @Test
    public void testSetUsername_Valid() {
        userProfile.setUsername("NewUsername");
        assertEquals("NewUsername", userProfile.getUsername());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetUsername_TooLong() {
        userProfile.setUsername("ThisUsernameIsWayTooLongPlsHelp");
    }

    @Test
    public void testIsUsernameValid_ValidUsername() {
        assertTrue(userProfile.isUsernameValid());
    }

    @Test
    public void testIsUsernameValid_InvalidUsername() {
        UserProfile userProfile = new UserProfile("hamidatIsValid"); // start with a valid username (me)

        try {
            userProfile.setUsername("A".repeat(MAX_USERNAME_LENGTH + 1)); // change the username to be too long
            fail("Expected IllegalArgumentException for long username");
        } catch (IllegalArgumentException e) {
            // code should throw an exception if the username exceeds 20 chars
            assertEquals("Username cannot exceed 20 characters.", e.getMessage());
        }
    }
}
