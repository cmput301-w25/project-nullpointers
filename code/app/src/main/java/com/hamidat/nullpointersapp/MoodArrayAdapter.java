package com.hamidat.nullpointersapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class MoodArrayAdapter extends ArrayAdapter<Mood> {
    public MoodArrayAdapter(Context context, ArrayList<Mood> moods) {
        super(context, 0, moods);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = (convertView == null) ? LayoutInflater.from(getContext()).inflate(R.layout.mood_item, parent, false) : convertView;

        Mood mood = getItem(position);
        if (mood != null) {
            TextView moodNameView = view.findViewById(R.id.edit_text_mood_name);
            TextView dateView = view.findViewById(R.id.edit_text_date);
            TextView descriptionView = view.findViewById(R.id.edit_text_description);


            moodNameView.setText(mood.getMoodName());
            dateView.setText("Date: " + mood.getDate());
            descriptionView.setText("Description: " + mood.getDescription());

//            editButton.setOnClickListener(v -> {
//                if (getContext() instanceof MainActivity) {
//                    MainActivity activity = (MainActivity) getContext();
//                    activity.openEditMoodDialog(mood, position);
//                }
//            });
//
//            deleteButton.setOnClickListener(v -> {
//                if (getContext() instanceof MainActivity) {
//                    MainActivity activity = (MainActivity) getContext();
//                    activity.deleteMood(position);
//                }
//            });
        }
        return view;
    }
}