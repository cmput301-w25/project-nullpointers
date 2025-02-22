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
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MoodArrayAdapter extends RecyclerView.Adapter<MoodArrayAdapter.MoodViewHolder> {
    private Context context;
    private List<Mood> moodList;

    public MoodArrayAdapter(Context context, List<Mood> moodList) {
        this.context = context;
        this.moodList = moodList;
    }

    @NonNull
    @Override
    public MoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mood_item, parent, false);
        return new MoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoodViewHolder holder, int position) {
        Mood mood = moodList.get(position);
        holder.moodNameView.setText(mood.getMoodName());
        holder.dateView.setText("Date: " + mood.getDate());
        holder.descriptionView.setText("Description: " + mood.getDescription());
    }

    @Override
    public int getItemCount() {
        return moodList.size();
    }

    public static class MoodViewHolder extends RecyclerView.ViewHolder {
        TextView moodNameView, dateView, descriptionView;

        public MoodViewHolder(View itemView) {
            super(itemView);
            moodNameView = itemView.findViewById(R.id.mood_name);
            dateView = itemView.findViewById(R.id.mood_date);
            descriptionView = itemView.findViewById(R.id.mood_description);
        }
    }
}
