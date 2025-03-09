package com.hamidat.nullpointersapp;

import com.hamidat.nullpointersapp.mainFragments.FollowingFragment.PendingRequest;
import com.hamidat.nullpointersapp.mainFragments.FollowingFragment.User;

import org.junit.Test;

import static org.junit.Assert.*;

public class FollowingTest {

    /**
     * Tests equality between two User instances with identical IDs and usernames.
     */
    @Test
    public void testUserEquality() {
        User user1 = new User("user123", "Alice");
        User user2 = new User("user123", "Alice");
        User user3 = new User("user456", "Bob");

        assertTrue(user1.equals(user2));
        assertFalse(user1.equals(user3));
    }

    /**
     * Tests equality between two PendingRequest instances with identical request IDs and senders.
     */
    @Test
    public void testPendingRequestEquality() {
        User sender1 = new User("user123", "Alice");
        User sender2 = new User("user456", "Bob");

        PendingRequest request1 = new PendingRequest("req123", sender1);
        PendingRequest request2 = new PendingRequest("req123", sender1);
        PendingRequest request3 = new PendingRequest("req456", sender2);

        assertTrue(request1.equals(request2));
        assertFalse(request1.equals(request3));
    }

    /**
     * Tests the toString method of the User class to ensure it returns the correct username.
     */
    @Test
    public void testUserToString() {
        User user = new User("user789", "Charlie");
        assertEquals("Charlie", user.toString());
    }

    /**
     * Tests the toString method of the PendingRequest class to ensure it returns the correct sender username.
     */
    @Test
    public void testPendingRequestToString() {
        User sender = new User("user789", "Charlie");
        PendingRequest request = new PendingRequest("req789", sender);
        assertEquals("Charlie", request.toString());
    }}
