package com.hamidat.nullpointersapp.mainFragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
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
import androidx.navigation.Navigation;
import androidx.fragment.app.Fragment;
import com.hamidat.nullpointersapp.MainActivity;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;

import java.io.InputStream;

/**
 * Fragment to add a mood event with an optional image.
 */
public class AddMoodFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView ivPhotoPreview;
    private Uri imageUri;
    private String base64Image;
    private FirestoreHelper firestoreHelper;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_mood, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind UI elements
        ivPhotoPreview = view.findViewById(R.id.ivPhotoPreview);
        EditText etReason = view.findViewById(R.id.Reason);
        RadioGroup rgSocialSituation = view.findViewById(R.id.rgSocialSituation);
        Button btnAttachPhoto = view.findViewById(R.id.AttachPhoto);
        Button btnSaveEntry = view.findViewById(R.id.btnSaveEntry);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        // Get Firestore helper from MainActivity
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            this.firestoreHelper = mainActivity.getFirestoreHelper();
            this.currentUserId = mainActivity.getCurrentUserId();

            if (firestoreHelper == null) {
                Toast.makeText(getActivity(), "Error: Firestore is unavailable!", Toast.LENGTH_LONG).show();
                return;
            }

            btnAttachPhoto.setOnClickListener(v -> openImagePicker());

            btnSaveEntry.setOnClickListener(v -> {
                String reasonText = etReason.getText().toString().trim();
                if (reasonText.isEmpty()) {
                    Toast.makeText(getActivity(), "Please enter a reason for your mood.", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirestoreHelper.FirestoreCallback moodCallback = new FirestoreHelper.FirestoreCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        if (isAdded() && getView() != null) { // Check if fragment is still attached
                            Toast.makeText(requireContext(), "Mood saved successfully!", Toast.LENGTH_SHORT).show();

                            // Safely navigate back to ProfileFragment
                            Navigation.findNavController(getView()).navigate(R.id.profileNavGraphFragment);
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        if (isAdded()) { // Ensure fragment is attached before showing error
                            Toast.makeText(requireContext(), "Failed to save mood: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                if
                (base64Image != null) {
                    // Save mood with image
                    Mood moodWithImage = new Mood("User Mood", reasonText, base64Image);
                    firestoreHelper.addMoodWithPhoto(currentUserId, moodWithImage, moodCallback);
                } else {
                    // Save mood without image
                    Mood moodWithoutImage = new Mood("User Mood", reasonText);
                    firestoreHelper.addMood(currentUserId, moodWithoutImage, moodCallback);
                }

            });

            btnCancel.setOnClickListener(v -> {
                Toast.makeText(getActivity(), "Entry cancelled", Toast.LENGTH_SHORT).show();
                getActivity().onBackPressed(); // Go back
            });
        }
    }

    /**
     * Opens the gallery to pick an image.
     */
    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    /**
     * Handles the result of the image picker.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            ivPhotoPreview.setImageURI(imageUri);
            ivPhotoPreview.setVisibility(View.VISIBLE);
            encodeImageToBase64(imageUri);
        }
    }

    /**
     * Converts the selected image to a Base64 string.
     */
    private void encodeImageToBase64(Uri imageUri) {
        try {
            InputStream inputStream = getActivity().getContentResolver().openInputStream(imageUri);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            inputStream.close();

            // Ensure image is under 64KB
            if (bytes.length > 65536) {
                Toast.makeText(getActivity(), "Image too large! Must be under 64KB.", Toast.LENGTH_SHORT).show();
                base64Image = null;
                return;
            }

            base64Image = Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Failed to process image", Toast.LENGTH_SHORT).show();
        }
    }
}
