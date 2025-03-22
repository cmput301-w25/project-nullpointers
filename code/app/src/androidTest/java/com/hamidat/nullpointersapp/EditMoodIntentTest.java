package com.hamidat.nullpointersapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hamidat.nullpointersapp.utils.testUtils.TestMoodHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import androidx.test.espresso.contrib.RecyclerViewActions;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class EditMoodIntentTest extends BaseUITest{

    @Before
    public void setUpMood() {
        TestMoodHelper.insertTestMood(
                TEST_USER_ID,
                "Sad",
                "I'm feeling pretty good!",
                51.5074, -0.1278,
                "Alone",
                false
        );
    }

    @Test
    public void editMoodShouldUpdateMoodEntry() {
        onView(withId(R.id.rvMoodList)).check(matches(isDisplayed())); // This will block until the view appears
        // Small delay to let the navigation complete
        SystemClock.sleep(2000);

        // Click the Edit button on the first item in the RecyclerView (newest mood)
        Log.d("EditMoodTest", "The rvMoodList is now displayed");
        SystemClock.sleep(4000);

        onView(withId(R.id.rvMoodList))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0,
                        ViewActionsHelper.clickChildViewWithId(R.id.btnEdit)));
        Log.d("EditMoodTest", "Clicked the edit button on the new mood we just added");

        // Now, actually edit the mood
        // Edit the reason text
        onView(withId(R.id.Reason))
                .perform(replaceText("Actually, I'm feeling awesome today!"), closeSoftKeyboard());
        onView(withId(R.id.rbHappy)).perform(click()); // change the mood to happy
        onView(withId(R.id.rbGroup)).perform(click()); // Change the social situation from Alone to Group
        onView(withId(R.id.btnAttachLocation)).perform(click()); // detach the location

        // Save edited mood
        onView(withId(R.id.btnSaveEntry)).perform(click());
        Log.d("EditMoodTest", "Clicked save on edited mood");

        // Wait for HomeFeedFragment to load
        SystemClock.sleep(3000);
        // Ensure the home feed is visible again
        onView(withId(R.id.rvMoodList)).check(matches(isDisplayed()));

        // Click the most recent mood entry to view details (the one that was just edited)
        onView(withId(R.id.rvMoodList))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0,
                        ViewActionsHelper.clickChildViewWithId(R.id.btnEdit)));

        // Small wait to let EditMoodFragment load
        SystemClock.sleep(1000);

        // Verify that the edited changes persisted,so the edits rem
        onView(withId(R.id.Reason))
                .check(matches(withText("Actually, I'm feeling awesome today!")));
        onView(withId(R.id.rbHappy)).check(matches(ViewMatchers.isChecked()));
        onView(withId(R.id.rbGroup)).check(matches(ViewMatchers.isChecked()));

        Log.d("EditMoodTest", "Verified that the edited data is correctly populated in the EditMoodFragment");
    }

    @After
    public void tearDown() {
        TestMoodHelper.deleteMoodByDescription(
                TEST_USER_ID,
                "Actually, I'm feeling awesome today!"
        );
    }
}
