package com.hamidat.nullpointersapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;
import android.os.SystemClock;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import androidx.test.espresso.contrib.RecyclerViewActions;

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
                .perform(typeText("I'm feeling pretty good!"), closeSoftKeyboard());
        // Select sad as the original mood
        onView(withId(R.id.rbSad)).perform(click());
        // Select a social situation (alone)
        onView(withId(R.id.rbAlone)).perform(click());
        onView(withId(R.id.btnAttachLocation)).perform(click());
        // Click on the Save button
        onView(withId(R.id.btnSaveEntry)).perform(click());

        // after this the UI goes back to the homefeed fragment
        // need to click the edit button on the specifc mood card

        // Wait briefly to ensure the home feed is updated (you can replace with IdlingResource later)
        SystemClock.sleep(2000);


        // Click the Edit button on the first item in the RecyclerView (newest mood)

        onView(withId(R.id.rvMoodList))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0,
                        ViewActionsHelper.clickChildViewWithId(R.id.btnEdit)));
    }

}
