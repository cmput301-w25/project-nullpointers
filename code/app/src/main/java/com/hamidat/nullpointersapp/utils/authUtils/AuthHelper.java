package com.hamidat.nullpointersapp.utils.authUtils;

import android.content.Context;
import android.widget.EditText;
import android.widget.Toast;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;

/**
 * AuthHelper provides utility methods for user authentication.
 */
public class AuthHelper {

    /**
     * Validates that none of the provided EditText fields are empty.
     *
     * @param context           The context in which the validation occurs.
     * @param textFieldsToCheck The EditText fields to check.
     * @return true if all fields are non-empty; false otherwise.
     */
    public static boolean validateNoEmptyFields(Context context, EditText... textFieldsToCheck) {
        for (EditText editTextField : textFieldsToCheck) {
            if (editTextField.getText().toString().trim().isEmpty()){
                giveAuthNotification(context, "Please fill in all fields");
                return false;
            }
        }
        return true;
    }

    /**
     * Callback interface for asynchronous unique username check.
     */
    public interface UniqueUsernameCallback {
        /**
         * Called when the unique username validation is complete.
         *
         * @param isUnique true if the username is unique, false otherwise.
         */
        void onResult(boolean isUnique);
    }

    /**
     * Checks Firestore to ensure that the given username is unique.
     *
     * @param context  The context in which the check occurs.
     * @param username The username to validate.
     * @param callback The callback to receive the result.
     */
    public static void validateUniqueUsername(Context context, String username, UniqueUsernameCallback callback) {
        FirestoreHelper firestoreHelper = new FirestoreHelper();
        firestoreHelper.getUserByUsername(username, new FirestoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                // A user was found, so the username is not unique.
                callback.onResult(false);
            }
            @Override
            public void onFailure(Exception e) {
                // If no user is found, we consider the username unique.
                callback.onResult(true);
            }
        });
    }

    /**
     * Adds a new user to Firestore.
     *
     * @param context         The context in which the addition occurs.
     * @param newUserUsername The new user's username.
     * @param newUserPassword The new user's password.
     * @param callback        The callback to receive the result.
     */
    public static void addNewUserToDB(Context context, String newUserUsername, String newUserPassword, FirestoreHelper.FirestoreCallback callback) {
        FirestoreHelper firestoreHelper = new FirestoreHelper();
        firestoreHelper.addUser(newUserUsername, newUserPassword, callback);
    }

    /**
     * Displays a toast notification for authentication feedback.
     *
     * @param context             The context in which the toast is shown.
     * @param notificationMessage The message to display.
     */
    public static void giveAuthNotification(Context context, String notificationMessage) {
        Toast.makeText(context, notificationMessage, Toast.LENGTH_SHORT).show();
    }
}
