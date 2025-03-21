package com.hamidat.nullpointersapp;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.espresso.Root;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import androidx.test.rule.GrantPermissionRule;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

@LargeTest
public class AddMoodFragmentTest {

    // Connect to the Firestore emulator (adjust port if needed)
    @BeforeClass
    public static void setupFirestoreEmulator() {
        FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080);
    }

    // default grant all permissions so the popups don't stop expresso from running
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            android.Manifest.permission.POST_NOTIFICATIONS,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    );

    // Launch MainActivity with a dummy user id so that AddMoodFragment can get a FirestoreHelper instance.
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class)
                    .putExtra("USER_ID", "testUser"));

    @Test
    public void addMoodShouldAddValidMoodEntry() {
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

    }

//    @Test
//    public void addMoodShouldShowErrorForEmptyReason() {
//        // Open AddMoodFragment
//        onView(withId(R.id.ivAddMood)).perform(click());
//
//        // Ensure the Add Mood UI is visible
//        onView(withId(R.id.tvAddNewMoodEvent)).check(matches(isDisplayed()));
//
//        // Do not enter any text into the Reason field.
//        // Select mood and social situation so that only the reason is missing.
//        onView(withId(R.id.rbSad)).perform(click());
//        onView(withId(R.id.rbGroup)).perform(click());
//
//        // Toggle off location to bypass the location validation.
//        onView(withId(R.id.btnAttachLocation)).perform(click());
//
//        // Attempt to save the mood entry.
//        onView(withId(R.id.btnSaveEntry)).perform(click());
//
//    }

//    @After
//    public void tearDown() {
//        String projectId = "moodify301";
//        URL url = null;
//        try {
//            url = new URL("http://10.0.2.2:8080/emulator/v1/projects/" + projectId + "/databases/(default)/documents");
//        } catch (MalformedURLException exception) {
//            Log.e("URL Error", Objects.requireNonNull(exception.getMessage()));
//        }
//        HttpURLConnection urlConnection = null;
//        try {
//            urlConnection = (HttpURLConnection) url.openConnection();
//            urlConnection.setRequestMethod("DELETE");
//            int response = urlConnection.getResponseCode();
//            Log.i("Response Code", "Response Code: " + response);
//        } catch (IOException exception) {
//            Log.e("IO Error", Objects.requireNonNull(exception.getMessage()));
//        } finally {
//            if (urlConnection != null) {
//                urlConnection.disconnect();
//            }
//        }
//    }

}
