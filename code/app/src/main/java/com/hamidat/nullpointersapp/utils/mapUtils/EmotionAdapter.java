/**
 * EmotionAdapter.java
 *
 * RecyclerView adapter for displaying a list of emotion checkboxes used for filtering mood data.
 * Integrates with a Switch to toggle all emotions on or off, and provides methods to access selected states.
 *
 * Outstanding Issues: None
 */

package com.hamidat.nullpointersapp.utils.mapUtils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hamidat.nullpointersapp.R;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * RecyclerView adapter for displaying a list of emotion checkboxes used for filtering mood data.
 */
public class EmotionAdapter extends RecyclerView.Adapter<EmotionAdapter.ViewHolder> {

    private final List<String> emotions;
    private final boolean[] checkboxStates;
    private final Switch allSwitch;

    /**
     * Constructs a new EmotionAdapter with the given list of emotions and a Switch to toggle all emotions.
     *
     * @param emotions The list of emotions to display as checkboxes.
     * @param allSwitch The Switch to toggle all emotions on or off.
     */
    public EmotionAdapter(List<String> emotions, Switch allSwitch) {
        this.emotions = emotions;
        this.checkboxStates = new boolean[emotions.size()];
        this.allSwitch = allSwitch;

        // Initialize all checkboxes to checked
        for (int i = 0; i < checkboxStates.length; i++) {
            checkboxStates[i] = true;
        }
    }

    /**
     * Creates a new ViewHolder for an emotion item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mood_item, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds the emotion data to the ViewHolder at the specified position.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String emotion = emotions.get(position);
        holder.checkbox.setText(emotion);
        holder.checkbox.setChecked(checkboxStates[position]);

        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            checkboxStates[position] = isChecked;
            updateAllSwitchState();
        });
    }

    /**
     * Returns the total number of emotion items in the data set held by the adapter.
     *
     * @return The size of the emotions list.
     */
    @Override
    public int getItemCount() {
        return emotions.size();
    }

    /**
     * Updates the state of all checkboxes to the given boolean value.
     *
     * @param isChecked The boolean value to set all checkboxes to.
     */
    public void updateCheckboxesState(boolean isChecked) {
        for (int i = 0; i < checkboxStates.length; i++) {
            checkboxStates[i] = isChecked;
        }
        notifyDataSetChanged();
    }

    /**
     * Clears all selections by setting all checkboxes to unchecked.
     */
    public void clearSelections() {
        for (int i = 0; i < checkboxStates.length; i++) {
            checkboxStates[i] = false;
        }
        notifyDataSetChanged();
    }

    /**
     * Returns a Set of selected emotions based on the checkbox states.
     *
     * @return A Set of strings representing the selected emotions.
     */
    public Set<String> getSelectedEmotions() {
        Set<String> selectedEmotions = new HashSet<>();
        for (int i = 0; i < emotions.size(); i++) {
            if (checkboxStates[i]) {
                selectedEmotions.add(emotions.get(i));
            }
        }
        return selectedEmotions;
    }

    /**
     * Updates the state of the allSwitch based on the current checkbox states.
     */
    private void updateAllSwitchState() {
        boolean allChecked = true;
        boolean anyChecked = false;

        for (boolean state : checkboxStates) {
            if (!state) {
                allChecked = false;
            } else {
                anyChecked = true;
            }
        }

        if (allChecked) {
            allSwitch.setChecked(true);
        } else if (!anyChecked) {
            allSwitch.setChecked(false);
        }
    }

    /**
     * ViewHolder for an emotion item.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkbox;

        /**
         * Constructs a new ViewHolder.
         *
         * @param itemView The View representing an emotion item.
         */
        ViewHolder(View itemView) {
            super(itemView);
            checkbox = itemView.findViewById(R.id.checkbox);
        }
    }
}