package com.hamidat.nullpointersapp;

import static com.hamidat.nullpointersapp.utils.AuthHelpers.giveAuthNotification;
import static com.hamidat.nullpointersapp.utils.AuthHelpers.validateNoEmptyFields;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Bind the UI elements
        EditText etLoginUsername = findViewById(R.id.etLoginUsername);
        EditText etLoginPassword = findViewById(R.id.etLoginPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegisterNow = findViewById(R.id.tvRegisterNow);

        // Listen to if the user wanted to login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Give feedback that the request was received
                giveAuthNotification(LoginActivity.this, "Request to login received");

                // Validate that there's no empty fields
                boolean noEmptyFields = validateNoEmptyFields(LoginActivity.this, etLoginUsername, etLoginPassword);

                String loginUsername = etLoginUsername.getText().toString().trim();
                String loginPassword = etLoginPassword.getText().toString().trim();

                // Attempt to login
                boolean loginSuccess = loginUser(loginUsername, loginPassword);
                if (!loginSuccess) {
                    return;
                } else {
                    // TODO - Create a token or method of keeping tracked of the logged in method throughout the app
                    Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
                    startActivity(intent);
                }
            }
        });

        // Listen to the "register now" instead hyperlink
        tvRegisterNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                giveAuthNotification(LoginActivity.this, "Request to login instead received");
            }
        });
    }

    // Login user function
    public boolean loginUser(String loginUsername, String loginPassword) {
        // TODO - Make connection to the db to login the user. For now just return true saying they're a valid user
        return true;
    }
}
