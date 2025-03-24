package com.hamidat.nullpointersapp;

import android.os.SystemClock;
import android.util.Log;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
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
public class AddMoodSocialSituationIntentTest extends BaseUITest {

    private static final String TEST_REASON = "Testing social situation label.";
    private static final String EXPECTED_SOCIAL_SITUATION_LABEL = "Group";

    @Test
    public void addMoodWithSocialSituation_shouldDisplayItInFeed() {
        // Launch AddMoodFragment
        onView(withId(R.id.ivAddMood)).perform(click());
        onView(withId(R.id.tvAddNewMoodEvent)).check(matches(isDisplayed()));

        // Enter mood reason
        onView(withId(R.id.Reason))
                .perform(typeText(TEST_REASON), closeSoftKeyboard());
        onView(withId(R.id.rbSad)).perform(click());

        // Select Group as the social situation
        onView(withId(R.id.rbGroup)).perform(click());

        onView(withId(R.id.btnAttachLocation)).perform(click());
        onView(withId(R.id.btnSaveEntry)).perform(click()); // save the entry

        // Wait for UI to return to HomeFeedFragment
        SystemClock.sleep(2000);
        onView(withId(R.id.rvMoodList)).check(matches(isDisplayed()));

        // verify the social situation text appears
        onView(withText(EXPECTED_SOCIAL_SITUATION_LABEL)).check(matches(isDisplayed()));

        Log.d("AddMoodSocialSituationIntentTest", "Verified social situation is correctly saved and restored.");
    }

    @After
    public void tearDown() {
        TestMoodHelper.deleteMoodByDescription(TEST_USER_ID, TEST_REASON);
    }
}
