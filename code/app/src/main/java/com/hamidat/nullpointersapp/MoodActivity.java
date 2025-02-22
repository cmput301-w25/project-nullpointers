package com.hamidat.nullpointersapp;

import android.os.Bundle;
import com.hamidat.nullpointersapp.Mood;
import com.hamidat.nullpointersapp.AddMoodFragment;
import com.hamidat.nullpointersapp.MoodManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MoodActivity extends AppCompatActivity implements AddMoodFragment.AddMoodDialogListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // Use the existing XML layout

        // Find the + button and set up its click listener
        ImageButton btnAddMood = findViewById(R.id.btnAddMood);
        btnAddMood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddMoodFragment addMoodFragment = new AddMoodFragment();
                addMoodFragment.show(getSupportFragmentManager(), "AddMoodFragment");
            }
        });


    }
    @Override
    public void addMood(Mood mood, int index) {
        MoodManager.getInstance().addMood(mood, index);
        // You may want to refresh your mood list here
    }
}

