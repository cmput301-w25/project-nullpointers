package com.hamidat.nullpointersapp.mainFragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.hamidat.nullpointersapp.AuthActivity;
import com.hamidat.nullpointersapp.MainActivity;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class SettingsFragment extends Fragment {

    private Button btnEditProfilePicture, btnChangeTheme, btnNotifications, btnLogout;
    private ActivityResultLauncher<Intent> galleryLauncher;
    // We use onActivityResult (or the ActivityResult API) for UCrop result
    private FirestoreHelper firestoreHelper;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        View layoutEditProfile = view.findViewById(R.id.layout_edit_profile);
        View layoutChangeTheme = view.findViewById(R.id.layout_change_theme);
        View layoutNotifications = view.findViewById(R.id.layout_notifications);
        View layoutLogout = view.findViewById(R.id.layout_logout);

        firestoreHelper = ((MainActivity) getActivity()).getFirestoreHelper();
        currentUserId = ((MainActivity) getActivity()).getCurrentUserId();

        // Initialize gallery launcher for selecting an image.
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri sourceUri = result.getData().getData();
                        if (sourceUri != null) {
                            // Prepare a destination URI for the cropped image.
                            String destFileName = "CroppedImage_" + UUID.randomUUID().toString() + ".jpg";
                            Uri destinationUri = Uri.fromFile(new File(getContext().getCacheDir(), destFileName));
                            // Configure UCrop for circular cropping.
                            UCrop.Options options = new UCrop.Options();
                            options.setCircleDimmedLayer(true);
                            options.setShowCropFrame(false);
                            options.setShowCropGrid(false);
                            UCrop.of(sourceUri, destinationUri)
                                    .withAspectRatio(1, 1)
                                    .withMaxResultSize(500, 500)
                                    .withOptions(options)
                                    .start(getContext(), SettingsFragment.this);
                        }
                    }
                }
        );

        layoutEditProfile.setOnClickListener(v -> {
            // Open the gallery to select an image.
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
        });

        layoutChangeTheme.setOnClickListener(v ->
                Toast.makeText(getActivity(), "Change Theme clicked", Toast.LENGTH_SHORT).show());

        layoutNotifications.setOnClickListener(v ->
                Toast.makeText(getActivity(), "Notifications clicked", Toast.LENGTH_SHORT).show());

        layoutLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(getActivity(), "Logged out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        });

        return view;
    }

    // Handle the UCrop result.
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UCrop.REQUEST_CROP) {
            if (resultCode == getActivity().RESULT_OK && data != null) {
                Uri resultUri = UCrop.getOutput(data);
                if (resultUri != null) {
                    try {
                        // Convert the cropped image to a Bitmap.
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), resultUri);
                        // Compress the Bitmap to JPEG.
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                        byte[] imageBytes = baos.toByteArray();
                        // Convert image bytes to Base64 string.
                        String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                        // Update Firestore with the new profile picture.
                        updateUserProfilePicture(base64Image);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "Failed to load cropped image", Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (resultCode == UCrop.RESULT_ERROR && data != null) {
                Throwable cropError = UCrop.getError(data);
                Toast.makeText(getActivity(), "Crop error: " + cropError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateUserProfilePicture(String base64Image) {
        // Update the user's document with the new profile picture.
        // You need to implement updateUserProfilePicture in FirestoreHelper/FirestoreUsers.
        firestoreHelper.updateUserProfilePicture(currentUserId, base64Image, new FirestoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                Toast.makeText(getActivity(), "Profile picture updated", Toast.LENGTH_SHORT).show();
                // Optionally, signal the ProfileFragment to refresh its UI.
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getActivity(), "Failed to update profile picture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
