package com.hamidat.nullpointersapp;

import android.os.SystemClock;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.util.HumanReadables;
import androidx.test.espresso.util.TreeIterables;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.concurrent.TimeoutException;

import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;

public class ViewActionsHelper {

    public static ViewAction clickChildViewWithId(final int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "Click on a child view with specified id.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                View childView = view.findViewById(id);
                if (childView != null && childView.isShown()) {
                    childView.performClick();
                }
            }
        };
    }

    public static ViewAction waitForView(final Matcher<View> viewMatcher, final long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "Wait up to " + millis + "ms for view: " + viewMatcher.toString();
            }

            @Override
            public void perform(final UiController uiController, final View rootView) {
                final long startTime = SystemClock.elapsedRealtime();
                final long endTime = startTime + millis;

                do {
                    for (View view : TreeIterables.breadthFirstViewTraversal(rootView)) {
                        if (viewMatcher.matches(view) && view.isShown()) {
                            return;
                        }
                    }
                    uiController.loopMainThreadForAtLeast(50);
                } while (SystemClock.elapsedRealtime() < endTime);

                throw new PerformException.Builder()
                        .withActionDescription(this.getDescription())
                        .withViewDescription(HumanReadables.describe(rootView))
                        .withCause(new TimeoutException())
                        .build();
            }
        };
    }

    /**
     * Matcher for checking RecyclerView item count.
     */
    public static Matcher<View> hasItemCount(int count) {
        return new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof RecyclerView)) return false;
                RecyclerView recyclerView = (RecyclerView) view;
                return recyclerView.getAdapter() != null &&
                        recyclerView.getAdapter().getItemCount() == count;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("RecyclerView should have " + count + " items");
            }
        };
    }
}
