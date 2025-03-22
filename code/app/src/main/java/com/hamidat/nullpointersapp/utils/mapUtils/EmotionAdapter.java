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

public class EmotionAdapter extends RecyclerView.Adapter<EmotionAdapter.ViewHolder> {

    private final List<String> emotions;
    private final boolean[] checkboxStates;
    private final Switch allSwitch;

    public EmotionAdapter(List<String> emotions, Switch allSwitch) {
        this.emotions = emotions;
        this.checkboxStates = new boolean[emotions.size()];
        this.allSwitch = allSwitch;

        // Initialize all checkboxes to checked
        for (int i = 0; i < checkboxStates.length; i++) {
            checkboxStates[i] = true;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mood_item, parent, false);
        return new ViewHolder(view);
    }

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

    @Override
    public int getItemCount() {
        return emotions.size();
    }

    public void updateCheckboxesState(boolean isChecked) {
        for (int i = 0; i < checkboxStates.length; i++) {
            checkboxStates[i] = isChecked;
        }
        notifyDataSetChanged();
    }

    public void clearSelections() {
        for (int i = 0; i < checkboxStates.length; i++) {
            checkboxStates[i] = false;
        }
        notifyDataSetChanged();
    }

    public Set<String> getSelectedEmotions() {
        Set<String> selectedEmotions = new HashSet<>();
        for (int i = 0; i < emotions.size(); i++) {
            if (checkboxStates[i]) {
                selectedEmotions.add(emotions.get(i));
            }
        }
        return selectedEmotions;
    }

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

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkbox;

        ViewHolder(View itemView) {
            super(itemView);
            checkbox = itemView.findViewById(R.id.checkbox);
        }
    }
}