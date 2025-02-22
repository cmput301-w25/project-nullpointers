package com.hamidat.nullpointersapp.utils.homeFeedUtils;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hamidat.nullpointersapp.R;

import java.util.List;

public class MoodAdapter extends RecyclerView.Adapter<MoodAdapter.MoodViewHolder> {
//

//    CAN REMOVE THIS FILE IF NEEDED

 //
    private final List<String> moodItems;

    public MoodAdapter(List<String> moodItems) {
        this.moodItems = moodItems;
    }

    @NonNull
    @Override
    public MoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mood, parent, false);
        return new MoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoodViewHolder holder, int position) {
        holder.bind(moodItems.get(position));
    }

    @Override
    public int getItemCount() {
        return moodItems.size();
    }

    static class MoodViewHolder extends RecyclerView.ViewHolder {
        private final TextView moodText;

        public MoodViewHolder(@NonNull View itemView) {
            super(itemView);
            moodText = itemView.findViewById(R.id.tvMoodTitle);
        }

        public void bind(String mood) {
            moodText.setText(mood);
        }
    }
}