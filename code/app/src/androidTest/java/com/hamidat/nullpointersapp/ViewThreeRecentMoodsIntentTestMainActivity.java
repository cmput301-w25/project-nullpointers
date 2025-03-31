package com.hamidat.nullpointersapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.hamidat.nullpointersapp.SearchForOtherUsersIntentTestMainActivity.withIndex;
import static com.hamidat.nullpointersapp.androidTestHelpers.ViewActionsHelper.hasItemCount;
import static org.hamcrest.Matchers.allOf;

import android.os.SystemClock;
import android.util.Log;

import androidx.test.espresso.contrib.RecyclerViewActions;

import com.hamidat.nullpointersapp.utils.testUtils.TestUsersHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ViewThreeRecentMoodsIntentTestMainActivity extends BaseMainActivityUITest {
    private static final String TEST_SEARCH_QUERY = "hamihami";

    @Before
    public void addFollowingBetween() {
        Log.d("ViewThreeRecentMoodsTest", "Adding following relationship for test setup...");
        TestUsersHelper.insertFollowingBetween(TEST_USER_ID, HAMIHAMI_USER_ID);
        SystemClock.sleep(2000); // Allow Firestore to process
    }

    @Test
    public void viewTheOtherUsersProfileForMoods() {
        // Click on search icon and ensure search edit text is displayed.
        onView(withId(R.id.ivSearch)).perform(click());
        onView(withId(R.id.etSearch)).check(matches(isDisplayed()));

        // Replace text in the search field.
        onView(withId(R.id.etSearch)).perform(replaceText(TEST_SEARCH_QUERY));
        SystemClock.sleep(2000);

        // Click on the first result in the recycler view.
        onView(withId(R.id.rvResults)).perform(
                RecyclerViewActions.actionOnItemAtPosition(0, click()));

        SystemClock.sleep(2000);

        // Verify username is displayed
        onView(allOf(withId(R.id.username_text), withText(TEST_SEARCH_QUERY)))
                .check(matches(isDisplayed()));

        // Wait for mood events to be fetched
        SystemClock.sleep(3000);

        // Assert that exactly 3 moods are loaded in rvMoodEvents
        onView(withIndex(withId(R.id.rvMoodEvents), 1)).check(matches(hasItemCount(3)));

        Log.d("ViewThreeRecentMoodsIntentTest", "3 mood events are visible");
    }

    @After
    public void removeFollowingBetween() {
        Log.d("ViewThreeRecentMoodsTest", "Cleaning up following relationship...");
        TestUsersHelper.removeFollowingBetween(TEST_USER_ID, HAMIHAMI_USER_ID);
        SystemClock.sleep(1500); // Allow Firestore to process
    }}
