package com.hamidat.nullpointersapp.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
        EditText etSignupPassword = findViewById(R.id.etSignUpPassword);
        EditText etSignupUsername = findViewById(R.id.etSignupUsername);
        TextView tvSignUpSubtitle = findViewById(R.id.tvSignupSubtitle);
        Button signUpButton = findViewById(R.id.signUpButton);

        // Add a listener to the signup button
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Give visual feedback that the click was received
                Toast.makeText(SignUpActivity.this,
                        "SignUp Request Recieved",
                        Toast.LENGTH_SHORT).show();

                String signupUsername = etSignupUsername.getText().toString().trim();
                String signUpPassword = etSignupPassword.getText().toString().trim();

                // Call validation to make sure no empty fields
                boolean noEmptyFields = AuthHelpers.validateNoEmptyFields(SignUpActivity.this, etSignupUsername, etSignupPassword);
                if (!noEmptyFields) return;

                // Call validation to ensure a unique username
                boolean uniqueUserName = AuthHelpers.validateUniqueUsername(signupUsername);
                if (!uniqueUserName) return;

                // Add valid users into the db
                boolean signupSuccess = AuthHelpers.addNewUserToDB(SignUpActivity.this, signupUsername, signUpPassword);
                if (!signupSuccess) return;
            }
        });

    }
}
