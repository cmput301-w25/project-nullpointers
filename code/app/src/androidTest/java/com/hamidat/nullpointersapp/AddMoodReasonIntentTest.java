package com.hamidat.nullpointersapp;

import android.os.SystemClock;

import androidx.test.filters.LargeTest;

import com.hamidat.nullpointersapp.utils.testUtils.TestMoodHelper;

import org.junit.After;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@LargeTest
public class AddMoodReasonIntentTest extends BaseUITest {

    private static final String TEST_REASON = "Just needed to write this down.";

    @Test
    public void addMoodWithReasonOnly_shouldSucceed() {
        // Open AddMoodFragment
        onView(withId(R.id.ivAddMood)).perform(click());
        onView(withId(R.id.tvAddNewMoodEvent)).check(matches(isDisplayed()));

        // Enter the reason
        onView(withId(R.id.Reason))
                .perform(typeText(TEST_REASON), closeSoftKeyboard());

        // fill out the rest
        onView(withId(R.id.rbHappy)).perform(click());
        onView(withId(R.id.rbOneOnOne)).perform(click());

        onView(withId(R.id.btnAttachLocation)).perform(click());

        onView(withId(R.id.btnSaveEntry)).perform(click());

        // Wait for HomeFeedFragment to show again
        SystemClock.sleep(3000);
        onView(withId(R.id.rvMoodList)).check(matches(isDisplayed()));

        // verify the reason text appears
        onView(withText(TEST_REASON)).check(matches(isDisplayed()));

    }

    @After
    public void tearDown() {
        TestMoodHelper.deleteMoodByDescription(TEST_USER_ID, TEST_REASON);
    }
}
