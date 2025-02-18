package com.hamidat.nullpointersapp;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.Mood; //uses mood
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MoodAdapter extends RecyclerView.Adapter<MoodAdapter.MoodViewHolder> {

    private final ArrayList<Mood> moodList;

    public MoodAdapter(List<Mood> moodList) {
        this.moodList = new ArrayList<>(moodList);
    }

    @NonNull
    @Override
    public MoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mood, parent, false);
        return new MoodViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MoodViewHolder holder, int position) {
        Mood mood = moodList.get(position);
        holder.title.setText(mood.getTitle());
        holder.comment.setText(mood.getDesc());
        holder.emotionalState.setText("Mood: " + mood.getEmotionalState());

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        holder.dateTime.setText(sdf.format(mood.getDateTime()));
    }

    @Override
    public int getItemCount() {
        return moodList.size();
    }

    static class MoodViewHolder extends RecyclerView.ViewHolder {
        TextView title, comment, emotionalState, dateTime;

        public MoodViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.moodTitle);
            comment = itemView.findViewById(R.id.moodComment);
            emotionalState = itemView.findViewById(R.id.moodState);
            dateTime = itemView.findViewById(R.id.moodDate);
        }
    }
}
