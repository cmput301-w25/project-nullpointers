package com.hamidat.nullpointersapp;

import android.os.SystemClock;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.hamidat.nullpointersapp.utils.testUtils.TestMoodHelper;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.hamidat.nullpointersapp.SearchForOtherUsersIntentTestMainActivity.withIndex;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AddMoodSocialSituationIntentTestMainActivity extends BaseMainActivityUITest {

    private static final String TEST_REASON = "Testing social situation label.";
    private static final String EXPECTED_SOCIAL_SITUATION_LABEL = "Group";
    private static final String TAG = "AddMoodSocialSituationIntentTest";

    @Test
    public void addMoodWithSocialSituation_shouldDisplayItInFeed() {
        // Launch AddMoodFragment
        onView(withId(R.id.ivAddMood)).perform(click());
        onView(withId(R.id.tvAddNewMoodEvent)).check(matches(isDisplayed()));

        // Enter mood reason
        onView(withId(R.id.Reason))
                .perform(typeText(TEST_REASON), closeSoftKeyboard());
        // Select a mood from the dropdown (Spinner)
        onView(withId(R.id.spinnerMood)).perform(click());
        onView(withText("Sad")).perform(click());


        // Select Group as the social situation
        onView(withId(R.id.rbGroup)).perform(click());

        onView(withId(R.id.btnAttachLocation)).perform(click());
        onView(withId(R.id.btnSaveEntry)).perform(click()); // save the entry

        // Wait for UI to return to HomeFeedFragment
        SystemClock.sleep(2000);
        onView(withId(R.id.rvMoodList)).check(matches(isDisplayed()));

        // Click on the first View More button (to open full mood details)
        Log.d(TAG, "Opening 'View More' dialog for the most recent mood");
        onView(withIndex(withId(R.id.btnViewMore), 0)).perform(click());

        Log.d(TAG, "Verifying mood social situation details in the dialog...");
        onView(withId(R.id.tvDialogSocial)).check(matches(withText("Situation: " + EXPECTED_SOCIAL_SITUATION_LABEL)));

        Log.d("AddMoodSocialSituationIntentTest", "Verified social situation is correctly saved and restored.");
    }

    @After
    public void tearDown() {
        TestMoodHelper.deleteMoodByDescription(TEST_USER_ID, TEST_REASON);
    }
}
