package com.hamidat.nullpointersapp;

import static android.content.ContentValues.TAG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.graphics.Movie;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.hamidat.nullpointersapp.models.Mood;

import androidx.navigation.Navigation;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.models.moodHistory;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;
import com.hamidat.nullpointersapp.utils.mapUtils.AppEventBus;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class FirestoreMoodTest {

    private static final String MOODS_COLLECTION = "moods";
    private FirebaseFirestore firestore;
    private FirestoreHelper firestoreHelper;

    private String userId;


    @BeforeClass
    public static void setup(){
        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";
        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
    }

    @Before
    public void seedDatabase() throws InterruptedException {
        // Seed the database with a new user which will be used to add a mood to.

        CountDownLatch latch = new CountDownLatch(1);

        firestore = FirebaseFirestore.getInstance();
        firestoreHelper = new FirestoreHelper();
        String newUsername = "Arden";
        String newPassword = "Arden123";

        // Adding a new user with the User's table.
        firestoreHelper.addUser(newUsername, newPassword, new FirestoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                Log.d("FirestoreTest", "User added successfully: " + result);
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                Log.e("FirestoreTest", "Failed to add user: " + e.getMessage());
                latch.countDown();
            }
        });

        // Wait up to 10 seconds for the callback to complete.
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.d(TAG, "Continue...");
        }

    }

    @Before
    public void retrieveUserId() {
        // Function to retrieve User ID and test if functionality works.

        final String[] usernameHolder = new String[1];
        final String[] passwordHolder = new String[1];

        CountDownLatch latch = new CountDownLatch(1);

        firestoreHelper.getUserByUsername("Arden", new FirestoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                Map<String, Object> userData = (Map<String, Object>) result;
                usernameHolder[0] = (String) userData.get("username");
                passwordHolder[0] = (String) userData.get("password");
                userId = (String) userData.get("userId");
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("Error retrieving user: " + e.getMessage());
                latch.countDown();
            }

        });
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.d(TAG, "Continue...");
        }

        assertEquals("Arden", usernameHolder[0]);
        assertEquals("Arden123", passwordHolder[0]);
    }

    @Test
    public void createMoodInDB() {
        // Checking if a mood can be created and displayed inside of the database.

        CountDownLatch latch = new CountDownLatch(1);
        final String[] callbackResult = new String[1];

        // Creating a Dummy Mood.
        String moodID = UUID.randomUUID().toString();

        // Create a new mood inside of the moods collection
        DocumentReference moodRef = firestore.collection(MOODS_COLLECTION).document(moodID);

        // Simple mood with no image , latitude or longitude
        Mood testMood = new Mood();
        testMood.setMood("Happy");
        testMood.setMoodDescription("I am happy");
        testMood.setSocialSituation("Group");

        FirestoreHelper.FirestoreCallback moodCallback = new FirestoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    AppEventBus.getInstance().post(new AppEventBus.MoodAddedEvent());
                });
                callbackResult[0] = (String) result;
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                // Fail to add mood to firebase Test Case Failed.
                fail("Error adding mood: " + e.getMessage());
                latch.countDown();
            }
        };

        firestoreHelper.addMood(userId, testMood, moodCallback);

        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.d(TAG, "Continue...");
        }
    }

//    @Test
//    public void retrieveMoodFromDB() {
//        // Testing to see If we can retrieve all the moods from the current user.
//
//        CountDownLatch latch = new CountDownLatch(1);
//        final String[] callbackResult = new String[1];
//
//    }

}
