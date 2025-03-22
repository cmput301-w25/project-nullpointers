package com.hamidat.nullpointersapp;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.os.SystemClock;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.hamidat.nullpointersapp.utils.testUtils.TestMoodHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import androidx.test.espresso.contrib.RecyclerViewActions;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ViewAllCommentsOnMoodIntentTest extends BaseUITest{
    @Before
    public void setUpMoodWithComment() {
        String moodDescription = "This mood needs a comment";

        TestMoodHelper.insertTestMood(
                TEST_USER_ID,
                "Happy",
                moodDescription,
                51.5074, -0.1278,
                "Group",
                false
        );

        TestMoodHelper.getMoodIdByDescription(
                TEST_USER_ID,
                moodDescription,
                new TestMoodHelper.MoodIdCallback() {
            @Override
            public void onMoodIdFound(String moodId) {
                TestMoodHelper.insertComment(
                        moodId,
                        TEST_USER_2_ID,
                        "Salim",
                        "This is a test comment from Salim");

                TestMoodHelper.insertComment(
                        moodId,
                        TEST_USER_3_ID,
                        "Moe",
                        "This is a test comment from Ogua");

                TestMoodHelper.insertComment(
                        moodId,
                        TEST_USER_3_ID,
                        "Moe",
                        "This is a test comment from Hamidat");
            }

            @Override
            public void onError(Exception e) {
                Log.e("TestSetup", "Could not get moodId for comment", e);
            }
        });

        SystemClock.sleep(2000);
    }

    @Test
    public void viewAllCommentsOnMood() {
        onView(withId(R.id.rvMoodList)).check(matches(isDisplayed()));

        // Click the comment button on the most recent mood
        onView(withId(R.id.rvMoodList))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0,
                        ViewActionsHelper.clickChildViewWithId(R.id.btnComment)));

        // Wait for the BottomSheet to load
        SystemClock.sleep(2000);

        // Assert all inserted comments are visible
        onView(withText("This is a test comment from Salim")).check(matches(isDisplayed()));
        onView(withText("This is a test comment from Ogua")).check(matches(isDisplayed()));
        onView(withText("This is a test comment from Hamidat")).check(matches(isDisplayed()));

        Log.d("ViewAllCommentsTest", "Successfully verified all test comments are displayed.");
    }


    @After
    public void tearDown() {
        TestMoodHelper.deleteMoodByDescription(
                TEST_USER_ID,
                "This mood needs a comment"
        );
    }
}
