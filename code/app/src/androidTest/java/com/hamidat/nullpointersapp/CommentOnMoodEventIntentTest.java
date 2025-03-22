package com.hamidat.nullpointersapp;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onView;
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

import com.hamidat.nullpointersapp.utils.testUtils.TestMoodHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import androidx.test.espresso.contrib.RecyclerViewActions;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class CommentOnMoodEventIntentTest extends BaseUITest{
    @Before
    public void setUpMood() {
        TestMoodHelper.insertTestMood(
                TEST_USER_ID,
                "Sad",
                "I'm feeling pretty good!",
                51.5074, -0.1278,
                "Alone",
                false
        );
    }

    @Test
    public void commentOnMood() {
        onView(withId(R.id.rvMoodList)).check(matches(isDisplayed()));
        SystemClock.sleep(2000);

        // Click the comment button on the most recent mood
        onView(withId(R.id.rvMoodList))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0,
                        ViewActionsHelper.clickChildViewWithId(R.id.btnComment)));

        SystemClock.sleep(2000); // Let the comment dialog open

        // Type a comment in the EditText and click "Post"
        onView(withId(R.id.etComment)).perform(typeText("This is a test comment"));
        closeSoftKeyboard();
        onView(withId(R.id.btnPostComment)).perform(click());

        SystemClock.sleep(2000); // Give time for Firestore + UI update

        // Check that the comment appears in the RecyclerView inside the BottomSheet where the comments are posted
        onView(withText("This is a test comment")).check(matches(isDisplayed()));

        Log.d("CommentOnMoodTest", "Successfully posted and verified comment on mood.");
    }


    @After
    public void tearDown() {
        TestMoodHelper.deleteMoodByDescription(
                TEST_USER_ID,
                "I'm feeling pretty good!"
        );
    }
}
