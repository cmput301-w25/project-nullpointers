package com.hamidat.nullpointersapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.os.SystemClock;
import android.util.Log;

import com.hamidat.nullpointersapp.utils.testUtils.TestUsersHelper;

import org.junit.After;
import org.junit.Test;

public class SignUpIntentTest extends BaseAuthActivityUITest {
    private static final String TAG = "LoginIntentTest";
    private static final String TEST_REGISTRATION_USERNAME = "registrationTestUser";
    private static final String TEST_REGISTRATION_PASSWORD = "password123";


    @Test
    public void canRegisterANewUserAndLogin(){
        onView(withId(R.id.tvRegisterNow)).perform(click());
        SystemClock.sleep(2000);

        // Type a unique username and password
        onView(withId(R.id.etSignupUsername)).perform(replaceText(TEST_REGISTRATION_USERNAME));
        onView(withId(R.id.etSignUpPassword)).perform(replaceText(TEST_REGISTRATION_PASSWORD));

        // Click the Sign Up button
        onView(withId(R.id.btnSignUp)).perform(click());

        // wait for the fragment switch to the login
        SystemClock.sleep(3000);

        // Wait for fragment switch and verify that LoginFragment is displayed
        onView(withId(R.id.etLoginUsername))
                .perform(typeText(TEST_REGISTRATION_USERNAME), closeSoftKeyboard());

        onView(withId(R.id.etLoginPassword))
                .perform(typeText(TEST_REGISTRATION_PASSWORD), closeSoftKeyboard());

        onView(withId(R.id.btnLogin)).perform(click());

        SystemClock.sleep(2000);

        // Check if the intent to launch MainActivity was triggered
        intended(hasComponent(MainActivity.class.getName()));
        Log.d(TAG, "Ending test: canRegisterANewUserAndLogin");
    }

    @After
    public void cleanUpAddedUser() {
        TestUsersHelper.deleteUserByUsername(TEST_REGISTRATION_USERNAME);
    }
}
