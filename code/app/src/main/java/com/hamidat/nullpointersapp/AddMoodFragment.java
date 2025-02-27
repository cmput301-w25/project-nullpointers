package com.hamidat.nullpointersapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class AddMoodFragment extends DialogFragment {
    private final Mood mood;
    private AddMoodDialogListener listener;
    private int moodIndex = -1;

    private EditText moodNameInput;
    private EditText dateInput;
    private EditText descriptionInput;

    public AddMoodFragment(Mood mood, int moodIndex) {
        this.mood = mood;
        this.moodIndex = moodIndex;
    }

    public AddMoodFragment() {
        this.mood = null;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AddMoodDialogListener) {
            listener = (AddMoodDialogListener) context;
        } else {
            throw new RuntimeException(context + " must implement AddMoodDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_add_mood, null);

        moodNameInput = view.findViewById(R.id.mood_name);
        dateInput = view.findViewById(R.id.mood_date);
        descriptionInput = view.findViewById(R.id.mood_description);





        if (mood != null) {
            moodNameInput.setText(mood.getMoodName());
            dateInput.setText(mood.getDate());
            descriptionInput.setText(mood.getDescription());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(view)
                .setTitle(mood == null ? "Add Mood" : "Edit Mood")
                .setNegativeButton("Cancel", null)
                .setPositiveButton(mood == null ? "Add" : "Update", null)
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String moodName = moodNameInput.getText().toString().trim();
                String date = dateInput.getText().toString().trim();
                String description = descriptionInput.getText().toString().trim();

                if (moodName.isEmpty()) {
                    showValidationError("Mood name is required");
                    return;
                }
                if (date.isEmpty()) {
                    showValidationError("Date is required");
                    return;
                }
                if (description.isEmpty()) {
                    showValidationError("Description is required");
                    return;
                }

                Mood updatedMood = new Mood(moodName, date, description);
                listener.addMood(updatedMood, moodIndex);
                dialog.dismiss();
            });
        }
    }

    private void showValidationError(String message) {
        new AlertDialog.Builder(getContext())
                .setTitle("Invalid Input")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .create()
                .show();
    }

    public interface AddMoodDialogListener {
        void addMood(Mood mood, int index);
    }
}
