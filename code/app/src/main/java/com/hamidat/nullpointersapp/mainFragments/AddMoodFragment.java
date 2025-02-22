package com.hamidat.nullpointersapp.mainFragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.utils.imageUtils.ImageAttachmentHandler;

import java.util.Calendar;

public class AddMoodFragment extends Fragment {
    private TextView tvDateValue, tvTimeValue;
    private EditText etReason;
    private RadioGroup rgSocialSituation;
    private ImageAttachmentHandler imageAttachmentHandler;
    private Calendar selectedDateTime = Calendar.getInstance();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_mood, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        // Cast requireActivity() to AppCompatActivity since ImageAttachmentHandler expects that type.
        imageAttachmentHandler = new ImageAttachmentHandler((androidx.appcompat.app.AppCompatActivity) requireActivity());

        initializeViews(view);
        setupDateTimePickers();
        setupPhotoAttachment(view);
        setupButtons(view);
    }

    private void initializeViews(View view) {
        tvDateValue = view.findViewById(R.id.tvDateValue);
        tvTimeValue = view.findViewById(R.id.tvTimeValue);
        etReason = view.findViewById(R.id.Reason);
        rgSocialSituation = view.findViewById(R.id.rgSocialSituation);
    }

    private void setupDateTimePickers() {
        tvDateValue.setOnClickListener(v -> showDatePicker());
        tvTimeValue.setOnClickListener(v -> showTimePicker());
        updateDateTimeDisplay();
    }

    private void showDatePicker() {
        DatePickerDialog datePicker = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            selectedDateTime.set(year, month, dayOfMonth);
            updateDateTimeDisplay();
        }, selectedDateTime.get(Calendar.YEAR), selectedDateTime.get(Calendar.MONTH), selectedDateTime.get(Calendar.DAY_OF_MONTH));

        datePicker.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePicker = new TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
            selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedDateTime.set(Calendar.MINUTE, minute);
            updateDateTimeDisplay();
        }, selectedDateTime.get(Calendar.HOUR_OF_DAY), selectedDateTime.get(Calendar.MINUTE), false);

        timePicker.show();
    }

    private void updateDateTimeDisplay() {
        tvDateValue.setText(String.format("%1$tB %1$te, %1$tY", selectedDateTime));
        tvTimeValue.setText(String.format("%1$tI:%1$tM %1$Tp", selectedDateTime));
    }

    private void setupPhotoAttachment(View view) {
        view.findViewById(R.id.AttachPhoto).setOnClickListener(v ->
                imageAttachmentHandler.openImagePicker()
        );
    }

    private void setupButtons(View view) {
        view.findViewById(R.id.btnSaveEntry).setOnClickListener(v -> saveEntry());
        view.findViewById(R.id.btnCancel).setOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void saveEntry() {
        String reason = etReason.getText().toString().trim();
        int selectedSituationId = rgSocialSituation.getCheckedRadioButtonId();

        if (reason.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a reason", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedSituationId == -1) {
            Toast.makeText(requireContext(), "Please select a social situation", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Implement actual save logic
        Toast.makeText(requireContext(), "Entry saved successfully", Toast.LENGTH_SHORT).show();
        requireActivity().onBackPressed();
    }

    public void handleImageActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        imageAttachmentHandler.handleActivityResult(requestCode, resultCode, data);
    }
}
