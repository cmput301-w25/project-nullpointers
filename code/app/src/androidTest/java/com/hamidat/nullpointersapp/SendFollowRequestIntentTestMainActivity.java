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

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SendFollowRequestIntentTestMainActivity extends BaseMainActivityUITest {

    private static final String TEST_SEARCH_QUERY = "guestUser";

    @Test
    public void sendFollowRequestIntent_shouldChangeButtonTextToPending() {
        // Open the search screen.
        onView(withId(R.id.ivSearch)).perform(click());
        onView(withId(R.id.etSearch)).check(matches(isDisplayed()));

        // Type the search query.
        onView(withId(R.id.etSearch)).perform(replaceText(TEST_SEARCH_QUERY));
        SystemClock.sleep(2000);

        // Click the first result in the RecyclerView.
        onView(withId(R.id.rvResults))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        SystemClock.sleep(2000);

        // Verify that the profile overlay displays the correct username.
        onView(allOf(withId(R.id.username_text), withText(TEST_SEARCH_QUERY)))
                .check(matches(isDisplayed()));
        Log.d("SearchForOtherUsersIntentTest", "Profile overlay displayed with correct username");

        // Click the follow button (using the custom withIndex matcher) to send a follow request.
        onView(withIndex(withId(R.id.btnFollowUnfollow), 1))
                .perform(click());
        SystemClock.sleep(2000); // Wait for the async update to occur

        // Verify that the text on the follow button has changed to "Pending", which means that the follow request was actually sent
        onView(withIndex(withId(R.id.btnFollowUnfollow), 1))
                .check(matches(withText("Pending")));

        // Use the back button (with the custom withIndex matcher) to return to the search screen.
        onView(withIndex(withId(R.id.ivBack), 1)).perform(click());
        onView(withId(R.id.searchMainLayout)).check(matches(isDisplayed()));
    }

    /**
     * Custom matcher to return the view at a given index among those that match the provided matcher.
     * This helps disambiguate multiple views with the same id.
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
