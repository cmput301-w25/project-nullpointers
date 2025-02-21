package com.hamidat.nullpointersapp.models;

import static com.hamidat.nullpointersapp.utils.AppConstants.MAX_USERNAME_LENGTH;
import androidx.annotation.NonNull;

/**
 * Represents a user profile in the Moodify system
 * This class manages userProfile-related data such as the username.
 */
public class UserProfile {
    private String username;

    // Constructor
    /**
     * Constructs a UserProfile with the specified username.
     *
     * @param username The user's name.
     * @throws IllegalArgumentException if the username exceeds the max length.
     */
    public UserProfile(@NonNull String username) {
        if (username.length() > MAX_USERNAME_LENGTH) {
            throw new IllegalArgumentException(
                    "Username cannot exceed " + MAX_USERNAME_LENGTH + " characters."
            );
        }
        this.username = username;
    }

    // Getters and setters
    /**
     * Returns the username of this user profile.
     *
     * @return The user's name.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username for this user profile.
     *
     * @param username The new username.
     * @throws IllegalArgumentException if the username exceeds the max length.
     */
    public void setUsername(@NonNull String username) {
        if (username.length() > MAX_USERNAME_LENGTH) {
            throw new IllegalArgumentException(
                    "Username cannot exceed " + MAX_USERNAME_LENGTH + " characters."
            );
        }
        this.username = username;
    }

    // Logic Methods
    /**
     * Checks if the username is valid based on length constraints.
     *
     * @return True if the username is valid, false otherwise.
     */
    public boolean isUsernameValid() {
        return username != null && username.length() <= MAX_USERNAME_LENGTH;
    }
}