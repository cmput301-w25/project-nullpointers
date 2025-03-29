/**
 * SplashActivity.java
 *
 * Initial entry point that displays the splash screen for a brief duration.
 * Transitions to AuthActivity after a short delay.
 *
 * Outstanding Issues: None
 */

package com.hamidat.nullpointersapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Displays a splash screen for a short duration when the app is launched.
 * After the delay, this activity transitions to the authentication screen.
 */
public class SplashActivity extends AppCompatActivity{
    // Delay in milliseconds (2000ms = 2 seconds)
    private static final long SPLASH_DELAY = 2000;

    /**
     * Called when the activity is created.
     *
     * <p>This method inflates the splash screen layout and schedules a delayed
     * transition to the authentication screen. Once the delay elapses,
     * {@link AuthActivity} is launched and this activity is finished so that
     * the user cannot return to the splash screen.</p>
     *
     * @param savedInstanceState The previously saved state, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use the splash layout
        setContentView(R.layout.fragment_splash_screen);

        // Post a delayed task to navigate to the next screen after SPLASH_DELAY
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Start AuthActivity
            startActivity(new Intent(SplashActivity.this, AuthActivity.class));
            // Finish the SplashActivity so the user can't go back to it
            finish();
        }, SPLASH_DELAY);
    }
}
