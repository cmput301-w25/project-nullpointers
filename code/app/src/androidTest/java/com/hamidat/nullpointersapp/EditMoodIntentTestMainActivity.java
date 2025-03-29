package com.hamidat.nullpointersapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
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
public class EditMoodIntentTestMainActivity extends BaseMainActivityUITest {

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
    public void editMoodShouldUpdateMoodEntry() {
        // Wait for HomeFeed to populate
        onView(withId(R.id.rvMoodList)).check(matches(isDisplayed()));
        SystemClock.sleep(3000);

        // Click "View More" on the first mood card
        onView(withId(R.id.rvMoodList))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0,
                        ViewActionsHelper.clickChildViewWithId(R.id.btnViewMore)));
        SystemClock.sleep(1000);

        // Click "Edit" in the dialog
        onView(withId(R.id.btnDialogEdit)).perform(click());

        // Update mood fields
        onView(withId(R.id.Reason))
                .perform(replaceText("Actually, I'm feeling awesome today!"), closeSoftKeyboard());

        onView(withId(R.id.spinnerMood)).perform(click());
        onView(withText("Happy")).perform(click());

        onView(withId(R.id.rbGroup)).perform(click());
        onView(withId(R.id.btnAttachLocation)).perform(click());

        // Save the edited mood
        onView(withId(R.id.btnSaveEntry)).perform(click());

        // Wait for HomeFeed to reload
        SystemClock.sleep(3000);

        // Click "View More" again on the updated mood
        onView(withId(R.id.rvMoodList))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0,
                        ViewActionsHelper.clickChildViewWithId(R.id.btnViewMore)));
        SystemClock.sleep(1000);

        // Assert changes are reflected in the dialog
        onView(withId(R.id.tvDialogDescription))
                .check(matches(withText("Why: Actually, I'm feeling awesome today!")));
        onView(withId(R.id.tvDialogMood)).check(matches(withText("😊  🟡")));
        onView(withId(R.id.tvDialogSocial)).check(matches(withText("Situation: Group")));

        Log.d("EditMoodTest", "Verified that edited mood values appear in the detail dialog.");
    }

    @After
    public void tearDown() {
        TestMoodHelper.deleteMoodByDescription(
                TEST_USER_ID,
                "Actually, I'm feeling awesome today!"
        );
    }
}
