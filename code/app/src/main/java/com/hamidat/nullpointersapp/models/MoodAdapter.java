package com.hamidat.nullpointersapp.models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.Timestamp;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.models.Mood;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for displaying a list of Mood objects.
 * Each item displays the mood state, description, timestamp, and social situation.
 *
 * @author
 *  (Salim Soufi)
 * @version 1.0
 * @since 2025-03-03
 */
public class MoodAdapter extends RecyclerView.Adapter<MoodAdapter.MoodViewHolder> {

    private final List<Mood> moods;

    /**
     * Constructs a new MoodAdapter.
     *
     * @param moods List of Mood objects to display.
     */
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

    @Override
    public void onBindViewHolder(@NonNull MoodViewHolder holder, int position) {
        Mood currentMood = moods.get(position);
        holder.bind(currentMood);

        // Handle "Edit" button click
        holder.btnEdit.setOnClickListener(v -> {
            // Navigate to EditMoodFragment with currentMood
            // The simplest approach is a Bundle with the Mood as a Serializable
            if (currentMood.getMoodId() != null) {
                // Create a bundle
                Bundle bundle = new Bundle();
                bundle.putSerializable("mood", currentMood);

                // Assumes an action (e.g., action_homeFeedFragment_to_editMoodFragment) in nav_graph
                Navigation.findNavController(v).navigate(R.id.action_homeFeedFragment_to_editMoodFragment, bundle);
            }
        });
    }

    @Override
    public int getItemCount() {
        return moods.size();
    }

    /**
     * Adds a new Mood at the top of the list.
     */
    public void addMood(Mood newMood) {
        moods.add(0, newMood);
        notifyItemInserted(0);
    }

    /**
     * Replaces the current list of moods with a new list.
     */
    public void updateMoods(List<Mood> newMoods) {
        moods.clear();
        moods.addAll(newMoods);
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class that binds a Mood object to its corresponding views.
     */
    static class MoodViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMood;
        private final TextView tvMoodDescription;
        private final TextView tvTimestamp;
        private final TextView tvSocialSituation;
        private final ImageView ivMoodImage;

        // Added for Edit button
        public final Button btnEdit;
        public final Button btnComment; // (Existing or for reference)

        public MoodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMood = itemView.findViewById(R.id.tvMood);
            tvMoodDescription = itemView.findViewById(R.id.tvMoodDescription);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvSocialSituation = itemView.findViewById(R.id.tvSocialSituation);
            ivMoodImage = itemView.findViewById(R.id.ivMoodCardImgIfExists);

            // Hook the newly added Edit button & the existing Comment button
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnComment = itemView.findViewById(R.id.btnComment);
        }

        void bind(Mood mood) {
            tvSocialSituation.setText(mood.getSocialSituation());
            tvMood.setText("Mood: " + mood.getMood());
            tvMoodDescription.setText(mood.getMoodDescription());

            Timestamp ts = mood.getTimestamp();
            if (ts != null) {
                Date date = ts.toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
                tvTimestamp.setText(sdf.format(date));
            } else {
                tvTimestamp.setText("No Timestamp");
            }

            // Show/hide image
            if (mood.getImageBase64() != null && !mood.getImageBase64().isEmpty()) {
                try {
                    byte[] decodedBytes = Base64.decode(mood.getImageBase64(), Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    ivMoodImage.setImageBitmap(bitmap);
                    ivMoodImage.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    ivMoodImage.setImageResource(R.drawable.ic_default_image);
                    ivMoodImage.setVisibility(View.VISIBLE);
                }
            } else {
                ivMoodImage.setVisibility(View.GONE);
            }
        }
    }
}
