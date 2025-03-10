package com.hamidat.nullpointersapp;

import android.os.SystemClock;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;

import com.hamidat.nullpointersapp.mainFragments.SplashFragment;

/**
 * UI tests for the {@link com.hamidat.nullpointersapp.mainFragments.SplashFragment} class.
 * <p>
 * Verifies that the splash screen displays briefly and then
 * transitions to {@link AuthActivity}.
 */
@RunWith(AndroidJUnit4.class)
public class SplashActivityTest {

    /**
     * Launches SplashActivity before each test and provides
     * the ability to monitor launched intents.
     */
    @Rule
    public IntentsTestRule<SplashFragment> splashActivityRule =
            new IntentsTestRule<>(SplashFragment.class);

    /**
     * Tests that the SplashActivity transitions to AuthActivity
     * after the splash delay (2 seconds) elapses.
     */
    @Test
    public void testSplashTransitionToAuthActivity() {
        // Wait slightly longer than the splash delay to ensure transition has occurred
        SystemClock.sleep(2500);

        // Verify that an Intent was fired to start AuthActivity
        intended(hasComponent(AuthActivity.class.getName()));
    }
}
