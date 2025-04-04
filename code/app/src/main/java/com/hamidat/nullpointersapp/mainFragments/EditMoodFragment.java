/**
 * EditMoodFragment.java
 *
 * Fragment for editing an existing Mood post.
 * Reuses the AddMoodFragment layout and functionality, but pre-fills data and updates
 * the existing document in Firestore instead of creating a new one.
 *
 * <p><b>Outstanding issues:</b> None.</p>
 */

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
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
    private Spinner spinnerMood;
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

    private SwitchCompat switchPrivacy;

    /**
     * Inflates the layout for this fragment.
     *
     * @param inflater           LayoutInflater to use
     * @param container          Optional parent view
     * @param savedInstanceState Previously saved state
     * @return The root view of the layout
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_mood, container, false);
    }

    /**
     * Called after the view is created.
     * Initializes UI elements, pre-fills data, and handles button actions.
     *
     * @param view               The root view
     * @param savedInstanceState Previously saved state
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Gets the Mood to edit from arguments
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
        rgSocialSituation = view.findViewById(R.id.rgSocialSituation);
        Button btnAttachPhoto = view.findViewById(R.id.AttachPhoto);
        Button btnSaveEntry = view.findViewById(R.id.btnSaveEntry);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        btnAttachLocation = view.findViewById(R.id.btnAttachLocation);
        switchPrivacy = view.findViewById(R.id.switchPrivacy);

        // Pre‑set - based on the existing Mood’s privacy flag
        switchPrivacy.setChecked(moodToEdit.isPrivate());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Populate fields with existing data
        etReason.setText(moodToEdit.getMoodDescription());
        latitude = moodToEdit.getLatitude();
        longitude = moodToEdit.getLongitude();
        base64Image = moodToEdit.getImageBase64();

        // Pre-select the mood radio
        spinnerMood = view.findViewById(R.id.spinnerMood);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.mood_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMood.setAdapter(adapter);

        // Pre-select the existing mood value
        String currentMood = moodToEdit.getMood();
        if (currentMood != null) {
            int position = adapter.getPosition(currentMood);
            if (position >= 0) spinnerMood.setSelection(position);
        }

        String socialStr = moodToEdit.getSocialSituation();
        if (socialStr != null && !socialStr.trim().isEmpty()) {
            for (int i = 0; i < rgSocialSituation.getChildCount(); i++) {
                RadioButton rb = (RadioButton) rgSocialSituation.getChildAt(i);
                if (rb.getText().toString().equalsIgnoreCase(socialStr.trim())) {
                    rb.setChecked(true);
                    break;
                }
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

        // Firestore helper from MainActivity
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            this.firestoreHelper = mainActivity.getFirestoreHelper();
            this.currentUserId = mainActivity.getCurrentUserId();

            if (firestoreHelper == null) {
                Toast.makeText(getActivity(), "Error: Firestore is unavailable!", Toast.LENGTH_LONG).show();
                return;
            }
        }

        //  Request location permissions if not granted
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastKnownLocation();
        }

        //  Button handlers
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
        String newMoodType = spinnerMood.getSelectedItem().toString();
        int selectedSocialId = rgSocialSituation.getCheckedRadioButtonId();
        if (selectedSocialId == -1) {
            Toast.makeText(getActivity(), "Please select a social situation", Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton socialButton = requireView().findViewById(selectedSocialId);
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

        //privacy indicator
        boolean isPrivate = switchPrivacy.isChecked();
        moodToEdit.setPrivate(isPrivate);

        // Firestore update
        firestoreHelper.updateMood(moodToEdit, new FirestoreHelper.FirestoreCallback() {
            /**
             * Called when the operation succeeds.
             *
             * @param result The result of the operation.
             */
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

            /**
             * Called when the operation fails.
             *
             * @param e The exception that occurred.
             */
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

    /**
     * Callback for activity result (e.g., image picker).
     *
     * @param requestCode Code for identifying the request
     * @param resultCode  The result code from the activity
     * @param data        The intent containing the result data
     */
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
     *
     * @param imageUri The URI of the selected image
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

    /**
     * Callback when permission request result is received.
     *
     * @param requestCode  The permission request code
     * @param permissions  The requested permissions
     * @param grantResults The results for the requested permissions
     */
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