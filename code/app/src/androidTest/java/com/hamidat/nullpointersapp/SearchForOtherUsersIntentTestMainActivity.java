package com.hamidat.nullpointersapp;

import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SearchForOtherUsersIntentTestMainActivity extends BaseMainActivityUITest {

    private static final String TEST_SEARCH_QUERY = "hamidatb";

    @Test
    public void searchUserAndViewProfileTest() {
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

        // Check that the profile overlay displays the correct username.
        onView(allOf(
                withText(TEST_SEARCH_QUERY),
                withId(R.id.username_text)
        )).check(matches(isDisplayed()));
        Log.d("SearchForOtherUsersIntentTest", "TODO");

        // Check that the follow/unfollow button is visible.
        onView(withIndex(withId(R.id.btnFollowUnfollow), 1))
                .check(matches(isDisplayed()));
        // Return to the search screen by clicking the back button.
        onView(withIndex(withId(R.id.ivBack), 1)).perform(click());

        // Confirm that the search main layout is displayed.
        onView(withId(R.id.searchMainLayout)).check(matches(isDisplayed()));
    }

    /**
     * Custom matcher to return the view at a given index among those that match the provided matcher.
     * This helps disambiguate multiple views with the same id, but idk why we even have this happening tbh.
     */
    public static Matcher<View> withIndex(final Matcher<View> matcher, final int index) {
        return new BoundedMatcher<View, View>(View.class) {
            int currentIndex = 0;
            @Override
            public void describeTo(Description description) {
                description.appendText("with index: ");
                description.appendValue(index);
                matcher.describeTo(description);
            }
            @Override
            public boolean matchesSafely(View view) {
                if (matcher.matches(view)) {
                    if (currentIndex == index) {
                        return true;
                    }
                    currentIndex++;
                }
                return false;
            }
        };
    }
}
