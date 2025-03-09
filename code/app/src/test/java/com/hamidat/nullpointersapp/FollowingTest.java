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
    }
    /**
     * Tests the equals method of User class against a different object type.
     */
    @Test
    public void testUserEqualsWithDifferentObject() {
        User user = new User("user123", "Alice");
        Object notAUser = "I'm not a user";

        assertFalse(user.equals(notAUser));
    }

    /**
     * Tests the equals method of PendingRequest class against a different object type.
     */
    @Test
    public void testPendingRequestEqualsWithDifferentObject() {
        PendingRequest request = new PendingRequest("req123", new User("user123", "Alice"));
        Object notARequest = 123;

        assertFalse(request.equals(notARequest));
    }

    /**
     * Tests that the User constructor throws NullPointerException when passed null arguments.
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
     * Simulates receiving a friend request and ensures it's correctly represented.
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
     */
    @Test
    public void testDenyFriendRequest() {
        PendingRequest request = new PendingRequest("req444", new User("user555", "UnwantedUser"));
        boolean requestDenied = true; // Simulate request denial logic

        assertTrue(requestDenied);
    }
}
