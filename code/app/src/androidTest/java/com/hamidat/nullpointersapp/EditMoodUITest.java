package com.hamidat.nullpointersapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;
import android.os.Bundle;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import com.google.firebase.firestore.FirebaseFirestore;
import com.hamidat.nullpointersapp.mainFragments.EditMoodFragment;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class EditMoodUITest {

    @BeforeClass
    public static void setupFirestoreEmulator() {
        FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080);
    }

    // Automatically grant runtime permissions so that system dialogs don't interrupt Espresso.
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            android.Manifest.permission.POST_NOTIFICATIONS,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    );

    // launch mainActivity with a dummy user ID
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class)
                    .putExtra("USER_ID", "testUser"));

    @Test
    public void editMoodShouldUpdateMoodEntry() {
        // Add a mood first so I can actually edit it
        // 1. Add a mood
        // Click on the Add Mood icon to open AddMoodFragment
        onView(withId(R.id.ivAddMood)).perform(click());
        // Verify the Add Mood UI is displayed (checking the title)
        onView(withId(R.id.tvAddNewMoodEvent)).check(matches(isDisplayed()));
        // Enter a reason for the mood
        onView(withId(R.id.Reason))
                .perform(typeText("Feeling great!"), closeSoftKeyboard());
        // Select a mood (e.g., Happy)
        onView(withId(R.id.rbHappy)).perform(click());
        // Select a social situation (e.g., One-on-One)
        onView(withId(R.id.rbOneOnOne)).perform(click());
        // Toggle off location to avoid validation issues (default is attached)
        onView(withId(R.id.btnAttachLocation)).perform(click());
        // Click on the Save button
        onView(withId(R.id.btnSaveEntry)).perform(click());

        // Launch the EditMoodFragment with a dummy mood object.
        activityRule.getScenario().onActivity(activity -> {
            // Create a dummy mood object.
            Mood dummyMood = new Mood();
            dummyMood.setMoodId("dummy_id");
            dummyMood.setMoodDescription("Original reason");
            dummyMood.setMood("Sad");
            dummyMood.setSocialSituation("Alone");
            dummyMood.setLatitude(12.34);
            dummyMood.setLongitude(56.78);
            dummyMood.setImageBase64(""); // no image

            // Prepare arguments.
            Bundle bundle = new Bundle();
            bundle.putSerializable("mood", dummyMood);

            // Create the EditMoodFragment and set its arguments.
            EditMoodFragment fragment = new EditMoodFragment();
            fragment.setArguments(bundle);

            // Replace the fragment container (using the nav_host_fragment from MainActivity)
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, fragment, "EditMoodFragment")
                    .commitNow();
        });

        // Verify that the original reason text is present.
        onView(withId(R.id.Reason)).check(matches(withText("Original reason")));

        // Change the text in the reason field.
        onView(withId(R.id.Reason))
                .perform(clearText(), typeText("Updated reason"), closeSoftKeyboard());

        // Change mood selection from "Sad" to "Happy"
        // (Assuming the radio button for Happy has id rbHappy)
        onView(withId(R.id.rbHappy)).perform(click());

        // Change social situation from "Alone" to "One on One"
        // (Assuming the radio button for One on One has id rbOneOnOne)
        onView(withId(R.id.rbOneOnOne)).perform(click());

        // Toggle off location attachment (if required).
        onView(withId(R.id.btnAttachLocation)).perform(click());

        // Click on the Save button.
        onView(withId(R.id.btnSaveEntry)).perform(click());
    }

    /**
     * A fake FirestoreHelper that simulates a successful update immediately.
     */
    public static class FakeFirestoreHelper extends FirestoreHelper {
        @Override
        public void updateMood(Mood mood, FirestoreHelper.FirestoreCallback callback) {
            // Immediately simulate a successful update.
            callback.onSuccess(null);
        }
    }

    /**
     * A TestMainActivity that extends your MainActivity.
     * It overrides getFirestoreHelper() to return our fake helper.
     */
    public static class TestMainActivity extends MainActivity {
        @Override
        public FirestoreHelper getFirestoreHelper() {
            return new FakeFirestoreHelper();
        }
    }

    /**
     * A custom matcher to verify Toast messages.
     */
    public static class ToastMatcher extends TypeSafeMatcher<android.view.View> {

        @Override
        public void describeTo(Description description) {
            description.appendText("is toast");
        }

        @Override
        public boolean matchesSafely(android.view.View view) {
            if (view.getWindowToken() == null) {
                return false;
            }
            int type = view.getLayoutParams().getClass().getSimpleName().equals("WindowManager$LayoutParams")
                    ? ((android.view.WindowManager.LayoutParams) view.getLayoutParams()).type : -1;
            // Check if the window type is TOAST
            return type == android.view.WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG
                    || type == android.view.WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
        }
    }
}
