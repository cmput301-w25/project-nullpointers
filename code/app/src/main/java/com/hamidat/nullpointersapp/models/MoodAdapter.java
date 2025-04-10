/**
 * MoodAdapter.java
 * RecyclerView adapter for displaying a list of Mood objects with support for user interaction,
 * liking, commenting, editing, and deleting moods.
 *
 * Outstanding Issues: None
 */

package com.hamidat.nullpointersapp.models;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.imageview.ShapeableImageView;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreDeleteMood;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;
import com.hamidat.nullpointersapp.utils.homeFeedUtils.CommentsBottomSheetFragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
    private FirestoreHelper firestoreHelper;
    private OnProfileClickListener profileClickListener;
    private final AppCompatActivity activity; // Store activity reference

    /**
     * Interface for handling profile click events.
     */
    public interface OnProfileClickListener {
        void onProfileClick(String userId);
    }


    /**
     * Constructs a new MoodAdapter.
     *
     * @param moods         List of Mood objects to display.
     * @param currentUserId The ID of the currently signed-in user.
     * @param activity      The activity context.
     */
    public MoodAdapter(List<Mood> moods, String currentUserId, AppCompatActivity activity) {
        this.moods = moods;
        this.currentUserId = currentUserId;
        this.activity = activity;
        firestoreHelper = new FirestoreHelper();
    }


    /**
     * Determines the view type based on whether the mood belongs to the current user.
     *
     * @param position The position of the item within the adapter's data set.
     * @return The view type of the item at position.
     */
    @Override
    public int getItemViewType(int position) {
        Mood mood = moods.get(position);
        return (mood.getUserId() != null && mood.getUserId().equals(currentUserId)) ?
                R.layout.item_mood_card :
                R.layout.item_mood_card_public;
    }

    /**
     * Creates a new ViewHolder for the item view.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new MoodViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public MoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(viewType, parent, false);
        return new MoodViewHolder(itemView);
    }

    /**
     * Binds data to the ViewHolder, including handling partial updates through payloads.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     * @param payloads A list of payloads associated with the change.
     */
    @Override
    public void onBindViewHolder(@NonNull MoodViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) {
            Object payload = payloads.get(0);
            Mood mood = moods.get(position);

            if ("likeOnly".equals(payload)) {
                holder.tvLikeCount.setText(String.valueOf(mood.getLikeCount()));
                holder.btnLike.setImageResource(mood.isLikedBy(currentUserId)
                        ? R.drawable.ic_heart_filled
                        : R.drawable.ic_heart_outline);
                return; // only update likes
            } else if ("commentOnly".equals(payload)) {
                holder.btnComment.setText("Comments (" + mood.getCommentCount() + ")");
                return; // only update comments
            }
        }

        // Fallback to full bind if no payload or unknown
        super.onBindViewHolder(holder, position, payloads);
    }


    /**
     * Binds data to the ViewHolder, including setting up click listeners and displaying mood details.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull MoodViewHolder holder, int position) {
        Mood currentMood = moods.get(position);
        holder.bind(currentMood);

        boolean isLiked = currentMood.isLikedBy(currentUserId);
        holder.btnLike.setImageResource(isLiked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
        holder.tvLikeCount.setText(String.valueOf(currentMood.getLikeCount()));
        holder.btnComment.setText("Comments (" + currentMood.getCommentCount() + ")");

        // Toggle like logic
        holder.btnLike.setOnClickListener(v -> {
            boolean currentlyLiked = currentMood.isLikedBy(currentUserId);
            if (currentlyLiked) {
                currentMood.getLikedByUserIds().remove(currentUserId);
                currentMood.setLikeCount(currentMood.getLikeCount() - 1);
            } else {
                currentMood.getLikedByUserIds().add(currentUserId);
                currentMood.setLikeCount(currentMood.getLikeCount() + 1);
            }
            notifyItemChanged(holder.getAdapterPosition(), "likeOnly");

            FirebaseFirestore.getInstance()
                    .collection("moods")
                    .document(currentMood.getMoodId())
                    .update("likedByUserIds", currentMood.getLikedByUserIds(),
                            "likeCount", currentMood.getLikeCount());
        });

        // Truncate the mood description to the first 15 characters and add an ellipsis only if there is an image. If not, don't truncate that short
        if (currentMood.getImageBase64() != null) {
            String fullDesc = currentMood.getMoodDescription();
            String truncated = fullDesc.length() > 15
                    ? fullDesc.substring(0, 15) + "…"
                    : fullDesc;
            holder.tvMoodDescription.setText(truncated);
        } else {
            String fullDesc = currentMood.getMoodDescription();
            String truncated = fullDesc.length() > 39
                    ? fullDesc.substring(0, 39) + "…"
                    : fullDesc;
            holder.tvMoodDescription.setText(truncated);
        }

        holder.btnViewMore.setOnClickListener(v -> showDetailDialog(currentMood, v));
        holder.itemView.setOnClickListener(v -> showDetailDialog(currentMood, v));

        // Show or hide the "edited" label.
        holder.tvEdited.setVisibility(currentMood.isEdited() ? View.VISIBLE : View.GONE);
        holder.tvPrivate.setVisibility(currentMood.isPrivate() ? View.VISIBLE : View.GONE);
        holder.ivPrivate.setVisibility(currentMood.isPrivate() ? View.VISIBLE : View.GONE);

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
                                /**
                                 * Called when the mood is successfully deleted from Firestore.
                                 * Updates the RecyclerView to reflect the deletion.
                                 *
                                 * @param result The result of the deletion operation.
                                 */
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

                                /**
                                 * Called when the mood deletion from Firestore fails.
                                 * Displays an error message.
                                 *
                                 * @param e The exception that occurred during the deletion.
                                 */
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
                    // Open the CommentsBottomSheetFragment using the stored activity.
                    CommentsBottomSheetFragment fragment = CommentsBottomSheetFragment.newInstance(
                            currentMood.getMoodId(), currentUserId);
                    fragment.show(activity.getSupportFragmentManager(), "CommentsBottomSheetFragment");
                });
            }
        }
    }

    /**
     * Displays a dialog with detailed information about the mood.
     *
     * @param mood   The Mood object to display.
     * @param anchor The view that the dialog is anchored to.
     */
    private void showDetailDialog(Mood mood, View anchor) {
        AlertDialog.Builder b = new AlertDialog.Builder(anchor.getContext());
        View dialogView = LayoutInflater.from(anchor.getContext())
                .inflate(R.layout.dialog_mood_details, null);
        b.setView(dialogView);

        TextView tvUsername = dialogView.findViewById(R.id.tvDialogUsername);
        tvUsername.setText("by @loading...");

        TextView tvMood = dialogView.findViewById(R.id.tvDialogMood);
        TextView tvDesc = dialogView.findViewById(R.id.tvDialogDescription);
        TextView tvTime = dialogView.findViewById(R.id.tvDialogTimestamp);
        TextView tvSocial = dialogView.findViewById(R.id.tvDialogSocial);
        TextView tvLocation = dialogView.findViewById(R.id.tvDialogLocation);

        ImageView iv = dialogView.findViewById(R.id.ivDialogImage);
        Button btnDelete = dialogView.findViewById(R.id.btnDialogDelete);
        Button btnEdit = dialogView.findViewById(R.id.btnDialogEdit);
        boolean isOwnMood = mood.getUserId() != null && mood.getUserId().equals(currentUserId);


        //only show edit and delete if it is my own mood
        if (!isOwnMood) {
            btnDelete.setVisibility(View.GONE);
            btnEdit.setVisibility(View.GONE);
        }

        AlertDialog dlg = b.create();

        if (isOwnMood) {//if its my own
            btnDelete.setOnClickListener(v -> {
                new FirestoreDeleteMood(FirebaseFirestore.getInstance())
                        .deleteMood(mood.getUserId(), mood, new FirestoreHelper.FirestoreCallback() {
                            /**
                             * Called when the mood is successfully deleted from Firestore.
                             * Updates the RecyclerView and displays a success message.
                             *
                             * @param result The result of the deletion operation.
                             */
                            @Override public void onSuccess(Object result) {
                                int pos = moods.indexOf(mood);
                                if (pos != -1) {
                                    moods.remove(pos);
                                    notifyItemRemoved(pos);
                                }
                                Toast.makeText(v.getContext(), "Mood deleted successfully.", Toast.LENGTH_SHORT).show();
                            }

                            /**
                             * Called when the mood deletion from Firestore fails.
                             * Displays an error message.
                             *
                             * @param e The exception that occurred during the deletion.
                             */
                            @Override public void onFailure(Exception e) {
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
        }

        // showing the username
        firestoreHelper.getUser(mood.getUserId(), new FirestoreHelper.FirestoreCallback() {
            /**
             * Called when the user data is successfully retrieved from Firestore.
             * Sets the username in the dialog.
             *
             * @param result The user data retrieved from Firestore as an Object.
             */
            @Override
            public void onSuccess(Object result) {
                if (result instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> userData = (Map<String, Object>) result;
                    String username = (String) userData.get("username");
                    if (username != null) {
                        tvUsername.setText("@" + username);
                    } else {
                        tvUsername.setText("@unknown");
                    }
                }
            }

            /**
             * Called when the retrieval of user data from Firestore fails.
             * Sets the username to "@unknown" in the dialog.
             *
             * @param e The exception that occurred during the failure.
             */
            @Override
            public void onFailure(Exception e) {
                tvUsername.setText("@unknown");
            }
        });

        //showing the location - if applicable
        double lat = mood.getLatitude();
        double lng = mood.getLongitude();
        if (lat != 0 && lng != 0) {
            new Thread(() -> {
                try {
                    Geocoder geocoder = new Geocoder(anchor.getContext(), Locale.getDefault());
                    // Limit to 1 result.
                    java.util.List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                    String addressStr = "Unknown location";
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        addressStr = address.getAddressLine(0); // Full address line.
                    }
                    final String finalAddressStr = "Location: " + addressStr;
                    anchor.post(() -> tvLocation.setText(finalAddressStr));
                } catch (Exception e) {
                    anchor.post(() -> tvLocation.setText("Location: N/A"));
                }
            }).start();
        }else {
            tvLocation.setText("Location: N/A");
        }
        //this adds the little labels and formats neater
        String moodEmoji;
        switch (mood.getMood().toLowerCase(Locale.ROOT)) {
            case "happy":     moodEmoji = "😊  🟡 - Happy"; break;
            case "sad":       moodEmoji = "😢  🔵 - Sad"; break;
            case "angry":     moodEmoji = "😠  🔴 - Angry"; break;
            case "confused":  moodEmoji = "😕  ⚫ - Confused"; break;
            case "disgusted": moodEmoji = "🤢  🟠 - Disgusted"; break;
            case "afraid":    moodEmoji = "😱  🟣 - Afraid"; break;
            case "shameful":  moodEmoji = "😳  🟤 - Shameful"; break;
            case "surprised": moodEmoji = "😮  🟢 - Surprised"; break;
            default:          moodEmoji = "❓  ⚪"; break;
        }
        tvMood.setText(moodEmoji);

        tvDesc.setText("Why: " + mood.getMoodDescription());

        Date date = mood.getTimestamp().toDate();
        String formatted = new SimpleDateFormat("MMM dd hh:mm a", Locale.getDefault()).format(date);
        tvTime.setText("Date: " + formatted);

        tvSocial.setText("Situation: " + mood.getSocialSituation());


        if (mood.getImageBase64() != null) {
            byte[] data = Base64.decode(mood.getImageBase64(), Base64.DEFAULT);
            iv.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
        } else iv.setVisibility(View.GONE);

        btnDelete.setOnClickListener(v -> {
            // Firestore delete
            new FirestoreDeleteMood(FirebaseFirestore.getInstance())
                    .deleteMood(mood.getUserId(), mood, new FirestoreHelper.FirestoreCallback() {
                        /**
                         * Called when the mood is successfully deleted from Firestore.
                         * Removes the mood from the adapter and refreshes the RecyclerView.
                         *
                         * @param result The result of the deletion operation.
                         */
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

                        /**
                         * Called when the mood deletion from Firestore fails.
                         * Displays an error message.
                         *
                         * @param e The exception that occurred during the deletion.
                         */
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


    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The size of the data set.
     */
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
     * ViewHolder class that binds a Mood object to its corresponding views.
     */
    static class MoodViewHolder extends RecyclerView.ViewHolder {
        TextView tvMood;
        TextView tvEdited;
        TextView tvPrivate;
        TextView tvMoodDescription;
        TextView tvTimestamp;
        TextView tvSocialSituation;
        TextView tvLikeCount;
        ShapeableImageView ivProfile;
        ImageView ivMoodImage;
        Button btnEdit;
        Button btnComment;
        Button btnDelete;
        Button btnViewMore;
        private OnProfileClickListener profileClickListener;
        ImageView ivPrivate; // Add this line
        ImageButton btnLike;


        /**
         * Constructs a new MoodViewHolder.
         *
         * @param itemView The view to display the Mood object.
         */
        public MoodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMood = itemView.findViewById(R.id.tvMood);
            tvEdited = itemView.findViewById(R.id.tvEdited);
            tvPrivate = itemView.findViewById(R.id.tvPrivate);
            tvMoodDescription = itemView.findViewById(R.id.tvMoodDescription);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvSocialSituation = itemView.findViewById(R.id.tvSocialSituation);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            ivMoodImage = itemView.findViewById(R.id.ivMoodCardImgIfExists);
            btnComment = itemView.findViewById(R.id.btnComment);
            btnViewMore = itemView.findViewById(R.id.btnViewMore);
            btnLike = itemView.findViewById(R.id.btnLike);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            ivPrivate = itemView.findViewById(R.id.ivDialogPrivate);
        }

        /**
         * Binds a Mood object to the views in the ViewHolder.
         *
         * @param mood The Mood object to bind.
         */
        void bind(Mood mood) {

            //showing a emoji-color instead of the word on the item cards and dialog
            String moodEmoji;
            switch (mood.getMood().toLowerCase(Locale.ROOT)) {
                case "happy":     moodEmoji = "😊  🟡"; break;
                case "sad":       moodEmoji = "😢  🔵"; break;
                case "angry":     moodEmoji = "😠  🔴"; break;
                case "confused":  moodEmoji = "😕  ⚫"; break;
                case "disgusted": moodEmoji = "🤢  🟠"; break;
                case "afraid":    moodEmoji = "😱  🟣"; break;
                case "shameful":  moodEmoji = "😳  🟤"; break;
                case "surprised": moodEmoji = "😮  🟢"; break;
                default:          moodEmoji = "❓  ⚪"; break;
            }
            tvMood.setText(moodEmoji);



            ivPrivate.setVisibility(mood.isPrivate() ? View.VISIBLE : View.GONE);
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
                /**
                 * Called when the user data is successfully retrieved from Firestore.
                 * Sets the profile picture in the ImageView if available.
                 *
                 * @param result The user data retrieved from Firestore as an Object.
                 */
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

                /**
                 * Called when the retrieval of user data from Firestore fails.
                 * Sets the default user icon in the ImageView.
                 *
                 * @param e The exception that occurred during the failure.
                 */
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