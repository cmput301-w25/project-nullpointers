package com.hamidat.nullpointersapp;

import android.content.Intent;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;

public class ImageAttachmentHandler {
    private static final int PICK_IMAGE_REQUEST = 1;
    private final AppCompatActivity activity;
    private final SizeValidator sizeValidator;

    public ImageAttachmentHandler(AppCompatActivity activity) {
        this.activity = activity;
        this.sizeValidator = new SizeValidator();
    }

    public void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        activity.startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    public void handleActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                validateImage(uri);
            }
        }
    }

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

    private void showSuccess() {
        Toast.makeText(activity, "Photo attached successfully", Toast.LENGTH_SHORT).show();
    }

    private void showSizeError() {
        Toast.makeText(activity, "Image exceeds maximum size of 64KB", Toast.LENGTH_LONG).show();
    }

    private void showGenericError() {
        Toast.makeText(activity, "Error processing image", Toast.LENGTH_SHORT).show();
    }

    private static class SizeValidator {
        private static final long MAX_SIZE_BYTES = 65536; // 64KB

        boolean isSizeValid(long fileSize) {
            return fileSize <= MAX_SIZE_BYTES;
        }
    }
}