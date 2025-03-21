package com.hamidat.nullpointersapp.mainFragments;

import com.hamidat.nullpointersapp.TestHelper;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for helper methods related to following logic.
 */
public class FollowingFragmentTest {

    private List<FollowingFragment.User> testFollowingList;

    /**
     * Sets up the test environment with sample users.
     */
    @Before
    public void setUp() {
        testFollowingList = new ArrayList<>();
        testFollowingList.add(new FollowingFragment.User("1", "Alice"));
        testFollowingList.add(new FollowingFragment.User("2", "Bob"));
    }

    /**
     * Tests username validation logic.
     */
    @Test
    public void testValidUsername() {
        assertTrue(TestHelper.isUsernameValid("JohnDoe"));
        assertFalse(TestHelper.isUsernameValid(""));
        assertFalse(TestHelper.isUsernameValid("   "));
        assertFalse(TestHelper.isUsernameValid(null));
    }

    /**
     * Tests whether the helper detects if a user is followed.
     */
    @Test
    public void testIsUserFollowed() {
        assertTrue(TestHelper.isUserFollowed("1", testFollowingList));
        assertTrue(TestHelper.isUserFollowed("2", testFollowingList));
        assertFalse(TestHelper.isUserFollowed("3", testFollowingList));
    }

    /**
     * Tests logic for validating follow/unfollow actions.
     */
    @Test
    public void testFollowActionValidity() {
        assertTrue(TestHelper.isFollowActionValid("1", testFollowingList, true));
        assertFalse(TestHelper.isFollowActionValid("3", testFollowingList, true));
        assertTrue(TestHelper.isFollowActionValid("3", testFollowingList, false));
        assertFalse(TestHelper.isFollowActionValid("1", testFollowingList, false));
    }
}
