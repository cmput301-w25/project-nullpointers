package com.hamidat.nullpointersapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

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
public class FilterMoodsIntentTest extends BaseMainActivityUITest {
    private static final String TAG = "FilterMoodIntentTest";

    // Moods for testing
    private static final String MOOD_HAPPY_REASON = "Chillin with coffee";
    private static final String MOOD_SAD_REASON = "Bad weather day";

    @Before
    public void setup() {
        // Insert test moods
        TestMoodHelper.insertTestMood(TEST_USER_ID, "Sad", MOOD_SAD_REASON, 0, 0, "Alone", false);
        TestMoodHelper.insertTestMood(TEST_USER_ID, "Happy", MOOD_HAPPY_REASON, 0, 0, "Group", false);

        SystemClock.sleep(5000);
    }

    @Test
    public void testFilterByEmotionHappyOnly() {
        Log.d(TAG, "Testing emotion filter");

        onView(withId(R.id.tvFollowing)).perform(click());
        SystemClock.sleep(1000);

        onView(withId(R.id.checkboxHappy)).perform(click());
        onView(withId(R.id.buttonApplyFilter)).perform(click());
        SystemClock.sleep(3000);

        onView(withText(MOOD_HAPPY_REASON)).check(matches(isDisplayed()));
        onView(withText(MOOD_SAD_REASON)).check(doesNotExist());
    }

    @Test
    public void testFilterByDescriptionKeyword() {
        Log.d(TAG, "Testing description keyword filter");

        onView(withId(R.id.tvFollowing)).perform(click());
        SystemClock.sleep(1000);

        onView(withId(R.id.reasonDescription)).perform(replaceText("coffee"), closeSoftKeyboard());
        onView(withId(R.id.buttonApplyFilter)).perform(click());
        SystemClock.sleep(3000);

        onView(withText(MOOD_HAPPY_REASON)).check(matches(isDisplayed()));
        onView(withText(MOOD_SAD_REASON)).check(doesNotExist());
    }

    @After
    public void tearDown() {
        TestMoodHelper.deleteMoodByDescription(TEST_USER_ID, MOOD_HAPPY_REASON);
        TestMoodHelper.deleteMoodByDescription(TEST_USER_ID, MOOD_SAD_REASON);
    }
}
