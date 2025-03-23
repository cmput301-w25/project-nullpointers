package com.hamidat.nullpointersapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.os.SystemClock;
import android.util.Log;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.hamidat.nullpointersapp.androidTestHelpers.ViewActionsHelper;
import com.hamidat.nullpointersapp.utils.testUtils.TestMoodHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import androidx.test.espresso.contrib.RecyclerViewActions;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DeleteMoodIntentTest extends BaseUITest {
    @Before
    public void setUpMoods() {
        TestMoodHelper.insertTestMood(
                TEST_USER_ID,
                "Sad",
                "I'm feeling pretty good!",
                51.5074, -0.1278,
                "Alone",
                false
        );

        TestMoodHelper.insertTestMood(
                TEST_USER_ID,
                "Happy",
                "Hey hey I'm just a fun guy",
                51.5074, -0.1278,
                "Group",
                false
        );
    }

    @Test
    public void deleteMoodShouldRemoveMood() {
        onView(withId(R.id.rvMoodList)).check(matches(isDisplayed())); // This will block until the view appears
        // Small delay to let the navigation complete

        // Click the delete button on the first item in the RecyclerView (newest mood)
        Log.d("DeleteMoodIntentTest", "The rvMoodList is now displayed");
        SystemClock.sleep(2000);

        onView(withId(R.id.rvMoodList))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0,
                        ViewActionsHelper.clickChildViewWithId(R.id.btnDelete)));
        Log.d("DeleteMoodIntentTest", "Clicked the delete button of the mood we just added.");

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
        TestMoodHelper.deleteMoodByDescription(
                TEST_USER_ID,
                "I'm feeling pretty good!"
        );
    }
}
