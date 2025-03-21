package com.hamidat.nullpointersapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import androidx.test.espresso.contrib.RecyclerViewActions;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DeleteMoodIntentTest {

    // Automatically grant runtime permissions so that system dialogs don't interrupt Espresso.
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            android.Manifest.permission.POST_NOTIFICATIONS,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    );

    // launch mainActivity with a dummy user ID
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class)
                    .setAction(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_LAUNCHER)
                    .putExtra("USER_ID", "EHxg6TEtQFWHaqbnkt5H"));


    @Test
    public void deleteMoodShouldRemoveMood() {
        // Add 2 moods mood first

        // Click on the Add Mood icon to open AddMoodFragment
        onView(withId(R.id.ivAddMood)).perform(click());
        // Verify the Add Mood UI is displayed (checking the title)
        onView(withId(R.id.tvAddNewMoodEvent)).check(matches(isDisplayed()));
        // Fill out the params for
        onView(withId(R.id.Reason))
                .perform(typeText("I'm feeling pretty good!"), closeSoftKeyboard());
        // Select sad as the original mood
        onView(withId(R.id.rbSad)).perform(click());
        // Select a social situation (alone)
        onView(withId(R.id.rbAlone)).perform(click());
        onView(withId(R.id.btnAttachLocation)).perform(click());
        // Click on the Save button
        onView(withId(R.id.btnSaveEntry)).perform(click());
        Log.d("DeleteMoodIntentTest", "Added the first mood: Clicked save, waiting for mood list...");

        // Please do not remove the the sleeps here, they are imperative
        // after this the UI goes back to the homefeed fragment
        SystemClock.sleep(2000);
        onView(withId(R.id.rvMoodList)).check(matches(isDisplayed())); // This will block until the view appears
        // Small delay to let the navigation complete
        SystemClock.sleep(1000);

        // Click on the Add Mood icon to open AddMoodFragment
        onView(withId(R.id.ivAddMood)).perform(click());
        // Verify the Add Mood UI is displayed (checking the title)
        onView(withId(R.id.tvAddNewMoodEvent)).check(matches(isDisplayed()));
        // Fill out the params for
        onView(withId(R.id.Reason))
                .perform(typeText("Hey hey I'm just a fun guy"), closeSoftKeyboard());
        // Select sad as the original mood
        onView(withId(R.id.rbHappy)).perform(click());
        // Select a social situation (alone)
        onView(withId(R.id.rbGroup)).perform(click());
        onView(withId(R.id.btnAttachLocation)).perform(click());
        // Click on the Save button
        onView(withId(R.id.btnSaveEntry)).perform(click());
        Log.d("DeleteMoodIntentTest", "Added the second mood: Clicked save, waiting for mood list...");

        // Please do not remove the the sleeps here, they are imperative
        // after this the UI goes back to the homefeed fragment
        SystemClock.sleep(2000);
        onView(withId(R.id.rvMoodList)).check(matches(isDisplayed())); // This will block until the view appears
        // Small delay to let the navigation complete
        SystemClock.sleep(2000);

        // Click the delete button on the first item in the RecyclerView (newest mood)
        Log.d("DeleteMoodIntentTest", "The rvMoodList is now displayed");
        SystemClock.sleep(4000);

        onView(withId(R.id.rvMoodList))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0,
                        ViewActionsHelper.clickChildViewWithId(R.id.btnDelete)));
        Log.d("DeleteMoodIntentTest", "Clicked the delete button of the mood we just added.");

        // Now, actually delete the mood
        SystemClock.sleep(3000);

        // Wait for HomeFeedFragment to load
        SystemClock.sleep(2000);
        // Ensure the home feed is visible again
        onView(withId(R.id.rvMoodList)).check(matches(isDisplayed()));

        // Click the most recent mood entry to view details (the one that was just edited)
        // Verify that the most recent mood is now the one that wasn't deleted changes persisted,so the edits rem
        onView(withId(R.id.rvMoodList))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0,
                        ViewActionsHelper.clickChildViewWithId(R.id.btnEdit)));

        // Small wait to let EditMoodFragment load
        SystemClock.sleep(1000);

        onView(withId(R.id.Reason))
                .check(matches(withText("I'm feeling pretty good!")));
        onView(withId(R.id.rbSad)).check(matches(ViewMatchers.isChecked()));
        onView(withId(R.id.rbAlone)).check(matches(ViewMatchers.isChecked()));

        Log.d("DeleteMoodIntentTest", "Verified that the mood that we pressed delete for is gone.");
    }

    @After
    public void tearDown() {
        // Run after each test method:
        // Delete the test mood from both the global moods collection and the user's moodHistory subcollection.
        deleteTestMoodIfExists("EHxg6TEtQFWHaqbnkt5H", "I'm feeling pretty good!");

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
