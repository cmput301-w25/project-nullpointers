package com.hamidat.nullpointersapp.utils.mapUtils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hamidat.nullpointersapp.R;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EmotionAdapter extends RecyclerView.Adapter<EmotionAdapter.ViewHolder> {
    private final List<String> emotions;
    private final Set<String> selectedEmotions = new HashSet<>();
    private final Switch allSwitch;
    /**
     * Constructs the adapter.
     *
     * @param emotions  List of emotion strings to display
     * @param allSwitch "Select All" switch reference
     */
    public EmotionAdapter(List<String> emotions, Switch allSwitch) {
        this.emotions = emotions;
        this.allSwitch = allSwitch;
    }
    /**
     * Creates view holders for RecyclerView.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emotion, parent, false);
        return new ViewHolder(view);
    }
    /**
     * Binds emotion data to list items.
     *
     * @param holder   ViewHolder instance
     * @param position Item position in list
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String emotion = emotions.get(position);
        holder.emotionText.setText(emotion);

        holder.emotionCheck.setChecked(selectedEmotions.contains(emotion));
        holder.emotionCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedEmotions.add(emotion);
                allSwitch.setChecked(false);
            } else {
                selectedEmotions.remove(emotion);
            }
            updateAllSwitchState();
        });
    }
    /**
     * Clears all emotion selections.
     */
    public void clearSelections() {
        selectedEmotions.clear();
        notifyDataSetChanged();
        updateAllSwitchState();
    }
    /**
     * Updates checkbox states based on "Select All" status.
     *
     * @param enabled True to select all emotions, false to deselect
     */
    public void updateCheckboxesState(boolean enabled) {
        if (enabled) {
            // Add all emotions only if not already selected
            if (selectedEmotions.size() != emotions.size()) {
                selectedEmotions.clear();
                selectedEmotions.addAll(emotions);
            }
        } else {
            // Clear selections only if not already empty
            if (!selectedEmotions.isEmpty()) {
                selectedEmotions.clear();
            }
        }
        notifyDataSetChanged();
        updateAllSwitchState();
    }
    /**
     * Updates "Select All" switch state based on selections.
     */
    private void updateAllSwitchState() {
        allSwitch.setChecked(selectedEmotions.size() == emotions.size());
    }
    /**
     * Gets currently selected emotions.
     * @return Set of selected emotion strings
     */
    public Set<String> getSelectedEmotions() {
        return new HashSet<>(selectedEmotions);
    }

    @Override
    public int getItemCount() {
        return emotions.size();
    }
    /**
     * Returns total number of emotions.
     * @return Count of emotion items
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView emotionText;
        CheckBox emotionCheck;

        ViewHolder(View view) {
            super(view);
            emotionText = view.findViewById(R.id.emotion_text);
            emotionCheck = view.findViewById(R.id.emotion_check);
        }
    }
}