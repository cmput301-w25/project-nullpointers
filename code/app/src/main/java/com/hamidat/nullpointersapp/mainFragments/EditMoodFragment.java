package com.hamidat.nullpointersapp.mainFragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hamidat.nullpointersapp.MainActivity;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;

public class EditMoodFragment extends Fragment {

    private Mood moodToEdit; // The mood object we’re editing.
    private EditText etReason;
    private RadioGroup rgMood, rgSocial;
    private ImageView ivPhotoPreview;
    private Uri imageUri;
    private String base64Image;
    private double latitude, longitude;
    private boolean attachLocation = true;

    private FirestoreHelper firestoreHelper;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_mood, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1) Retrieve the mood object from arguments
        //    This can be done with safe args or direct bundles
        if (getArguments() != null && getArguments().containsKey("mood")) {
            moodToEdit = (Mood) getArguments().getSerializable("mood");
        }

        etReason = view.findViewById(R.id.Reason);
        rgMood = view.findViewById(R.id.rgMood);
        rgSocial = view.findViewById(R.id.rgSocialSituation);
        ivPhotoPreview = view.findViewById(R.id.ivPhotoPreview);
        Button btnAttachPhoto = view.findViewById(R.id.AttachPhoto);
        Button btnSaveEntry = view.findViewById(R.id.btnSaveEntry);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnAttachLocation = view.findViewById(R.id.btnAttachLocation);

        // 2) Pre-populate the fields with the existing mood data
        if (moodToEdit != null) {
            etReason.setText(moodToEdit.getMoodDescription());
            // If you stored location, set latitude/longitude
            latitude = moodToEdit.getLatitude();
            longitude = moodToEdit.getLongitude();
            // Pre-select the mood radio
            // Example: if mood is "Happy", check the happy radio.
            // (Similarly for social situation.)
            // ...
            // If there's an image:
            if (moodToEdit.getImageBase64() != null) {
                // convert base64 to bitmap, set ivPhotoPreview visible
            }
        }

        // 3) Set up your FirestoreHelper instance, just like in AddMoodFragment
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            this.firestoreHelper = mainActivity.getFirestoreHelper();
            this.currentUserId = mainActivity.getCurrentUserId();
        }

        // 4) Hook up button clicks
        btnAttachPhoto.setOnClickListener(v -> openImagePicker());
        btnAttachLocation.setOnClickListener(v -> {
            // toggle attachLocation
        });
        btnSaveEntry.setOnClickListener(v -> {
            // Gather updated values from radio groups, reason, possibly updated photo, location, etc.
            String reasonText = etReason.getText().toString().trim();
            // validations as needed
            // moodToEdit.setMoodDescription(reasonText);
            // moodToEdit.setMood(selectedMoodString);
            // moodToEdit.setSocialSituation(selectedSocialString);
            // moodToEdit.setLatitude(... attachLocation ? latitude : 0.0, etc.)

            // If new photo was attached, set the new base64
            // else keep the old base64 if user didn't change it

            // 5) Firestore update call
            firestoreHelper.updateMood(moodToEdit, new FirestoreHelper.FirestoreCallback() {
                @Override
                public void onSuccess(Object result) {
                    Toast.makeText(getContext(), "Mood updated!", Toast.LENGTH_SHORT).show();
                    // Navigate back to Home (or popBackStack)
                }
                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
        btnCancel.setOnClickListener(v -> requireActivity().onBackPressed());
    }

    // openImagePicker(), onActivityResult(), etc. same as AddMoodFragment,
    // but store base64 in moodToEdit or local variable.
}
