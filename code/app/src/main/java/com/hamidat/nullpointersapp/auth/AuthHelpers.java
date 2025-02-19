package com.hamidat.nullpointersapp.auth;

import android.content.Context;
import android.widget.EditText;
import android.widget.Toast;

public class AuthHelpers {
    public static boolean validateNoEmptyFields(Context context, EditText... textFieldsToCheck) {
        for (EditText editTextField : textFieldsToCheck){
            if (editTextField.getText().toString().trim().isEmpty()){
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show();
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
    public static boolean addNewUserToDB (Context context, String newUserUsername, String newUserPassword) {
        // TODO - connect to the DB and add adding a new user
        // Give a response message that they're registered
        Toast.makeText(context, "You have been successfully registered", Toast.LENGTH_SHORT).show();

        // For testing, just returning true
        return true;
    }
}
