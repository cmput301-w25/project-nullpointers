//package com.hamidat.nullpointersapp;
//
//import android.widget.Button;
//
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//
//// TODO: Restore this functionality - was moved out of MainActivity.java
//public class attachPhotoHelper {
//    private ImageAttachmentHandler imageHandler;
//
//    setContentView(R.layout.mood_reason);
//    imageHandler = new ImageAttachmentHandler(this);
//    Button attachPhotoButton = findViewById(R.id.AttachPhoto);
//        attachPhotoButton.setOnClickListener(v -> imageHandler.openImagePicker());
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//        Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//        v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//        return insets;
//    });
//}
