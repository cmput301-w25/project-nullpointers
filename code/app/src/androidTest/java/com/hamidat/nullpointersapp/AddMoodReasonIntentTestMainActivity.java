package com.hamidat.nullpointersapp;

import android.os.SystemClock;
import android.util.Log;

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
import static com.hamidat.nullpointersapp.SearchForOtherUsersIntentTestMainActivity.withIndex;

@LargeTest
public class AddMoodReasonIntentTestMainActivity extends BaseMainActivityUITest {

    private static final String TEST_REASON = "Just needed to write this down.";
    private static final String TAG = "AddMoodReasonIntentTest";

    @Test
    public void addMoodWithReasonOnly_shouldSucceed() {
        // Open AddMoodFragment
        onView(withId(R.id.ivAddMood)).perform(click());
        onView(withId(R.id.tvAddNewMoodEvent)).check(matches(isDisplayed()));

        // Enter the reason
        onView(withId(R.id.Reason))
                .perform(typeText(TEST_REASON), closeSoftKeyboard());

        // fill out the rest
        // Select a mood from the dropdown (Spinner)
        onView(withId(R.id.spinnerMood)).perform(click());
        onView(withText("Happy")).perform(click());
        onView(withId(R.id.rbOneOnOne)).perform(click());

        onView(withId(R.id.btnAttachLocation)).perform(click());

        onView(withId(R.id.btnSaveEntry)).perform(click());

        // Wait for HomeFeedFragment to show again
        SystemClock.sleep(3000);
        onView(withId(R.id.rvMoodList)).check(matches(isDisplayed()));

        // verify the reason text appears
        // Click on the first View More button (to open full mood details)
        Log.d(TAG, "Opening 'View More' dialog for the most recent mood");
        onView(withIndex(withId(R.id.btnViewMore), 0)).perform(click());

        // Validate contents in dialog
        Log.d(TAG, "Verifying mood reason in the dialog...");
        onView(withId(R.id.tvDialogDescription)).check(matches(withText("Why: " + TEST_REASON)));

    }

    @After
    public void tearDown() {
        TestMoodHelper.deleteMoodByDescription(TEST_USER_ID, TEST_REASON);
    }
}
