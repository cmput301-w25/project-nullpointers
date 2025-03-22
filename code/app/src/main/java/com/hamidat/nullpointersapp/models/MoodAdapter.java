package com.hamidat.nullpointersapp.models;

import android.app.AlertDialog;
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
import com.google.android.material.imageview.ShapeableImageView;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.mainFragments.DeleteMoodFragment;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreDeleteMood;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * RecyclerView adapter for displaying a list of Mood objects.
 * If the mood belongs to the current user, the item layout (item_mood_card.xml)
 * displays Edit and Delete buttons; otherwise, a public layout (item_mood_card_public.xml)
 * is used, which shows only the Comment button.
 */
public class MoodAdapter extends RecyclerView.Adapter<MoodAdapter.MoodViewHolder> {

    private final List<Mood> moods;
    private final String currentUserId;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
    private FirestoreHelper firestoreHelper;

    /**
     * Constructs a new MoodAdapter.
     *
     * @param moods         List of Mood objects to display.
     * @param currentUserId The ID of the currently signed-in user.
     */
    public MoodAdapter(List<Mood> moods, String currentUserId) {
        this.moods = moods;
        this.currentUserId = currentUserId;
        firestoreHelper = new FirestoreHelper();
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

        // Truncate the mood description to the first 15 characters and add an ellipsis if needed.
        String fullDesc = currentMood.getMoodDescription();
        String truncated = fullDesc.length() > 15
                ? fullDesc.substring(0, 15) + "…"
                : fullDesc;
        holder.tvMoodDescription.setText("Why: " + truncated);

        holder.btnViewMore.setOnClickListener(v -> showDetailDialog(currentMood, v));


        // Show or hide the "edited" label.
        holder.tvEdited.setVisibility(currentMood.isEdited() ? View.VISIBLE : View.GONE);

        boolean isOwnMood = currentMood.getUserId() != null && currentMood.getUserId().equals(currentUserId);
        if (isOwnMood) {
            if (holder.btnEdit != null) {
                holder.btnEdit.setOnClickListener(v -> {
                    if (currentMood.getMoodId() != null) {
                        // Bundle the mood and navigate to the edit fragment.
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("mood", currentMood);
                        Navigation.findNavController(v)
                                .navigate(R.id.action_global_editMoodFragment, bundle);


                    }
                });
            }
            if (holder.btnDelete != null) {
                holder.btnDelete.setOnClickListener(v -> {
                    int adapterPosition = holder.getAdapterPosition();
                    if (adapterPosition == RecyclerView.NO_POSITION) return;
                    Mood moodToDelete = moods.get(adapterPosition);
                    new com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreDeleteMood(FirebaseFirestore.getInstance())
                            .deleteMood(moodToDelete.getUserId(), moodToDelete, new FirestoreHelper.FirestoreCallback() {
                                @Override
                                public void onSuccess(Object result) {
                                    Toast.makeText(v.getContext(), "Mood deleted successfully.", Toast.LENGTH_SHORT).show();
                                    int indexToRemove = -1;
                                    for (int i = 0; i < moods.size(); i++) {
                                        if (moods.get(i).getMoodId().equals(moodToDelete.getMoodId())) {
                                            indexToRemove = i;
                                            break;
                                        }
                                    }
                                    if (indexToRemove != -1) {
                                        moods.remove(indexToRemove);
                                        notifyItemRemoved(indexToRemove);
                                        notifyItemRangeChanged(indexToRemove, moods.size());
                                    }
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
                holder.btnComment.setOnClickListener(v ->
                        Toast.makeText(v.getContext(), "Comment button clicked.", Toast.LENGTH_SHORT).show());
            }
        }
    }

    private void showDetailDialog(Mood mood, View anchor) {
        AlertDialog.Builder b = new AlertDialog.Builder(anchor.getContext());
        View dialogView = LayoutInflater.from(anchor.getContext())
                .inflate(R.layout.dialog_mood_details, null);
        b.setView(dialogView);

        TextView tvMood = dialogView.findViewById(R.id.tvDialogMood);
        TextView tvDesc = dialogView.findViewById(R.id.tvDialogDescription);
        TextView tvTime = dialogView.findViewById(R.id.tvDialogTimestamp);
        TextView tvSocial = dialogView.findViewById(R.id.tvDialogSocial);
        ImageView iv = dialogView.findViewById(R.id.ivDialogImage);
        Button btnDelete = dialogView.findViewById(R.id.btnDialogDelete);
        Button btnEdit = dialogView.findViewById(R.id.btnDialogEdit);


        //this adds the little labels and formats neater
        tvMood.setText(mood.getMood());
        tvDesc.setText("Why: " + mood.getMoodDescription());

        Date date = mood.getTimestamp().toDate();
        String formatted = new SimpleDateFormat("MMM dd hh:mm a", Locale.getDefault()).format(date);
        tvTime.setText("Date: " + formatted);

        tvSocial.setText("Situation: " + mood.getSocialSituation());


        if (mood.getImageBase64() != null) {
            byte[] data = Base64.decode(mood.getImageBase64(), Base64.DEFAULT);
            iv.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
        } else iv.setVisibility(View.GONE);

        AlertDialog dlg = b.create();
        btnDelete.setOnClickListener(v -> {
            // Firestore delete
            new FirestoreDeleteMood(FirebaseFirestore.getInstance())
                    .deleteMood(mood.getUserId(), mood, new FirestoreHelper.FirestoreCallback() {
                        @Override
                        public void onSuccess(Object result) {
                            // Remove from adapter and refresh
                            int pos = moods.indexOf(mood);
                            if (pos != -1) {
                                moods.remove(pos);
                                notifyItemRemoved(pos);
                            }
                            Toast.makeText(v.getContext(), "Mood deleted successfully.", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(v.getContext(), "Error deleting mood: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            dlg.dismiss();
        });

        btnEdit.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putSerializable("mood", mood);
            Navigation.findNavController(anchor).navigate(R.id.editMoodFragment, args);
            dlg.dismiss();
        });
        dlg.show();
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
        TextView tvMood;
        TextView tvEdited;
        TextView tvMoodDescription;
        TextView tvTimestamp;
        TextView tvSocialSituation;
        ShapeableImageView ivProfile;
        ImageView ivMoodImage;
        Button btnEdit;
        Button btnComment;
        Button btnDelete;
        Button btnViewMore;



        public MoodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMood = itemView.findViewById(R.id.tvMood);
            tvEdited = itemView.findViewById(R.id.tvEdited);
            tvMoodDescription = itemView.findViewById(R.id.tvMoodDescription);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvSocialSituation = itemView.findViewById(R.id.tvSocialSituation);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            ivMoodImage = itemView.findViewById(R.id.ivMoodCardImgIfExists);
            //btnEdit = itemView.findViewById(R.id.btnEdit);
            //btnDelete = itemView.findViewById(R.id.btnDelete);
            btnComment = itemView.findViewById(R.id.btnComment);
            btnViewMore = itemView.findViewById(R.id.btnViewMore);

        }

        void bind(Mood mood) {
            tvMood.setText(mood.getMood());
            tvMoodDescription.setText("Description: " + mood.getMoodDescription());

            // NEW FORMAT -- date to only show month, day, and time (e.g. “Mar 22 09:00 AM”)
            Timestamp ts = mood.getTimestamp();
            if (ts != null) {
                Date date = ts.toDate();
                String formatted = new SimpleDateFormat("MMM dd hh:mm a", Locale.getDefault()).format(date);
                tvTimestamp.setText("Date: " + formatted);
            } else {
                tvTimestamp.setText("Date: N/A");
            }

            tvSocialSituation.setText("Situation: " + mood.getSocialSituation());

            // Reset profile image to fallback first.
            ivProfile.setImageResource(R.drawable.default_user_icon);

            // Load profile picture from Firestore user data.
            new FirestoreHelper().getUser(mood.getUserId(), new FirestoreHelper.FirestoreCallback() {
                @Override
                public void onSuccess(Object result) {
                    if (result instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> userData = (Map<String, Object>) result;
                        String username = (String) userData.get("username");
                        // Optionally update tvUsername if present in your layout.
                        String profilePicBase64 = (String) userData.get("profilePicture");
                        if (profilePicBase64 != null && !profilePicBase64.isEmpty()) {
                            try {
                                byte[] decodedBytes = Base64.decode(profilePicBase64, Base64.DEFAULT);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                                ivProfile.post(() -> ivProfile.setImageBitmap(bitmap));
                            } catch (Exception e) {
                                ivProfile.post(() -> ivProfile.setImageResource(R.drawable.default_user_icon));
                            }
                        } else {
                            ivProfile.post(() -> ivProfile.setImageResource(R.drawable.default_user_icon));
                        }
                    } else {
                        ivProfile.post(() -> ivProfile.setImageResource(R.drawable.default_user_icon));
                    }
                }
                @Override
                public void onFailure(Exception e) {
                    ivProfile.post(() -> ivProfile.setImageResource(R.drawable.default_user_icon));
                }
            });

            // Display mood image if available.
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
