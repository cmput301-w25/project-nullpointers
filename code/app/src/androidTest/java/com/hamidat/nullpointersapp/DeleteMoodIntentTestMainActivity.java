package com.hamidat.nullpointersapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.os.SystemClock;
import android.util.Log;

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
public class DeleteMoodIntentTestMainActivity extends BaseMainActivityUITest {
    private static final String TAG = "DeleteMoodIntentTest";

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
        // Small delay to let the navigation complete
        onView(withId(R.id.rvMoodList)).check(matches(isDisplayed()));
        SystemClock.sleep(2000);

        // Click the "View More" button on the newest mood
        onView(withId(R.id.rvMoodList))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0,
                        ViewActionsHelper.clickChildViewWithId(R.id.btnViewMore)));
        SystemClock.sleep(1000);

        // Click the "Delete" button in the dialog
        onView(withId(R.id.btnDialogDelete)).perform(click());
        Log.d(TAG, "Clicked delete on the mood.");

        // Wait for home feed to refresh
        SystemClock.sleep(2000);
        onView(withId(R.id.rvMoodList)).check(matches(isDisplayed()));

        Log.d(TAG, "Clicked the delete button of the mood we just added.");

        // Wait for HomeFeedFragment to load
        SystemClock.sleep(2000);
        // Ensure the home feed is visible again
        onView(withId(R.id.rvMoodList)).check(matches(isDisplayed()));

    }

    @After
    public void tearDown() {
        TestMoodHelper.deleteMoodByDescription(
                TEST_USER_ID,
                "I'm feeling pretty good!"
        );

        TestMoodHelper.deleteMoodByDescription(
                TEST_USER_ID,
                "Hey hey I'm just a fun guy"
        );
    }
}
