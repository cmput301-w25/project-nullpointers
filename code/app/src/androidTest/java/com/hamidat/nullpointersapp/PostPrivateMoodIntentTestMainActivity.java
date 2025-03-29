package com.hamidat.nullpointersapp;

import android.os.SystemClock;

import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import com.hamidat.nullpointersapp.utils.testUtils.TestMoodHelper;

@LargeTest
public class PostPrivateMoodIntentTestMainActivity extends BaseMainActivityUITest {
    /*
    TODO - Do we need to login as another user and check this? Or is being able to post a private mood enough
     */
    @Test
    public void addPrivateMoodShouldAddValidMoodEntry() {
        // Click on the Add Mood icon to open AddMoodFragment
        onView(withId(R.id.ivAddMood)).perform(click());

        onView(withId(R.id.tvAddNewMoodEvent)).check(matches(isDisplayed()));
        onView(withId(R.id.Reason))
                .perform(typeText("Feeling great!"), closeSoftKeyboard());
        // Turn on the toggle to make it a private mood
        onView(withId(R.id.switchPrivacy))
                .perform(click());

        onView(withId(R.id.spinnerMood)).perform(click());
        onView(withText("Happy")).perform(click());

        onView(withId(R.id.rbOneOnOne)).perform(click());
        onView(withId(R.id.btnAttachLocation)).perform(click());
        onView(withId(R.id.btnSaveEntry)).perform(click());


        // after this the UI goes back to the homefeed fragment
        // need to click the edit button on the specifc mood card
        SystemClock.sleep(3000);

        // Wait briefly to ensure the home feed is updated
        onView(withId(R.id.rvMoodList)).check(matches(isDisplayed())); // This will block until the view appears
    }

    @After
    public void tearDown() {
        TestMoodHelper.deleteMoodByDescription(
                TEST_USER_ID,
                "Feeling great!"
        );
    }
}
