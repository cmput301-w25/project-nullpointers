package com.hamidat.nullpointersapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.hamidat.nullpointersapp.SearchForOtherUsersIntentTestMainActivity.withIndex;

import android.os.SystemClock;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.hamidat.nullpointersapp.utils.testUtils.TestMoodHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ViewMoodEventDetailsIntentTest extends BaseMainActivityUITest {

    private static final String TEST_REASON = "Feeling like espresso rocks!";
    private static final String TAG = "ViewMoodDetailsTest";

    @Before
    public void setup() {
        Log.d(TAG, "Inserting test mood...");
        TestMoodHelper.insertTestMood(TEST_USER_ID, "Happy", TEST_REASON, 0.0, 0.0, "Group", false);
        SystemClock.sleep(2000); // Allow Firestore time to sync
    }

    @Test
    public void viewMoodEventDetails_shouldDisplayCorrectInfo() {
        Log.d(TAG, "Waiting for HomeFeed to load...");
        onView(withId(R.id.rvMoodList)).check(matches(isDisplayed()));
        SystemClock.sleep(2000);

        Log.d(TAG, "Clicking the 'View More' button on first mood card...");
        onView(withIndex(withId(R.id.btnViewMore), 0)).perform(click());
        SystemClock.sleep(1000);

        Log.d(TAG, "Asserting mood dialog content...");

        // Validate description
        onView(withId(R.id.tvDialogDescription))
                .check(matches(withText("Why: " + TEST_REASON)));

        // Validate mood emoji and color
        onView(withId(R.id.tvDialogMood))
                .check(matches(withText("😊  🟡 - Happy")));

        // Validate social situation
        onView(withId(R.id.tvDialogSocial))
                .check(matches(withText("Situation: Group")));

        Log.d(TAG, "Mood details verified.");
    }

    @After
    public void tearDown() {
        Log.d(TAG, "Cleaning up inserted mood...");
        TestMoodHelper.deleteMoodByDescription(TEST_USER_ID, TEST_REASON);
    }
}
