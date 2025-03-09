package com.hamidat.nullpointersapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.hamidat.nullpointersapp.authFragments.LoginFragment;



/**
 * Handles user authentication by displaying relevant fragments.
 */
public class AuthActivity extends AppCompatActivity {

    /**
     * Called when the activity is created.
     *
     * @param savedInstanceState The previously saved state, if any.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // Load the LoginFragment by default if not already restored
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.auth_fragment_container, new LoginFragment())
                    .commit();
        }
    }

    /**
     * Switches the current fragment.
     *
     * @param fragment The fragment to display.
     */
    public void switchToFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.auth_fragment_container, fragment)
                .addToBackStack(null) // Enables back navigation
                .commit();
    }
}
