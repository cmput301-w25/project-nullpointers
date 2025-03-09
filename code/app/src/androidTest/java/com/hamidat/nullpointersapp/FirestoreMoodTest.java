package com.hamidat.nullpointersapp;

import static android.content.ContentValues.TAG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.graphics.Movie;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.hamidat.nullpointersapp.models.Mood;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.firestore.FirebaseFirestore;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.models.MoodAdapter;
import com.hamidat.nullpointersapp.models.moodHistory;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;
import com.hamidat.nullpointersapp.utils.mapUtils.AppEventBus;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class FirestoreMoodTest {
    private static final String MOODS_COLLECTION = "moods";
    private FirebaseFirestore firestore;
    private FirestoreHelper firestoreHelper;
    private static final String FIXED_USER_ID = "testUser123";

//    private String userId = "testUser123";

    /**
     * Waits up to 5 seconds for the provided CountDownLatch to reach zero.
     * If the latch does not count down within the timeout period, a debug log is printed.
     *
     * @param latch the CountDownLatch to wait on.
     */
    public static void latch5seconds(CountDownLatch latch) {
        // Checks to see if countdown has successfully count down within 5 seconds.
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.d(TAG, "Continue...");
        }
    }

    /**
     * Seeds the Firestore database with a new user that will be used for adding a mood.
     * This method creates a new user document in the "users" collection using the specified
     * username and password. It then waits for the asynchronous operation to complete using {@code latch5seconds}.
     */
    public String seedDatabase(String userId, String newUsername, String newPassword) {
        // Seed the database with a new user which will be used to add a mood to.

        CountDownLatch latch = new CountDownLatch(1);

        firestore = FirebaseFirestore.getInstance();
        firestoreHelper = new FirestoreHelper();

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", newUsername);
        userData.put("password", newPassword);

        firestore.collection("users").document(userId).set(userData);

        // Wait up to 5 seconds for the callback to complete.
        latch5seconds(latch);

        return userId;

    }

    /**
     * Returns a {@code FirestoreCallback} that decrements the provided CountDownLatch when
     * the asynchronous mood operation completes. On a successful mood operation, the callback
     * posts a {@code MoodAddedEvent} to the {@code AppEventBus}. On failure, the callback fails the test.
     *
     * @param latch a CountDownLatch that will be decremented when the asynchronous operation completes.
     * @return a FirestoreCallback suitable for use with asynchronous mood operations.
     */
    public FirestoreHelper.FirestoreCallback getMoodCallback(CountDownLatch latch) {
        FirestoreHelper.FirestoreCallback moodCallback = new FirestoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    AppEventBus.getInstance().post(new AppEventBus.MoodAddedEvent());
                });
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                // Fail to add mood to firebase Test Case Failed.
                fail("Error adding mood: " + e.getMessage());
                latch.countDown();
            }
        };

       return moodCallback;
    }

    @BeforeClass
    public static void setup(){
        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";
        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
    }

    @Test
    public void queryUserName() {
        // Function to check if Querying a username gets the correct user.

        final String[] usernameHolder = new String[1];
        final String[] passwordHolder = new String[1];

        CountDownLatch latch = new CountDownLatch(1);

        // Initializing value inside of the DB.
        String userId;
        String newUsername = "Arden";
        String newPassword = "Arden123";
        userId = seedDatabase("UserId1", newUsername, newPassword);

        // Now query on username being Arden.
        firestoreHelper.getUserByUsername("Arden", new FirestoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {

                // Expect a Map as our return value
                Map<String, Object> userData = (Map<String, Object>) result;
                usernameHolder[0] = (String) userData.get("username");
                passwordHolder[0] = (String) userData.get("password");
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("Error retrieving user: " + e.getMessage());
                latch.countDown();
            }
        });

        // Wait up to 5 seconds for the callback to complete.
        latch5seconds(latch);

        // Checking if retrieving the User is correct.
        assertEquals("Arden", usernameHolder[0]);
        assertEquals("Arden123", passwordHolder[0]);

    }

    @Test
    public void createMoodInDB() {
        // Checking if a mood can be created and displayed inside of the database.

        CountDownLatch latch = new CountDownLatch(1);

        String userId;
        String newUsername = "Hamidat";
        String newPassword = "Hamidat123";
        userId = seedDatabase("UserId2", newUsername, newPassword);

        // Creating Dummy Moods.
        Mood testMood = new Mood();
        testMood.setMood("Happy");
        testMood.setMoodDescription("I am happy");
        testMood.setSocialSituation("Group");

        Mood testMood2 = new Mood();
        testMood2.setMood("Sad");
        testMood2.setMoodDescription("I am quite sad");
        testMood2.setSocialSituation("One-On-One");

        // Set testMood1 and testMood2 to the same userId
        firestoreHelper.addMood(userId, testMood, getMoodCallback(latch));
        firestoreHelper.addMood(userId, testMood2, getMoodCallback(latch));

        // Wait up to 5 seconds for the callback to complete.
        latch5seconds(latch);
    }

    @Test
    public void createMoodWithImage() {
        // Checking if a mood can be created with data and has a dummy image set to it.

        CountDownLatch latch = new CountDownLatch(1);
        String exampleBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PQ7TeAAAAABJRU5ErkJggg==";

        String userId;
        String newUsername = "Shahab";
        String newPassword = "Shahab123";
        userId = seedDatabase("UserId3", newUsername, newPassword);

        Mood testMood = new Mood();
        testMood.setMood("Angry");
        testMood.setMoodDescription("I am quite angry");
        testMood.setSocialSituation("Group");

        // Setting image
        testMood.setImageBase64(exampleBase64);
        firestoreHelper.addMoodWithPhoto(userId, testMood, new FirestoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                latch.countDown();
            }
            @Override
            public void onFailure(Exception e) {
                latch.countDown();
            }
        });

        // Wait up to 5 seconds for the callback to complete and check DB.
        latch5seconds(latch);
    }

    @Test
    public void retrieveMoodFromDB() {
        // Testing to see If we can retrieve all the moods from the current user after they are added into the DB.

        CountDownLatch latch = new CountDownLatch(1);
        String userId;
        String newUsername = "Ogua";
        String newPassword = "Ogua123";
        userId = seedDatabase("UserId4", newUsername, newPassword);

        Mood testMood = new Mood();
        testMood.setMood("Angry");
        testMood.setMoodDescription("I am Angry");
        testMood.setSocialSituation("Group");

        Mood testMood2 = new Mood();
        testMood2.setMood("Sad");
        testMood2.setMoodDescription("I am quite sad");
        testMood2.setSocialSituation("One-On-One");
        testMood2.setLatitude(10.53);
        testMood2.setLongitude(20.24);

        // Set testMood1 and testMood2 to the same userId
        firestoreHelper.addMood(userId, testMood, getMoodCallback(latch));
        firestoreHelper.addMood(userId, testMood2, getMoodCallback(latch));

        // arrayList of userIds as parameter.
        ArrayList<String> userList = new ArrayList<String>();
        userList.add(userId);

        // Create a new MoodAdapter class
        ArrayList<Mood> moodsInit = new ArrayList<Mood>();

        firestoreHelper.firebaseToMoodHistory(userList, new FirestoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                // Expecting a moodHistory object to be returned.
                moodHistory history = (moodHistory) result;
                // moodsInit contains an Array of Moods.
                moodsInit.addAll(history.getMoodArray());
            }
            @Override
            public void onFailure(Exception e) {
                fail("Error Retrieving moods from user. " + e.getMessage());
            }
        });

        // Wait up to 5 seconds for the callback to complete.
        latch5seconds(latch);

        // Test 1: Size is 2
        assertEquals(2, moodsInit.size());

        // Test 2: All the Moods retrieved are from the same user.
        for (int i = 0; i < moodsInit.size(); i++) {
            Mood mood = moodsInit.get(i);
            assertEquals(userId, mood.getUserId());
        }
    }

    @Test
    public void QueryEmotionalState() {
        // Testing querying for a sad mood user the user. Testing all the fields as well.
        // Note: This test does fail, Need to refactor this query to fit our current implementation.

        // Initially seed the database with user values
        String userId;
        String newUsername = "Salim";
        String newPassword = "Salim123";
        userId = seedDatabase("UserId5", newUsername, newPassword);

        CountDownLatch latch = new CountDownLatch(1);
        ArrayList<Mood> moodsInit = new ArrayList<Mood>();

        Mood testMood2 = new Mood();
        testMood2.setMood("Happy");
        testMood2.setMoodDescription("I am quite very yes happy");
        testMood2.setSocialSituation("One-On-One");
        testMood2.setLatitude(10.53);
        testMood2.setLongitude(20.24);

        // Adds and starts counting down latch.
        firestoreHelper.addMood(userId, testMood2, getMoodCallback(latch));

        // Takes in a userId and Not a userId List.
        firestoreHelper.firebaseQueryEmotional(userId, "Happy", new FirestoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                // Expecting a moodHistory object to be returned.
                moodHistory history = (moodHistory) result;
                // moodsInit contains an Array of Moods.
                moodsInit.addAll(history.getMoodArray());
            }
            @Override
            public void onFailure(Exception e) {
                fail("Error Retrieving a Mood: " + e.getMessage());
            }
        });

        // Wait up to 5 seconds for the callback to complete.
        latch5seconds(latch);

        // Test to see if it is the happy mood with all the values.
        assertEquals(userId, moodsInit.get(0).getUserId());
        assertEquals("Happy", moodsInit.get(0).getMood());
        assertEquals("I am quite very yes happy", moodsInit.get(0).getMoodDescription());
        assertEquals("One-On-One", moodsInit.get(0).getSocialSituation());
        assertEquals(10.53, moodsInit.get(0).getLatitude(), 0.01);
        assertEquals(20.24, moodsInit.get(0).getLongitude(), 0.01);
    }

}
