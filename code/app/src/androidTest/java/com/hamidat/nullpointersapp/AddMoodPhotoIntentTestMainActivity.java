package com.hamidat.nullpointersapp;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.content.FileProvider;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import com.hamidat.nullpointersapp.utils.testUtils.TestMoodHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AddMoodPhotoIntentTestMainActivity extends BaseMainActivityUITest {
    private static final String TAG = "AddMoodWithPhotoTest";

    @Test
    public void addMoodWithPhotoShouldSuceed() {
        // I need intents to simulate the user "clicking" adding a photo even though I'm auto genning it
        Intents.init();

        try {

            Log.d(TAG, "Starting test: addMoodWithPhotoShouldSuceed");

            // Start AddMoodFragment
            Log.d(TAG, "Clicking Add Mood icon");

            // Launch AddMoodFragment
            onView(withId(R.id.ivAddMood)).perform(click());
            onView(withId(R.id.tvAddNewMoodEvent)).check(matches(isDisplayed()));

            // Fill in mood fields
            onView(withId(R.id.Reason)).perform(typeText("Mood with photo!"), closeSoftKeyboard());

            // Select mood from Spinner
            onView(withId(R.id.spinnerMood)).perform(click());
            onView(withText("Happy")).perform(click());

            onView(withId(R.id.rbGroup)).perform(click());
            onView(withId(R.id.btnAttachLocation)).perform(click());

            // Prepare mock image intent result
            Log.d(TAG, "Creating test image URI");
            Uri imageUri = createTestImageUri();

            if (imageUri != null) {
                Log.d(TAG, "Image URI created: " + imageUri.toString());

                Intent resultData = new Intent();
                resultData.setData(imageUri);

                Log.d(TAG, "Stubbing intent response for image picker");
                intending(hasAction(Intent.ACTION_GET_CONTENT)).respondWith(
                        new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)
                );
            } else {
                Log.e(TAG, "Image URI is null. Skipping image picker intent.");
            }
            SystemClock.sleep(1000); // Let the UI settle and preview load

            // Click "Attach Photo"
            Log.d(TAG, "Clicking Attach Photo");
            onView(withId(R.id.AttachPhoto)).perform(click());
            onView(withId(R.id.btnSaveEntry)).perform(click());
            SystemClock.sleep(2000);

            Log.d(TAG, "Checking rvMoodList is displayed");
            onView(withId(R.id.rvMoodList)).check(matches(isDisplayed()));

            Log.d(TAG, "Test finished: Mood with photo should be added successfully");

        }  finally {
            Intents.release();
        }
    }

    private Uri createTestImageUri() {
        Context context = ApplicationProvider.getApplicationContext();
        File testImageFile = new File(context.getCacheDir(), "test_image.jpg");

        try (InputStream input = context.getAssets().open("test_image.jpg");
             OutputStream output = new FileOutputStream(testImageFile)) {
            byte[] buffer = new byte[1024];
            int len;

            while ((len = input.read(buffer)) > 0) {
                output.write(buffer, 0, len);
            }
            Log.d(TAG, "Test image copied successfully");

        } catch (IOException e) {
            Log.e(TAG, "Failed to copy test image: " + e.getMessage(), e);
            return null;
        }

        Uri uri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".provider",
                testImageFile
        );

        Log.d(TAG, "Returning FileProvider URI: " + uri);
        return uri;
    }

    @After
    public void tearDown() {
        TestMoodHelper.deleteMoodByDescription(TEST_USER_ID, "Mood with photo!");
        Log.d(TAG, "Teardown complete");
    }
}
