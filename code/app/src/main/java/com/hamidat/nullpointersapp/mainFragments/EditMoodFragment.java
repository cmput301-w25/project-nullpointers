package com.hamidat.nullpointersapp.mainFragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.hamidat.nullpointersapp.MainActivity;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;
import com.hamidat.nullpointersapp.utils.mapUtils.AppEventBus;

import java.io.InputStream;

/**
 * Fragment that allows editing an existing Mood object.
 * It closely mirrors AddMoodFragment but updates an existing Firestore document.
 */
public class EditMoodFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private Mood moodToEdit;
    private ImageView ivPhotoPreview;
    private EditText etReason;
    private RadioGroup rgMood;
    private RadioGroup rgSocialSituation;
    private Button btnAttachLocation;

    private Uri imageUri;
    private String base64Image;
    private FirestoreHelper firestoreHelper;
    private String currentUserId;
    private FusedLocationProviderClient fusedLocationClient;
    private double latitude;
    private double longitude;
    private boolean attachLocation = true;

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

        // 1) Get the Mood to edit from arguments
        if (getArguments() != null && getArguments().containsKey("mood")) {
            moodToEdit = (Mood) getArguments().getSerializable("mood");
        }
        if (moodToEdit == null || moodToEdit.getMoodId() == null) {
            Toast.makeText(getContext(), "Error: No Mood or missing moodId", Toast.LENGTH_SHORT).show();
            // Optionally navigate back
            return;
        }

        // Initialize UI components
        ivPhotoPreview = view.findViewById(R.id.ivPhotoPreview);
        etReason = view.findViewById(R.id.Reason);
        rgMood = view.findViewById(R.id.rgMood);
        rgSocialSituation = view.findViewById(R.id.rgSocialSituation);
        Button btnAttachPhoto = view.findViewById(R.id.AttachPhoto);
        Button btnSaveEntry = view.findViewById(R.id.btnSaveEntry);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        btnAttachLocation = view.findViewById(R.id.btnAttachLocation);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // 2) Populate fields with existing data
        etReason.setText(moodToEdit.getMoodDescription());
        latitude = moodToEdit.getLatitude();
        longitude = moodToEdit.getLongitude();
        base64Image = moodToEdit.getImageBase64();

        // Pre-select the mood radio
        // Example: if mood is "Happy", check that radio button. Adjust logic as needed:
        String moodStr = moodToEdit.getMood();
        if (moodStr != null) {
            if (moodStr.equalsIgnoreCase("Happy")) {
                rgMood.check(R.id.rbHappy);
            } else if (moodStr.equalsIgnoreCase("Sad")) {
                rgMood.check(R.id.rbSad);
            } else if (moodStr.equalsIgnoreCase("Angry")) {
                rgMood.check(R.id.rbAngry);
            } else if (moodStr.equalsIgnoreCase("Chill")) {
                rgMood.check(R.id.rbChill);
            }
        }

        // Pre-select the social situation
        String socialStr = moodToEdit.getSocialSituation();
        if (socialStr != null) {
            if (socialStr.equalsIgnoreCase("Alone")) {
                rgSocialSituation.check(R.id.rbAlone);
            } else if (socialStr.equalsIgnoreCase("One on One")) {
                rgSocialSituation.check(R.id.rbOneOnOne);
            } else if (socialStr.equalsIgnoreCase("Group")) {
                rgSocialSituation.check(R.id.rbGroup);
            } else if (socialStr.equalsIgnoreCase("Crowd")) {
                rgSocialSituation.check(R.id.rbCrowd);
            }
        }

        // If there's an existing image, show it
        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                ivPhotoPreview.setImageBitmap(bitmap);
                ivPhotoPreview.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                ivPhotoPreview.setVisibility(View.GONE);
            }
        } else {
            ivPhotoPreview.setVisibility(View.GONE);
        }

        // 3) Firestore helper from MainActivity
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            this.firestoreHelper = mainActivity.getFirestoreHelper();
            this.currentUserId = mainActivity.getCurrentUserId();

            if (firestoreHelper == null) {
                Toast.makeText(getActivity(), "Error: Firestore is unavailable!", Toast.LENGTH_LONG).show();
                return;
            }
        }

        // 4) Request location permissions if not granted
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastKnownLocation();
        }

        // 5) Button handlers
        btnAttachPhoto.setOnClickListener(v -> openImagePicker());
        btnAttachLocation.setOnClickListener(v -> {
            attachLocation = !attachLocation;
            btnAttachLocation.setAlpha(attachLocation ? 1.0f : 0.5f);
            Toast.makeText(getActivity(), attachLocation ? "Location Attached" : "Location Detached", Toast.LENGTH_SHORT).show();
        });
        btnSaveEntry.setOnClickListener(v -> saveEdits());
        btnCancel.setOnClickListener(v -> requireActivity().onBackPressed());
    }

    /**
     * Retrieve last known location if permission is granted.
     */
    private void getLastKnownLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
            });
        }
    }

    /**
     * Called when the user taps "Save Entry".
     */
    private void saveEdits() {
        // Gather updated data
        String reasonText = etReason.getText().toString().trim();
        int selectedMoodId = rgMood.getCheckedRadioButtonId();
        int selectedSocialId = rgSocialSituation.getCheckedRadioButtonId();

        if (reasonText.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter a reason", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedMoodId == -1 || selectedSocialId == -1) {
            Toast.makeText(getActivity(), "Please select mood and social situation", Toast.LENGTH_SHORT).show();
            return;
        }
        if (attachLocation && (latitude == 0.0 || longitude == 0.0)) {
            // optional check if you want to force valid location
            Toast.makeText(getActivity(), "Location not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get selected values
        MaterialRadioButton moodButton = getView().findViewById(selectedMoodId);
        MaterialRadioButton socialButton = getView().findViewById(selectedSocialId);
        String newMoodType = moodButton.getText().toString();
        String newSocialSituation = socialButton.getText().toString();

        double finalLat = attachLocation ? latitude : 0.0;
        double finalLng = attachLocation ? longitude : 0.0;

        // Update the mood object
        moodToEdit.setMoodDescription(reasonText);
        moodToEdit.setMood(newMoodType);
        moodToEdit.setSocialSituation(newSocialSituation);
        moodToEdit.setLatitude(finalLat);
        moodToEdit.setLongitude(finalLng);

        // If user picked a new image, base64Image is set; else keep old. So:
        moodToEdit.setImageBase64(base64Image);
        moodToEdit.setEdited(true);

        // Firestore update
        firestoreHelper.updateMood(moodToEdit, new FirestoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                // Trigger any local feed refresh if desired
                new Handler(Looper.getMainLooper()).post(() -> {
                    AppEventBus.getInstance().post(new AppEventBus.MoodAddedEvent()); // or a new 'MoodUpdatedEvent'
                });
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Mood updated!", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigate(R.id.homeFeedFragment);
            }

            @Override
            public void onFailure(Exception e) {
                if (!isAdded()) return;
                Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Open an image picker to choose a new photo.
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            ivPhotoPreview.setImageURI(imageUri);
            ivPhotoPreview.setVisibility(View.VISIBLE);
            encodeImageToBase64(imageUri);
        }
    }

    /**
     * Encodes the selected image into a Base64 string (<= 64 KB).
     */
    private void encodeImageToBase64(Uri imageUri) {
        try {
            InputStream inputStream = requireActivity().getContentResolver().openInputStream(imageUri);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            inputStream.close();

            if (bytes.length > 65536) {
                Toast.makeText(getActivity(), "Image too large! Max 64KB", Toast.LENGTH_SHORT).show();
                base64Image = null;
                return;
            }
            base64Image = Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Failed to process image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastKnownLocation();
            } else {
                Toast.makeText(getActivity(), "Location permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
