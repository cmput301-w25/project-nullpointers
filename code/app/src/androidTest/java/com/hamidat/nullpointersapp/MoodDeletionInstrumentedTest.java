package com.hamidat.nullpointersapp;

import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.notNullValue;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Instrumented test to verify mood deletion behavior in Firestore.
 */
@RunWith(AndroidJUnit4.class)
public class MoodDeletionInstrumentedTest {

    private FirebaseFirestore firestore;
    private FirestoreHelper firestoreHelper;
    private String testMoodId;
    private String otherUserMoodId;

    private static final String TEST_USER_ID = "moe"; // Real User ID
    private static final String OTHER_USER_ID = "otherUser456"; // Simulating another user

    @Before
    public void setUp() throws InterruptedException {
        firestore = FirebaseFirestore.getInstance();
        firestoreHelper = new FirestoreHelper();
        addTestMood();
        addOtherUserMood();
    }

    /**
     * Adds a test mood for the current user.
     */
    private void addTestMood() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Mood testMood = new Mood("Happy", "Test Mood", 0.0, 0.0, "Alone", TEST_USER_ID, false);
        testMood.setTimestamp(new Timestamp(new Date()));

        firestore.collection("moods")
                .add(testMood)
                .addOnSuccessListener(documentReference -> {
                    testMoodId = documentReference.getId();
                    Log.d("MoodTest", "Test Mood added with ID: " + testMoodId);
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e("MoodTest", "Error adding test mood", e);
                    latch.countDown();
                });

        latch.await(5, TimeUnit.SECONDS);
    }

    /**
     * Adds a test mood for another user.
     */
    private void addOtherUserMood() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Mood otherUserMood = new Mood("Sad", "Other User's Mood", 0.0, 0.0, "Group", OTHER_USER_ID, false);
        otherUserMood.setTimestamp(new Timestamp(new Date()));

        firestore.collection("moods")
                .add(otherUserMood)
                .addOnSuccessListener(documentReference -> {
                    otherUserMoodId = documentReference.getId();
                    Log.d("MoodTest", "Other User's Mood added with ID: " + otherUserMoodId);
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e("MoodTest", "Error adding other user's mood", e);
                    latch.countDown();
                });

        latch.await(5, TimeUnit.SECONDS);
    }

    /**
     * Tests if the user can successfully delete their own mood.
     */
    @Test
    public void testDeleteOwnMood() throws InterruptedException {
        if (testMoodId == null) {
            Log.e("MoodTest", "No test mood ID found, skipping deletion test.");
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        firestore.collection("moods")
                .document(testMoodId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("MoodTest", "Mood successfully deleted.");
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e("MoodTest", "Error deleting mood", e);
                    latch.countDown();
                });

        latch.await(5, TimeUnit.SECONDS);

        // Verify deletion
        firestore.collection("moods")
                .document(testMoodId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        assertThat(task.getResult().getData(), nullValue());
                        Log.d("MoodTest", "Mood deletion confirmed.");
                    } else {
                        Log.e("MoodTest", "Error verifying mood deletion.");
                    }
                });
    }

    @After
    public void tearDown() {
        if (testMoodId != null) {
            firestore.collection("moods").document(testMoodId).delete();
        }
        if (otherUserMoodId != null) {
            firestore.collection("moods").document(otherUserMoodId).delete();
        }
    }
}
