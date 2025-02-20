package com.hamidat.nullpointersapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MoodActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // Use the existing XML layout

        // Find the + button and set up its click listener
        ImageButton btnAddMood = findViewById(R.id.btnAddMood);
        btnAddMood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show a Toast popup when clicked
                Toast.makeText(MoodActivity.this, "Done", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

