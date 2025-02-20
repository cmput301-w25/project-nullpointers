package com.hamidat.nullpointersapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // New XML for landing page

        // Find button and set click listener to navigate to MoodActivity
        Button btnOpenMoodActivity = findViewById(R.id.btnOpenMoodActivity);
        btnOpenMoodActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MoodActivity.class);
                startActivity(intent);
            }
        });

        // Button for FollowingMoodActivity
        Button btnOpenFollowingMoodActivity = findViewById(R.id.btnOpenFollowingMoodActivity);
        btnOpenFollowingMoodActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FollowingMoodActivity.class);
                startActivity(intent);
            }
        });
    }
}
