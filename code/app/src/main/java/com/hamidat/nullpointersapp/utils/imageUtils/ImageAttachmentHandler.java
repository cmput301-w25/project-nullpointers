/**
 * ImageAttachmentHandler.java
 *
 * Handles image selection and validation from the device's storage.
 * Validates that the image size does not exceed 64KB.
 *
 * Outstanding Issues: None
 */

package com.hamidat.nullpointersapp.utils.imageUtils;

import android.content.Intent;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;

/**
 * Handles image selection and validation from the device's storage.
 */
public class ImageAttachmentHandler {
    private static final int PICK_IMAGE_REQUEST = 1;
    private final AppCompatActivity activity;
    private final SizeValidator sizeValidator;

    /**
     * Constructs a new ImageAttachmentHandler with the given AppCompatActivity.
     *
     * @param activity The AppCompatActivity used for starting activities and accessing content resolvers.
     */
    public ImageAttachmentHandler(AppCompatActivity activity) {
        this.activity = activity;
        this.sizeValidator = new SizeValidator();
    }

    /**
     * Opens the device's image picker to select an image.
     */
    public void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        activity.startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    /**
     * Handles the result from the image picker activity.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult(), allowing you to identify who this result came from.
     * @param resultCode The integer result code returned by the child activity through its setResult().
     * @param data An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    public void handleActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                validateImage(uri);
            }
        }
    }

    /**
     * Validates the selected image's size.
     *
     * @param uri The URI of the selected image.
     */
    private void validateImage(Uri uri) {
        try (ParcelFileDescriptor fileDescriptor =
                     activity.getContentResolver().openFileDescriptor(uri, "r")) {
            long fileSize = fileDescriptor.getStatSize();
            if (sizeValidator.isSizeValid(fileSize)) {
                showSuccess();
            } else {
                showSizeError();
            }
        } catch (IOException e) {
            showGenericError();
        }
    }

    /**
     * Shows a success message when the image is successfully attached.
     */
    private void showSuccess() {
        Toast.makeText(activity, "Photo attached successfully", Toast.LENGTH_SHORT).show();
    }

    /**
     * Shows an error message when the image size exceeds the maximum allowed size.
     */
    private void showSizeError() {
        Toast.makeText(activity, "Image exceeds maximum size of 64KB", Toast.LENGTH_LONG).show();
    }

    /**
     * Shows a generic error message when an error occurs while processing the image.
     */
    private void showGenericError() {
        Toast.makeText(activity, "Error processing image", Toast.LENGTH_SHORT).show();
    }

    /**
     * Inner class for validating image size.
     */
    private static class SizeValidator {
        private static final long MAX_SIZE_BYTES = 65536; // 64KB

        /**
         * Checks if the given file size is valid.
         *
         * @param fileSize The size of the file in bytes.
         * @return true if the file size is valid, false otherwise.
         */
        boolean isSizeValid(long fileSize) {
            return fileSize <= MAX_SIZE_BYTES;
        }
    }
}