package com.hamidat.nullpointersapp.utils.homeFeedUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;
import com.google.android.material.imageview.ShapeableImageView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MoodAdapter extends RecyclerView.Adapter<MoodAdapter.MoodViewHolder> {

    private final List<Mood> moodItems;
    private FirestoreHelper firestoreHelper;
    private Context context;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public MoodAdapter(List<Mood> moodItems) {
        this.moodItems = moodItems;
    }

    @NonNull
    @Override
    public MoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_mood_card, parent, false);
        firestoreHelper = new FirestoreHelper();
        return new MoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoodViewHolder holder, int position) {
        Mood mood = moodItems.get(position);
        holder.tvMoodTitle.setText("Mood: " + mood.getMood());
        if (mood.getTimestamp() != null) {
            holder.tvDate.setText(dateFormat.format(mood.getTimestamp().toDate()));
        } else {
            holder.tvDate.setText("Unknown Date");
        }
        holder.tvUsername.setText("Username: Loading...");
        firestoreHelper.getUser(mood.getUserId(), new FirestoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                if (result instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> userData = (Map<String, Object>) result;
                    String username = (String) userData.get("username");
                    holder.tvUsername.post(() ->
                            holder.tvUsername.setText("Username: " + (username != null ? username : "Unknown"))
                    );
                    String profilePicBase64 = (String) userData.get("profilePicture");
                    if (profilePicBase64 != null && !profilePicBase64.isEmpty()) {
                        byte[] decodedBytes = Base64.decode(profilePicBase64, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                        holder.ivProfile.post(() -> {
                            holder.ivProfile.setImageBitmap(bitmap);
                            // Clear any tint that might override the loaded image
                            holder.ivProfile.clearColorFilter();
                        });
                    } else {
                        holder.ivProfile.post(() -> {
                            holder.ivProfile.setImageResource(R.drawable.default_user_icon);
                            holder.ivProfile.clearColorFilter();
                        });
                    }
                } else {
                    holder.tvUsername.post(() -> holder.tvUsername.setText("Username: Unknown"));
                    holder.ivProfile.post(() -> {
                        holder.ivProfile.setImageResource(R.drawable.default_user_icon);
                        holder.ivProfile.clearColorFilter();
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                holder.tvUsername.post(() -> holder.tvUsername.setText("Username: Unavailable"));
                holder.ivProfile.post(() -> {
                    holder.ivProfile.setImageResource(R.drawable.default_user_icon);
                    holder.ivProfile.clearColorFilter();
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return moodItems.size();
    }

    static class MoodViewHolder extends RecyclerView.ViewHolder {
        TextView tvMoodTitle;
        TextView tvUsername;
        TextView tvDate;
        ShapeableImageView ivProfile;

        public MoodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMoodTitle = itemView.findViewById(R.id.tvMoodTitle);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvDate = itemView.findViewById(R.id.tvDate);
            ivProfile = itemView.findViewById(R.id.ivProfile);
        }
    }
}
