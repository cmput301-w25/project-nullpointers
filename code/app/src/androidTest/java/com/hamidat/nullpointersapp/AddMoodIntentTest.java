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

import com.hamidat.nullpointersapp.utils.testUtils.TestMoodHelper;

@LargeTest
public class AddMoodIntentTest extends BaseUITest {
    private static final String TEST_REASON = "Feeling great!";

    @Test
    public void addMoodShouldAddValidMoodEntry() {
        // Click on the Add Mood icon to open AddMoodFragment
        onView(withId(R.id.ivAddMood)).perform(click());

        // Verify the Add Mood UI is displayed (checking the title)
        onView(withId(R.id.tvAddNewMoodEvent)).check(matches(isDisplayed()));

        onView(withId(R.id.Reason))
                .perform(typeText(TEST_REASON), closeSoftKeyboard());

        // Select a mood ( Happy)
        onView(withId(R.id.rbHappy)).perform(click());

        // Select a social situation
        onView(withId(R.id.rbGroup)).perform(click());

        // Toggle off location to avoid validation issues (default is attached)
        onView(withId(R.id.btnAttachLocation)).perform(click());

        // Click on the Save button
        onView(withId(R.id.btnSaveEntry)).perform(click());

        // after this the UI goes back to the homefeed fragment
        // need to click the edit button on the specifc mood card
        SystemClock.sleep(3000);

        // Wait briefly to ensure the home feed is updated
        onView(withId(R.id.rvMoodList)).check(matches(isDisplayed()));

        // Verify the mood card contents are shown
        onView(withText(TEST_REASON)).check(matches(isDisplayed()));
        onView(withText("Happy")).check(matches(isDisplayed()));
        onView(withText("Group")).check(matches(isDisplayed()));
    }


    @After
    public void tearDown() {
        TestMoodHelper.deleteMoodByDescription(
                TEST_USER_ID,
                TEST_REASON
        );
    }
}
