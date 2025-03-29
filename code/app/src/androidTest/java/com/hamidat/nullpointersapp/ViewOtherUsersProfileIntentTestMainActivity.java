package com.hamidat.nullpointersapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import android.os.SystemClock;
import android.util.Log;

import androidx.test.espresso.contrib.RecyclerViewActions;

import org.junit.Test;

public class ViewOtherUsersProfileIntentTestMainActivity extends BaseMainActivityUITest {
    private static final String TEST_SEARCH_QUERY = "hamihami";

    @Test
    public void viewTheOtherUsersProfile() {
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

        Log.d("ViewOtherUsersProfileTest", "The other users profile is visible");
    }
}
