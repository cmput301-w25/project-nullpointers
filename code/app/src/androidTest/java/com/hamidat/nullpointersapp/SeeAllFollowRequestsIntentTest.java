package com.hamidat.nullpointersapp;

import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.filters.LargeTest;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static com.hamidat.nullpointersapp.ViewActionsHelper.waitForView;

import com.hamidat.nullpointersapp.utils.testUtils.TestUsersHelper;
public class SeeAllFollowRequestsIntentTest extends BaseUITest {
    private static final String TAG = "SeeAllFollowRequestsTest";

    @Before
    public void setupFakeFollowRequests() {
        Log.d(TAG, "Setting up fake follow requests...");
        TestUsersHelper.insertFakeFollowRequests(TEST_USER_ID, TEST_USER_2_ID);
    }

    @Test
    public void testSeeAllFollowRequestsUI() {
        // Tap the notification icon
        // wait for the system notification to go away
        SystemClock.sleep(10000);
        onView(withId(R.id.ivNotification)).perform(click());
        onView(withId(R.id.ivNotification)).check(matches(isDisplayed()));

        // RecyclerView should be visible
        onView(withId(R.id.rvNotifications)).check(matches(isDisplayed()));

        SystemClock.sleep(4000); // Wait for Firestore writes to complete (its seriously a race condition)

        // Scroll to both requests to ensure they are loaded
        // Assert that the friend request from test_follower_2 is displayed
                onView(withText("testUser2 has sent you a friend request"))
                        .check(matches(isDisplayed()));
    }

    @After
    public void cleanupFakeFollowRequests() {
        Log.d(TAG, "Cleaning up fake follow requests...");
        TestUsersHelper.deleteFakeFollowRequest(TEST_USER_2_ID, TEST_USER_ID);
        SystemClock.sleep(1500);
    }
}

