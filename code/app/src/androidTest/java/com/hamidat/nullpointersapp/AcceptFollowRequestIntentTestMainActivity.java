package com.hamidat.nullpointersapp;

import android.os.SystemClock;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


import com.hamidat.nullpointersapp.utils.testUtils.TestUsersHelper;
public class AcceptFollowRequestIntentTestMainActivity extends BaseMainActivityUITest {
    private static final String TAG = "SeeAllFollowRequestsTest";

    @Before
    public void setupFakeFollowRequests() {
        Log.d(TAG, "Setting up fake follow requests...");
        TestUsersHelper.insertFakeFollowRequests(TEST_USER_ID, TEST_USER_2_ID);
    }

    @Test
    public void acceptAFollowRequest() {
        // Tap the notification icon
        // wait for the system notification to go away
        SystemClock.sleep(15000);
        onView(withId(R.id.ivNotification)).perform(click());
        onView(withId(R.id.ivNotification)).check(matches(isDisplayed()));

        // RecyclerView should be visible
        onView(withId(R.id.rvNotifications)).check(matches(isDisplayed()));

        SystemClock.sleep(4000); // Wait for Firestore writes to complete (its seriously a race condition)

        // Assert that the friend request from testUser2 is displayed
        onView(withText("testUser2 has sent you a friend request"))
                .check(matches(isDisplayed()));

        // press the accept button on the first item in the recvcler view
        onView(withId(R.id.btnAccept)).check(matches(isDisplayed()));
        onView(withId(R.id.btnAccept)).perform((click()));

        SystemClock.sleep(3000); // Wait for Firestore writes to complete (its seriously a race condition)
        // Assert that the notification is no longer displayed
        onView(withText("testUser2 has sent you a friend request"))
                .check(doesNotExist());
    }

    @After
    public void removeFollowingRelationship() {
        Log.d(TAG, "Cleaning up fake follow requests...");
        TestUsersHelper.deleteFakeFollowRequest(TEST_USER_2_ID, TEST_USER_ID);
        TestUsersHelper.removeFollowingBetween(TEST_USER_2_ID, TEST_USER_ID);
        SystemClock.sleep(1500);
    }
}

