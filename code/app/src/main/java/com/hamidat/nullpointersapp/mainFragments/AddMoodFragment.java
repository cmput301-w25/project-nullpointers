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
import androidx.navigation.Navigation;
import androidx.fragment.app.Fragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.hamidat.nullpointersapp.MainActivity;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;
import com.hamidat.nullpointersapp.utils.mapUtils.AppEventBus;

import java.io.InputStream;

public class AddMoodFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private ImageView ivPhotoPreview;
    private Uri imageUri;
    private String base64Image;
    private FirestoreHelper firestoreHelper;
    private String currentUserId;
    private RadioGroup rgMood;
    private RadioGroup rgSocialSituation;
    private FusedLocationProviderClient fusedLocationClient;
    private double latitude;
    private double longitude;
    private boolean attachLocation = true;


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

        // Initialize UI components
        ivPhotoPreview = view.findViewById(R.id.ivPhotoPreview);
        EditText etReason = view.findViewById(R.id.Reason);
        rgMood = view.findViewById(R.id.rgMood);
        rgSocialSituation = view.findViewById(R.id.rgSocialSituation);
        Button btnAttachPhoto = view.findViewById(R.id.AttachPhoto);
        Button btnSaveEntry = view.findViewById(R.id.btnSaveEntry);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        // New: Bind the Attach Location button
        Button btnAttachLocation = view.findViewById(R.id.btnAttachLocation);
        btnAttachLocation.setOnClickListener(v -> {
            // Toggle the attachLocation flag
            attachLocation = !attachLocation;
            if (!attachLocation) {
                // Darken the button to indicate it is "pressed" (location not attached)
                // light button is no location
                // default is location attached
                btnAttachLocation.setAlpha(0.5f);
            } else {
                btnAttachLocation.setAlpha(1.0f);
            }
            Toast.makeText(getActivity(), attachLocation ? "Location Attached" : "Location Detached", Toast.LENGTH_SHORT).show();
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Get Firestore helper from MainActivity, etc.
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            this.firestoreHelper = mainActivity.getFirestoreHelper();
            this.currentUserId = mainActivity.getCurrentUserId();

            if (firestoreHelper == null) {
                Toast.makeText(getActivity(), "Error: Firestore is unavailable!", Toast.LENGTH_LONG).show();
                return;
            }

            // Request location permissions if needed
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                getLastKnownLocation();
            }

            btnAttachPhoto.setOnClickListener(v -> openImagePicker());

            btnSaveEntry.setOnClickListener(v -> {
                String reasonText = etReason.getText().toString().trim();
                int selectedMoodId = rgMood.getCheckedRadioButtonId();
                int selectedSocialId = rgSocialSituation.getCheckedRadioButtonId();

                // Validate inputs
                if (reasonText.isEmpty()) {
                    Toast.makeText(getActivity(), "Please enter a reason", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (selectedMoodId == -1 || selectedSocialId == -1) {
                    Toast.makeText(getActivity(), "Please select mood and social situation", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Only enforce location validation if attachLocation is true
                if (attachLocation && (latitude == 0.0 || longitude == 0.0)) {
                    Toast.makeText(getActivity(), "Location not available", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get selected values
                MaterialRadioButton moodButton = view.findViewById(selectedMoodId);
                MaterialRadioButton socialButton = view.findViewById(selectedSocialId);
                String moodType = moodButton.getText().toString();
                String socialSituation = socialButton.getText().toString();

                // If attachLocation is false, set lat and lng to 0.0 so that it won't be displayed on the map
                double finalLat = attachLocation ? latitude : 0.0;
                double finalLng = attachLocation ? longitude : 0.0;

                // Create Mood object based on whether a photo was attached
                Mood newlyCreatedMood;
                if (base64Image != null) {
                    newlyCreatedMood = new Mood(
                            moodType,
                            reasonText,
                            base64Image,
                            latitude,
                            longitude,
                            socialSituation,
                            currentUserId  // Pass current user's ID
                    );
                } else {
                    newlyCreatedMood = new Mood(
                            moodType,
                            reasonText,
                            latitude,
                            longitude,
                            socialSituation,
                            currentUserId  // Pass current user's ID
                    );
                }


                // Save to Firestore
                FirestoreHelper.FirestoreCallback moodCallback = new FirestoreHelper.FirestoreCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            AppEventBus.getInstance().post(new AppEventBus.MoodAddedEvent());
                        });
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "Mood saved!", Toast.LENGTH_SHORT).show();
                        mainActivity.addMoodToCache(newlyCreatedMood);
                        Navigation.findNavController(requireView()).navigate(R.id.homeFeedFragment);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        if (!isAdded()) return;
                        Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                };

                if (base64Image != null) {
                    firestoreHelper.addMoodWithPhoto(currentUserId, newlyCreatedMood, moodCallback);
                } else {
                    firestoreHelper.addMood(currentUserId, newlyCreatedMood, moodCallback);
                }
            });

            btnCancel.setOnClickListener(v -> getActivity().onBackPressed());
        }
    }


    private void getLastKnownLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                } else {
                    // Fallback to default location
                    latitude = 51.5074;  // London coordinates
                    longitude = -0.1278;
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastKnownLocation();
            } else {
                Toast.makeText(getActivity(), "Location permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }

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
}