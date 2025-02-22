package com.hamidat.nullpointersapp.utils.authUtils;

import android.content.Context;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;



import java.util.HashMap;
import java.util.Map;

public class AuthHelpers {
    public static boolean validateNoEmptyFields(Context context, EditText... textFieldsToCheck) {
        for (EditText editTextField : textFieldsToCheck){
            if (editTextField.getText().toString().trim().isEmpty()){
                giveAuthNotification(context, "Please fill in all fields");
                return false;
            }
        }
        return true;
    }

    // A helper to ensure that the username is unique (checks the username against the db)
    public static boolean validateUniqueUsername(String username){
        // TODO - connect to the DB and return if the username already exists or not
        // For testing, just retuning true
        return true;
    }

    // A Helper to add the username and password to the db
    public static boolean addNewUserToDB (Context context, String newUserUsername, String newUserPassword, FirebaseAuth auth,  FirebaseFirestore firestore) {
        // TODO - connect to the DB and add adding a new user
        // Give a response message that they're registered

        auth.createUserWithEmailAndPassword(newUserUsername, newUserPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("TAG", "createUserWithEmailAndPassword:success");
                            FirebaseUser user = auth.getCurrentUser();

                            Map<String, Object> userData = new HashMap<>();
                            userData.put("uid", user.getUid());
                            userData.put("username", newUserUsername);
                            userData.put("createdAt", FieldValue.serverTimestamp());

                            firestore.collection("users").document(user.getUid()).set(userData)
                                    .addOnSuccessListener(aVoid -> Log.d("TAG", "User added to Firestore"))
                                    .addOnFailureListener(e -> Log.d("TAG", "Firestore error: " + e.getMessage()));
                        } else {
                            Log.d("TAG", "createUserWithEmailAndPassword:failure", task.getException());
                        }
                    }
                });


        giveAuthNotification(context, "You have been successfully registered");
        // For testing, just returning true
        return true;
    }

    // Create a styles toast message for visual feedback
    public static void giveAuthNotification(Context context, String notificationMessage) {
        Toast.makeText(context, notificationMessage, Toast.LENGTH_SHORT).show();
    }
}
