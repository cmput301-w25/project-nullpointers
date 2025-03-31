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

/**
 * RecyclerView adapter for displaying mood entries in the home feed.
 */
public class MoodAdapter extends RecyclerView.Adapter<MoodAdapter.MoodViewHolder> {

    private final List<Mood> moodItems;
    private FirestoreHelper firestoreHelper;
    private Context context;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    /**
     * Constructs a new MoodAdapter with the given list of mood items.
     *
     * @param moodItems The list of mood entries to display.
     */
    public MoodAdapter(List<Mood> moodItems) {
        this.moodItems = moodItems;
    }

    /**
     * Creates a new ViewHolder for a mood item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new MoodViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public MoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        // Inflate a layout for mood events. Ensure that item_mood.xml has at least two TextViews: one with id tvMoodTitle and one with id tvUsername.
        View view = LayoutInflater.from(context).inflate(R.layout.item_mood, parent, false);
        firestoreHelper = new FirestoreHelper();
        return new MoodViewHolder(view);
    }

    /**
     * Binds the mood data to the ViewHolder at the specified position.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
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
            /**
             * Called when the user data is successfully retrieved from Firestore.
             * Sets the username in the TextView.
             *
             * @param result The user data retrieved from Firestore.
             */
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

            /**
             * Called when the user data retrieval fails.
             * Sets the TextView to indicate that the username is unavailable.
             *
             * @param e The exception that occurred during the failure.
             */
            @Override
            public void onFailure(Exception e) {
                holder.tvUsername.post(() -> holder.tvUsername.setText("Username: Unavailable"));
            }
        });
    }

    /**
     * Returns the total number of mood items in the data set held by the adapter.
     *
     * @return The size of the mood items list.
     */
    @Override
    public int getItemCount() {
        return moodItems.size();
    }

    /**
     * ViewHolder for a mood item.
     */
    static class MoodViewHolder extends RecyclerView.ViewHolder {
        TextView tvMoodTitle;
        TextView tvUsername;
        TextView tvDate;

        /**
         * Constructs a new MoodViewHolder.
         *
         * @param itemView The View representing a mood item.
         */
        public MoodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMoodTitle = itemView.findViewById(R.id.tvMoodTitle);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}