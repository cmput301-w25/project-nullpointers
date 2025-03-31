package com.hamidat.nullpointersapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.os.SystemClock;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LoginIntentTest extends BaseAuthActivityUITest {

    private static final String TAG = "LoginIntentTest";

    @Test
    public void canUseGuestUserLogin() {
        Log.d(TAG, "Starting test: canUseGuestUserLogin");

        // Click the "Skip Auth" guest login button
        onView(withId(R.id.btnSkipAuthForDemo)).perform(click());

        SystemClock.sleep(2000);

        // Check if the intent to launch MainActivity was triggered
        intended(hasComponent(MainActivity.class.getName()));
        Log.d(TAG, "Ending test: canUseGuestUserLogin");
    }

    @Test
    public void canLoginWithValidInfo() {
        Log.d(TAG, "Starting test: canLoginWithValidInfo");

        onView(withId(R.id.etLoginUsername))
                .perform(typeText(TEST_LOGIN_USERNAME), closeSoftKeyboard());

        onView(withId(R.id.etLoginPassword))
                .perform(typeText(TEST_LOGIN_PASSWORD), closeSoftKeyboard());

        onView(withId(R.id.btnLogin)).perform(click());

        SystemClock.sleep(2000);

        // Check if the intent to launch MainActivity was triggered
        intended(hasComponent(MainActivity.class.getName()));
        Log.d(TAG, "Ending test: canLoginWithValidInfo");
    }
}
