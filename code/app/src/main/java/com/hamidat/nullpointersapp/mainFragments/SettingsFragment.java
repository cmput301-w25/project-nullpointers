/**
 * SettingsFragment.java
 *
 * This fragment provides the user with access to various account and app settings, including:
 * - Updating profile picture using image picker and UCrop for circular cropping
 * - Logging out and redirecting to the authentication screen
 * - Updating user status
 *
 * Uses Firebase Authentication for logout and Firestore for profile picture and status storage.
 * Cropped profile pictures are converted to Base64 and stored in the user's Firestore document.
 * <p><b>Outstanding issues:</b> None.</p>
 */

package com.hamidat.nullpointersapp.mainFragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

/**
 * Fragment that provides settings and account management options.
 */
public class SettingsFragment extends Fragment {

    private Button btnEditProfilePicture, btnChangeTheme, btnNotifications, btnLogout;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private FirestoreHelper firestoreHelper;
    private String currentUserId;

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     * UI should be attached to. The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from
     * a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        View layoutEditProfile = view.findViewById(R.id.layout_edit_profile);
        View layoutUpdateStatus = view.findViewById(R.id.layout_update_status);
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

        layoutUpdateStatus.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_update_status, null);
            EditText statusEditText = dialogView.findViewById(R.id.etStatus);
            TextView charCountView = dialogView.findViewById(R.id.tvCharCount);
            Button btnSave = dialogView.findViewById(R.id.btnSave);
            Button btnCancel = dialogView.findViewById(R.id.btnCancel);

            AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.CustomDialogStyle)
                    .setView(dialogView)
                    .create();

            statusEditText.addTextChangedListener(new TextWatcher() {
                /**
                 * Called before text changes.
                 * @param s The character sequence being changed.
                 * @param start The start index of the change.
                 * @param count The number of characters being replaced.
                 * @param after The number of characters that will replace the removed characters.
                 */
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                /**
                 * Called when the text changes. Updates the character count.
                 * @param s The character sequence that has changed.
                 * @param start The start index of the change.
                 * @param before The number of characters replaced.
                 * @param count The number of characters added.
                 */
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    charCountView.setText(s.length() + "/50");
                }
                /**
                 * Called after text changes.
                 * @param s The editable character sequence after the change.
                 */
                @Override public void afterTextChanged(Editable s) {}
            });

            btnSave.setOnClickListener(btn -> {
                String newStatus = statusEditText.getText().toString().trim();
                if (!newStatus.isEmpty()) {
                    firestoreHelper.updateUserStatus(currentUserId, newStatus, new FirestoreHelper.FirestoreCallback() {
                        /**
                         * Called when the status update operation succeeds.
                         *
                         * @param result The result of the operation.
                         */
                        @Override
                        public void onSuccess(Object result) {
                            Toast.makeText(getContext(), "Status updated", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }

                        /**
                         * Called when the status update operation fails.
                         *
                         * @param e The exception that occurred.
                         */
                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getContext(), "Failed to update status", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    statusEditText.setError("Status cannot be empty");
                }
            });

            btnCancel.setOnClickListener(btn -> dialog.dismiss());

            dialog.show();
        });

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

    /**
     * Handles the result from the UCrop activity.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult(),
     * allowing you to identify who this result came from.
     * @param resultCode  The integer result code returned by the child activity through its setResult().
     * @param data        An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
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

    /**
     * Updates the user's profile picture in Firestore.
     *
     * @param base64Image The Base64 encoded string of the profile picture.
     */
    private void updateUserProfilePicture(String base64Image) {
        firestoreHelper.updateUserProfilePicture(currentUserId, base64Image, new FirestoreHelper.FirestoreCallback() {
            /**
             * Called when the operation succeeds.
             *
             * @param result The result of the operation.
             */
            @Override
            public void onSuccess(Object result) {
                Toast.makeText(getActivity(), "Profile picture updated", Toast.LENGTH_SHORT).show();
            }

            /**
             * Called when the operation fails.
             *
             * @param e The exception that occurred.
             */
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getActivity(), "Failed to update profile picture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}