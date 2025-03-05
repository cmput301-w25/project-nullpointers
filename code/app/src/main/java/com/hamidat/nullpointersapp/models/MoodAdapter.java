package com.hamidat.nullpointersapp.models;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.Timestamp;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.models.Mood;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A RecyclerView adapter for displaying a list of Mood objects
 * in item_mood_card.xml layouts. Each item view shows the
 * Mood's main fields such as mood state, description,
 * and timestamp.
 *
 * @author
 *  (Salim Soufi)
 * @version 1.0
 * @since 2025-03-03
 */


public class MoodAdapter extends RecyclerView.Adapter<MoodAdapter.MoodViewHolder> {

    private final List<Mood> moods;

    public MoodAdapter(List<Mood> moods) {
        this.moods = moods;
    }

    @NonNull
    @Override
    public MoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_mood_card, parent, false);
        return new MoodViewHolder(itemView);
    }

    // For binding (populating) each item of the list
    @Override
    public void onBindViewHolder(@NonNull MoodViewHolder holder, int position) {
        Mood currentMood = moods.get(position);
        holder.bind(currentMood);
    }

    @Override
    public int getItemCount() {
        return moods.size();
    }

    // adds a new Mood at the top of the list
    public void addMood(Mood newMood) {
        moods.add(0, newMood);         // Insert at position top
        notifyItemInserted(0);
    }
    public void updateMoods(List<Mood> newMoods) {
        moods.clear();
        moods.addAll(newMoods);
        notifyDataSetChanged();
    }


    // ViewHolder holds references to views for each item
    /**
     * ViewHolder: binds a single Mood to the TextViews.
     */
    static class MoodViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvMood;
        private final TextView tvMoodDescription;
        private final TextView tvTimestamp;
        private final TextView tvSocialSituation;

        public MoodViewHolder(@NonNull View itemView) {
            super(itemView);
            // Match these IDs to item_mood_card.xml
            tvMood            = itemView.findViewById(R.id.tvMood);
            tvMoodDescription = itemView.findViewById(R.id.tvMoodDescription);
            tvTimestamp       = itemView.findViewById(R.id.tvTimestamp);
            tvSocialSituation = itemView.findViewById(R.id.tvSocialSituation);
        }

        void bind(Mood mood) {

            tvSocialSituation.setText(mood.getSocialSituation());
            tvMood.setText("Mood: " + mood.getMood());

            // shows the mood description
            tvMoodDescription.setText(mood.getMoodDescription());

            // Convert Firebase Timestamp to a readable string
            Timestamp ts = mood.getTimestamp();
            if (ts != null) {
                Date date = ts.toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
                tvTimestamp.setText(sdf.format(date));
            } else {
                tvTimestamp.setText("No Timestamp");
            }
        }
    }
}


