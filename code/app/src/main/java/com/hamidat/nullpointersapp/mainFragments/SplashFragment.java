package com.hamidat.nullpointersapp.mainFragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

import com.hamidat.nullpointersapp.AuthActivity;
import com.hamidat.nullpointersapp.R;


public class SplashFragment extends AppCompatActivity{
    // Delay in milliseconds (2000ms = 2 seconds)
    private static final long SPLASH_DELAY = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use the splash layout (you can directly use the fragment layout)
        setContentView(R.layout.fragment_splash_screen);

        // Post a delayed task to navigate to the next screen after SPLASH_DELAY
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Start AuthActivity (or MainActivity, based on your app flow)
            startActivity(new Intent(SplashFragment.this, AuthActivity.class));
            // Finish the SplashActivity so the user can't go back to it
            finish();
        }, SPLASH_DELAY);
    }
}
