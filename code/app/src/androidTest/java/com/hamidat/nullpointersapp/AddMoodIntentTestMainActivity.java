package com.hamidat.nullpointersapp;

import android.os.SystemClock;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.hamidat.nullpointersapp.SearchForOtherUsersIntentTestMainActivity.withIndex;

import com.hamidat.nullpointersapp.utils.testUtils.TestMoodHelper;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AddMoodIntentTestMainActivity extends BaseMainActivityUITest {
    private static final String TEST_REASON = "Feeling great!";
    private static final String TAG = "AddMoodIntentTest";

    @Test
    public void addMoodShouldAddValidMoodEntry() {
        Log.d(TAG, "Starting test: addMoodShouldAddValidMoodEntry");
        // Click on the Add Mood icon to open AddMoodFragment
        onView(withId(R.id.ivAddMood)).perform(click());
        SystemClock.sleep(5000);

        // Verify the Add Mood UI is displayed (checking the title)
        onView(withId(R.id.tvAddNewMoodEvent)).check(matches(isDisplayed()));

        // Type the reason
        onView(withId(R.id.Reason))
                .perform(typeText(TEST_REASON), closeSoftKeyboard());

        // Select a mood from the dropdown (Spinner)
        onView(withId(R.id.spinnerMood)).perform(click());
        onView(withText("Happy")).perform(click());

        // Select a social situation
        onView(withId(R.id.rbGroup)).perform(click());

        // Toggle off location to avoid validation issues
        onView(withId(R.id.btnAttachLocation)).perform(click());

        // Click on Save button
        Log.d(TAG, "Saving the mood entry");
        onView(withId(R.id.btnSaveEntry)).perform(click());

        // after this the UI goes back to the homefeed fragment
        // need to click the edit button on the specifc mood card
        SystemClock.sleep(3000);

        // Ensure RecyclerView is visible
        onView(withId(R.id.rvMoodList)).check(matches(isDisplayed()));
        SystemClock.sleep(3000);

        // Click on the first View More button (to open full mood details)
        Log.d(TAG, "Opening 'View More' dialog for the most recent mood");
        onView(withIndex(withId(R.id.btnViewMore), 0)).perform(click());

        // Validate contents in dialog
        Log.d(TAG, "Verifying mood details in the dialog...");
        onView(withId(R.id.tvDialogDescription)).check(matches(withText("Why: " + TEST_REASON)));
        onView(withId(R.id.tvDialogMood)).check(matches(withText("😊  🟡 - Happy")));
        onView(withId(R.id.tvDialogSocial)).check(matches(withText("Situation: Group")));

        Log.d(TAG, "Mood details verified successfully.");

    }



    @After
    public void tearDown() {
        TestMoodHelper.deleteMoodByDescription(
                TEST_USER_ID,
                TEST_REASON
        );
    }
}
