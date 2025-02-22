package com.hamidat.nullpointersapp;

import android.os.Bundle;
import com.hamidat.nullpointersapp.Mood;
import com.hamidat.nullpointersapp.AddMoodFragment;
import com.hamidat.nullpointersapp.MoodManager;
import com.hamidat.nullpointersapp.MoodArrayAdapter;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MoodActivity extends AppCompatActivity implements AddMoodFragment.AddMoodDialogListener {
    private MoodArrayAdapter moodAdapter;
    private ArrayList<Mood> moodList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // Use the existing XML layout

        // Retrieve the mood list from MoodManager instead of creating a new one
        moodList = (ArrayList<Mood>) MoodManager.getInstance().getMoodList();
        moodAdapter = new MoodArrayAdapter(this, moodList);

        RecyclerView moodRecyclerView = findViewById(R.id.rvMoodList);
        moodRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        moodRecyclerView.setAdapter(moodAdapter);


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

    // **Add this missing method**
    @Override
    public void addMood(Mood mood, int index) {
        if (index >= 0 && index < moodList.size()) {
            moodList.set(index, mood);
        } else {
            moodList.add(mood);
        }
        moodAdapter.notifyDataSetChanged();
    }
}

