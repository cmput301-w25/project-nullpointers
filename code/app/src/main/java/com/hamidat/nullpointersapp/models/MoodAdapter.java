package com.hamidat.nullpointersapp.models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

    /**
     * Creates a new ViewHolder for a mood item.
     *
     * @param parent   The parent ViewGroup.
     * @param viewType The view type of the new view.
     * @return A new MoodViewHolder instance.
     */
    @NonNull
    @Override
    public MoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_mood_card, parent, false);
        return new MoodViewHolder(itemView);
    }

    /**
     * Binds a Mood object to the ViewHolder.
     *
     * @param holder   The MoodViewHolder.
     * @param position The position of the item in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull MoodViewHolder holder, int position) {
        Mood currentMood = moods.get(position);
        holder.bind(currentMood);
    }

    /**
     * Returns the number of Mood items.
     *
     * @return The size of the moods list.
     */
    @Override
    public int getItemCount() {
        return moods.size();
    }

    /**
     * Adds a new Mood at the top of the list.
     *
     * @param newMood The new Mood object to add.
     */
    public void addMood(Mood newMood) {
        moods.add(0, newMood);
        notifyItemInserted(0);
    }

    /**
     * Replaces the current list of moods with a new list.
     *
     * @param newMoods The new list of Mood objects.
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
        public Button btnEdit;

        /**
         * Constructs a new MoodViewHolder.
         *
         * @param itemView The view representing a single mood item.
         */
        public MoodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMood            = itemView.findViewById(R.id.tvMood);
            tvMoodDescription = itemView.findViewById(R.id.tvMoodDescription);
            tvTimestamp       = itemView.findViewById(R.id.tvTimestamp);
            tvSocialSituation = itemView.findViewById(R.id.tvSocialSituation);
            ivMoodImage = itemView.findViewById(R.id.ivMoodCardImgIfExists);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }

        /**
         * Binds a Mood object to the TextViews in the item view.
         *
         * @param mood The Mood object to display.
         */
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

            // we have to handle image loading (Show only if image exists) -> I'm not showing a default, just hiding if if thee is not
            if (mood.getImageBase64() != null && !mood.getImageBase64().isEmpty()) {
                try {
                    byte[] decodedBytes = Base64.decode(mood.getImageBase64(), Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    ivMoodImage.setImageBitmap(bitmap);
                    ivMoodImage.setVisibility(View.VISIBLE);  // Show ImageView
                } catch (Exception e) {
                    ivMoodImage.setImageResource(R.drawable.ic_default_image);
                    ivMoodImage.setVisibility(View.VISIBLE);  // Show with default image
                }
            } else {
                ivMoodImage.setVisibility(View.GONE);  // Hide ImageView if no image
            }
        }
    }
}
