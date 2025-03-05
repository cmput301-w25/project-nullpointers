package com.hamidat.nullpointersapp.utils.firebaseUtils;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles user-related Firestore operations.
 */
public class FirestoreUsers {
    private static final String USERS_COLLECTION = "users";

    private final FirebaseFirestore firestore;

    /**
     * Constructs a new FirestoreUsers instance.
     *
     * @param firestore The FirebaseFirestore instance.
     */
    public FirestoreUsers(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Adds a new user using an auto-generated document ID.
     * Stores both the username and password.
     *
     * @param userName The user's username.
     * @param password The user's password.
     * @param callback The callback to receive the result.
     */
    public void addUser(String userName, String password,
                        FirestoreHelper.FirestoreCallback callback) {
        Map<String, Object> userFields = new HashMap<>();
        userFields.put("username", userName);
        userFields.put("password", password);

        firestore.collection(USERS_COLLECTION)
                .add(userFields)
                .addOnSuccessListener(documentReference ->
                        callback.onSuccess("User added with ID: "
                                + documentReference.getId()))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Retrieves user information by user ID.
     * On success, returns a Map<String, Object> containing user data.
     * @param userID   The user identifier.
     * @param callback The callback to receive the user data.
     */
    public void getUser(String userID, FirestoreHelper.FirestoreCallback callback) {
        firestore.collection(USERS_COLLECTION)
                .document(userID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        callback.onSuccess(documentSnapshot.getData());
                    } else {
                        callback.onFailure(new Exception("User not found"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Retrieves user information by username.
     * Returns a Map containing username, password, and the autogenerated UID.
     *
     * @param username The username to search for.
     * @param callback The callback to receive the user data.
     */
    public void getUserByUsername(String username,
                                  FirestoreHelper.FirestoreCallback callback) {
        firestore.collection(USERS_COLLECTION)
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        callback.onFailure(new Exception("User not found"));
                    } else {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        Map<String, Object> userData = doc.getData();
                        userData.put("userId", doc.getId());
                        callback.onSuccess(userData);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void getAllUsers(FirestoreHelper.FirestoreCallback callback) {
        firestore.collection(USERS_COLLECTION)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    ArrayList<Map<String, Object>> userList = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Map<String, Object> userData = doc.getData();
                        userData.put("userId", doc.getId());
                        userList.add(userData);
                    }
                    callback.onSuccess(userList);
                })
                .addOnFailureListener(callback::onFailure);
    }

}
