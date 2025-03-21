package com.hamidat.nullpointersapp;

import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import androidx.test.rule.GrantPermissionRule;

@LargeTest
public class PostPrivateMoodIntentTest {
    /*
    TODO - Do we need to login as another user and check this? Or is being able to post a private mood enough
     */

    // default grant all permissions so the popups don't stop expresso from running
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            android.Manifest.permission.POST_NOTIFICATIONS,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    );

    // Launch MainActivity with a dummy user id so that AddMoodFragment can get a FirestoreHelper instance.
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class)
                    .putExtra("USER_ID", "EHxg6TEtQFWHaqbnkt5H")); // the user id for testUser

    @Test
    public void addPrivateMoodShouldAddValidMoodEntry() {
        // Click on the Add Mood icon to open AddMoodFragment
        onView(withId(R.id.ivAddMood)).perform(click());

        // Verify the Add Mood UI is displayed (checking the title)
        onView(withId(R.id.tvAddNewMoodEvent)).check(matches(isDisplayed()));

        // Enter a reason for the mood
        onView(withId(R.id.Reason))
                .perform(typeText("Feeling great!"), closeSoftKeyboard());
        // Turn on the toggle to make it a private mood
        onView(withId(R.id.switchPrivacy))
                .perform(click());

        // Select a mood (e.g., Happy)
        onView(withId(R.id.rbHappy)).perform(click());

        // Select a social situation (e.g., One-on-One)
        onView(withId(R.id.rbOneOnOne)).perform(click());

        // Toggle off location to avoid validation issues (default is attached)
        onView(withId(R.id.btnAttachLocation)).perform(click());

        // Click on the Save button
        onView(withId(R.id.btnSaveEntry)).perform(click());


        // after this the UI goes back to the homefeed fragment
        // need to click the edit button on the specifc mood card
        SystemClock.sleep(3000);

        // Wait briefly to ensure the home feed is updated (you can replace with IdlingResource later)
        onView(withId(R.id.rvMoodList)).check(matches(isDisplayed())); // This will block until the view appears
    }

    @After
    public void tearDown() {
        // Run after each test method:
        // Delete the test mood from both the global moods collection and the user's moodHistory subcollection.
        deleteTestMoodIfExists("EHxg6TEtQFWHaqbnkt5H", "Feeling great!");
    }

    private void deleteTestMoodIfExists(String userId, String moodDescription) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Query the global "moods" collection for the test mood using moodDescription and userId
        db.collection("moods")
                .whereEqualTo("moodDescription", moodDescription)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (var doc : queryDocumentSnapshots.getDocuments()) {
                        String moodId = doc.getId();
                        // Delete the mood document from the global "moods" collection
                        db.collection("moods").document(moodId).delete()
                                .addOnSuccessListener(aVoid -> Log.d("Teardown", "Deleted test mood from moods collection: " + moodId))
                                .addOnFailureListener(e -> Log.e("Teardown", "Failed to delete test mood from moods collection: " + e.getMessage()));

                        // Now delete the mood reference from the user's "moodHistory" subcollection
                        db.collection("users").document(userId)
                                .update("moodHistory", FieldValue.arrayRemove(moodId))
                                .addOnSuccessListener(aVoid -> Log.d("Teardown", "Deleted mood reference from user's moodHistory: " + moodId))
                                .addOnFailureListener(e -> Log.e("Teardown", "Failed to delete mood reference: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> Log.e("Teardown", "Failed to query test mood: " + e.getMessage()));
    }

}
