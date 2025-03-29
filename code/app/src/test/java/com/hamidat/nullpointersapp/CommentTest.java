package com.hamidat.nullpointersapp;

import com.google.firebase.Timestamp;
import com.hamidat.nullpointersapp.models.Comment;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the {@link Comment} model class.
 * <p>
 * These tests verify:
 * <ul>
 *     <li>Proper object construction</li>
 *     <li>Correct behavior of getter and setter methods</li>
 *     <li>Handling of the no-argument constructor</li>
 * </ul>
 * <p>
 * This class helps ensure that the {@code Comment} model functions as expected
 * when used in components such as Firestore, adapters, and views.
 */
public class CommentTest {

    private Comment comment;
    private Timestamp timestamp;

    /**
     * Set up a Comment object before each test.
     */
    @Before
    public void setUp() {
        timestamp = new Timestamp(new java.util.Date());
        comment = new Comment("user123", "This is a test comment", timestamp);
    }

    /**
     * Test that the constructor correctly initializes fields,
     * and getters return the correct values.
     */
    @Test
    public void testConstructorAndGetters() {
        assertEquals("user123", comment.getUserId());
        assertEquals("This is a test comment", comment.getCommentText());
        assertEquals(timestamp, comment.getTimestamp());
    }

    /**
     * Test that setter methods correctly update each field.
     */
    @Test
    public void testSetters() {
        Timestamp newTimestamp = new Timestamp(new java.util.Date());

        comment.setUserId("newUser456");
        comment.setCommentText("Updated comment");
        comment.setTimestamp(newTimestamp);

        assertEquals("newUser456", comment.getUserId());
        assertEquals("Updated comment", comment.getCommentText());
        assertEquals(newTimestamp, comment.getTimestamp());
    }

    /**
     * Test the no-argument constructor to ensure it initializes fields to null.
     */
    @Test
    public void testNoArgConstructor() {
        Comment emptyComment = new Comment();
        assertNull(emptyComment.getUserId());
        assertNull(emptyComment.getCommentText());
        assertNull(emptyComment.getTimestamp());
    }
}
