/**
 * MoodAdapter.java
 *
 * RecyclerView adapter for displaying mood entries in the home feed.
 * Displays mood title, associated username, and the date of the mood event.
 *
 * Outstanding Issues: None
 */

package com.hamidat.nullpointersapp.utils.homeFeedUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;
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
        // Inflate a layout for mood events. Ensure that item_mood.xml has at least two TextViews: one with id tvMoodTitle and one with id tvUsername.
        View view = LayoutInflater.from(context).inflate(R.layout.item_mood, parent, false);
        firestoreHelper = new FirestoreHelper();
        return new MoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoodViewHolder holder, int position) {
        Mood mood = moodItems.get(position);
        // Display the mood title and date (if available).
        holder.tvMoodTitle.setText("Mood: " + mood.getMood());
        if (mood.getTimestamp() != null) {
            holder.tvDate.setText(dateFormat.format(mood.getTimestamp().toDate()));
        } else {
            holder.tvDate.setText("Unknown Date");
        }
        // For testing, fetch and display the username associated with this mood event.
        holder.tvUsername.setText("Username: Loading...");
        firestoreHelper.getUser(mood.getUserId(), new FirestoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                if (result instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> userData = (Map<String, Object>) result;
                    String username = (String) userData.get("username");
                    holder.tvUsername.post(() ->
                            holder.tvUsername.setText("Username: " + (username != null ? username : "Unknown")));
                } else {
                    holder.tvUsername.post(() -> holder.tvUsername.setText("Username: Unknown"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                holder.tvUsername.post(() -> holder.tvUsername.setText("Username: Unavailable"));
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

        public MoodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMoodTitle = itemView.findViewById(R.id.tvMoodTitle);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
