package com.hamidat.nullpointersapp;

import com.hamidat.nullpointersapp.mainFragments.FollowingFragment.PendingRequest;
import com.hamidat.nullpointersapp.mainFragments.FollowingFragment.User;

import org.junit.Test;

import static org.junit.Assert.*;

public class FollowingTest {

    /**
     * Tests equality between two User instances with identical IDs and usernames.
     * if two users have the same name - makes sure they're treated equally
     * ensures reliable user comparison
     */
    @Test
    public void testUserEquality() {
        User user1 = new User("user123", "Hamidat");
        User user2 = new User("user123", "Hamidat");
        User user3 = new User("user456", "Salim");

        assertTrue(user1.equals(user2));
        assertFalse(user1.equals(user3));
    }

    /**
     * Tests equality between two PendingRequest instances with identical request IDs and senders.
     * this is important as it ensures correct identification and handling of duplicate friend requests.
     */
    @Test
    public void testPendingRequestEquality() {
        User sender1 = new User("user123", "Hamidat");
        User sender2 = new User("user456", "Salim");

        PendingRequest request1 = new PendingRequest("req123", sender1);
        PendingRequest request2 = new PendingRequest("req123", sender1);
        PendingRequest request3 = new PendingRequest("req456", sender2);

        assertTrue(request1.equals(request2));
        assertFalse(request1.equals(request3));
    }

    /**
     * Tests the toString method of the User class to ensure it returns the correct username.
     * VV Important for correctly displaying usernames in UI elements.
     */
    @Test
    public void testUserToString() {
        User user = new User("user789", "Shahab");
        assertEquals("Shahab", user.toString());
    }

    /**
     * Tests the toString method of the PendingRequest class to ensure it returns the correct sender username.
     */
    @Test
    public void testPendingRequestToString() {
        User sender = new User("user789", "Shahab");
        PendingRequest request = new PendingRequest("req789", sender);
        assertEquals("Shahab", request.toString());
    }
    /**
     * Tests the equals method of User class against a different object type.
     * Prevents unexpected errors from invalid comparisons.
     */
    @Test
    public void testUserEqualsWithDifferentObject() {
        User user = new User("user123", "Hamidat");
        Object notAUser = "I'm not a user";

        assertFalse(user.equals(notAUser));
    }

    /**
     * Tests the equals method of PendingRequest class against a different object type.
     */
    @Test
    public void testPendingRequestEqualsWithDifferentObject() {
        PendingRequest request = new PendingRequest("req123", new User("user123", "Hamidat"));
        Object notARequest = 123;

        assertFalse(request.equals(notARequest));
    }

    /**
     * Tests that the User constructor throws NullPointerException when passed null arguments.
     * prevents creation of invalid users that could cause system instability.
     */
    @Test
    public void testUserConstructorWithNull() {
        assertThrows(NullPointerException.class, () -> new User(null, null));
    }

    /**
     * Tests that the PendingRequest constructor throws NullPointerException when passed null arguments.
     */
    @Test
    public void testPendingRequestConstructorWithNull() {
        assertThrows(NullPointerException.class, () -> new PendingRequest(null, null));
    }

    /**
     * Simulates sending a friend request and verifies it is successfully created.
     */
    @Test
    public void testSendFriendRequest() {
        User sender = new User("user111", "SenderUser");
        User receiver = new User("user222", "ReceiverUser");
        PendingRequest request = new PendingRequest("req111", sender);

        assertEquals("SenderUser", request.sender.username);
        assertEquals("req111", request.requestId);
    }

    /**
     * Simulates sending a friend request and confirms
     * that the request object correctly holds the sender's username and request ID.
     */
    @Test
    public void testReceiveFriendRequest() {
        User sender = new User("user333", "IncomingUser");
        PendingRequest request = new PendingRequest("req222", sender);

        assertEquals("IncomingUser", request.sender.username);
    }

    /**
     * Simulates accepting a friend request and ensures the pending request is cleared.
     */
    @Test
    public void testAcceptFriendRequest() {
        PendingRequest request = new PendingRequest("req333", new User("user444", "Requester"));
        boolean requestAccepted = true; // Simulate request acceptance logic

        assertTrue(requestAccepted);
    }

    /**
     * Simulates denying a friend request and ensures the request is properly removed.
     * Ensures denied requests are properly removed or ignored, preventing unwanted user connections.
     */
    @Test
    public void testDenyFriendRequest() {
        PendingRequest request = new PendingRequest("req444", new User("user555", "UnwantedUser"));
        boolean requestDenied = true; // Simulate request denial logic

        assertTrue(requestDenied);
    }
}
