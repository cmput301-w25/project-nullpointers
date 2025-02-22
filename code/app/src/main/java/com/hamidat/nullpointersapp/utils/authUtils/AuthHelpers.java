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
import com.google.android.gms.tasks.Tasks;




import java.util.HashMap;
import java.util.Map;

public class AuthHelpers {

    // Adding firebase reference to be final, inside of AuthHelpers. login and sign up functions interact with firebase
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();
    private static final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    public static boolean validateNoEmptyFields(Context context, EditText... textFieldsToCheck) {
        for (EditText editTextField : textFieldsToCheck){
            if (editTextField.getText().toString().trim().isEmpty()){
                giveAuthNotification(context, "Please fill in all fields");
                return false;
            }
        }
        return true;
    }

    public interface LoginCallback {
        void onLoginResult(boolean success);
    }

    public interface SignupCallback {
        void onSignupResult(boolean success);
    }


    // A helper to ensure that the username is unique (checks the username against the db)
    public static boolean validateUniqueUsername(String username){
        // TODO - connect to the DB and return if the username already exists or not
        // For testing, just retuning true
        return true;
    }

    // A Helper to add the username and password to the db
    public static void addNewUserToDB(Context context, String newUserUsername, String newUserPassword, final SignupCallback callback) {
        auth.createUserWithEmailAndPassword(newUserUsername, newUserPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("TAG", "createUserWithEmailAndPassword: success");
                            FirebaseUser user = auth.getCurrentUser();

                            // Prepare the user data
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("uid", user.getUid());
                            userData.put("username", newUserUsername);
                            userData.put("createdAt", FieldValue.serverTimestamp());

                            // Write user data to Firestore after Creating auth user
                            firestore.collection("users").document(user.getUid()).set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        giveAuthNotification(context, "You have been successfully registered");
                                        callback.onSignupResult(true);
                                    })
                                    .addOnFailureListener(e -> {
                                        giveAuthNotification(context, "Registration failed: Firestore error");
                                        callback.onSignupResult(false);
                                    });
                        } else {
                            giveAuthNotification(context, "User registration failed");
                            callback.onSignupResult(false);
                        }
                    }
                });
    }


    /**
     * Attempts to authenticate a user with the provided credentials.
     *
     * @param username User's username.
     * @param password User's password.
     * @return True if authentication is successful, false otherwise.
     */
    public static void loginUser(String username, String password, final LoginCallback callback) {
        // TODO - use the Auth to login.

        auth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser user = auth.getCurrentUser();
                            Log.d("TAG", "User ID: " + user.getUid());

                            callback.onLoginResult(true);
                        } else {
                            Log.d("TAG", "signIn: failure", task.getException());
                            callback.onLoginResult(false);
                        }
                    }
                });
    }

    // Create a styles toast message for visual feedback
    public static void giveAuthNotification(Context context, String notificationMessage) {
        Toast.makeText(context, notificationMessage, Toast.LENGTH_SHORT).show();
    }
}
