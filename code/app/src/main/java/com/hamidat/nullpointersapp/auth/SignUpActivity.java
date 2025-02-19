package com.hamidat.nullpointersapp.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hamidat.nullpointersapp.R;

public class SignUpActivity extends AppCompatActivity {

    /**
     * Called when the activity is created.
     * Sets up the layout, binds UI elements, and initializes the user profile.
     *
     * @param savedInstanceState The previously saved state, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Bind the UI elements
        EditText etSignUpPassword = findViewById(R.id.etSignUpPassword);
        EditText etSignupUsername = findViewById(R.id.etSignupUsername);
        TextView tvSignUpSubtitle = findViewById(R.id.tvSignupSubtitle);
        Button signUpButton = findViewById(R.id.signUpButton);

        // Add a listener to the signup button
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Give visual feedback that the click was recieved

                // Call validation to make sure no empty fields

                // Call validation to ensure a unique username
            }
        });

    }
}
