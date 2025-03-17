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
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreDeleteMood;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for displaying a list of Mood objects.
 * If the mood belongs to the current user, the item layout (item_mood_card.xml)
 * displays Edit and Delete buttons; otherwise, a public layout (item_mood_card_public.xml)
 * is used, which shows only the Comment button.
 *
 * @author
 * (Salim Soufi)
 * @version 1.0
 * @since 2025-03-03
 */
public class MoodAdapter extends RecyclerView.Adapter<MoodAdapter.MoodViewHolder> {

    private final List<Mood> moods;
    private final String currentUserId;

    /**
     * Constructs a new MoodAdapter.
     *
     * @param moods         List of Mood objects to display.
     * @param currentUserId The ID of the currently signed-in user.
     */
    public MoodAdapter(List<Mood> moods, String currentUserId) {
        this.moods = moods;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Mood mood = moods.get(position);
        return (mood.getUserId() != null && mood.getUserId().equals(currentUserId)) ?
                R.layout.item_mood_card :
                R.layout.item_mood_card_public;
    }

    @NonNull
    @Override
    public MoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(viewType, parent, false);
        return new MoodViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MoodViewHolder holder, int position) {
        Mood currentMood = moods.get(position);
        holder.bind(currentMood);

        // Show/hide the "edited" label.
        holder.tvEdited.setVisibility(currentMood.isEdited() ? View.VISIBLE : View.GONE);

        // Check if this is the owner's mood
        boolean isOwnMood = currentMood.getUserId() != null && currentMood.getUserId().equals(currentUserId);

        System.out.println("Mood ID: " + currentMood.getMoodId() + " | isOwnMood: " + isOwnMood);

        if (isOwnMood) {
            if (holder.btnEdit != null) {
                holder.btnEdit.setOnClickListener(v -> {
                    if (currentMood.getMoodId() != null) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("mood", currentMood);
                        Navigation.findNavController(v)
                                .navigate(R.id.action_homeFeedFragment_to_editMoodFragment, bundle);
                    }
                });
            }

            if (holder.btnDelete != null) {
                holder.btnDelete.setOnClickListener(v -> {
                    int adapterPosition = holder.getAdapterPosition();
                    if (adapterPosition == RecyclerView.NO_POSITION) return;

                    Mood moodToDelete = moods.get(adapterPosition);
                    FirestoreDeleteMood deleteUtil =
                            new FirestoreDeleteMood(FirebaseFirestore.getInstance());

                    deleteUtil.deleteMood(moodToDelete.getUserId(), moodToDelete, new FirestoreHelper.FirestoreCallback() {
                        @Override
                        public void onSuccess(Object result) {
                            Toast.makeText(v.getContext(), "Mood deleted successfully.", Toast.LENGTH_SHORT).show();
                            moods.remove(adapterPosition);
                            notifyItemRemoved(adapterPosition);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(v.getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            }
        } else {
            if (holder.btnComment != null) {
                holder.btnComment.setOnClickListener(v -> {
                    Toast.makeText(v.getContext(), "Comment button clicked.", Toast.LENGTH_SHORT).show();
                });
            }
        }
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
        private final TextView tvEdited;
        private final TextView tvMoodDescription;
        private final TextView tvTimestamp;
        private final TextView tvSocialSituation;
        private final ImageView ivMoodImage;
        private final Button btnEdit;
        private final Button btnComment;
        private final Button btnDelete;

        /**
         * Constructs a new MoodViewHolder.
         *
         * @param itemView The item view.
         */
        public MoodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMood = itemView.findViewById(R.id.tvMood);
            tvMoodDescription = itemView.findViewById(R.id.tvMoodDescription);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvSocialSituation = itemView.findViewById(R.id.tvSocialSituation);
            tvEdited = itemView.findViewById(R.id.tvEdited);
            ivMoodImage = itemView.findViewById(R.id.ivMoodCardImgIfExists);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
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

            // Display mood image if available
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
