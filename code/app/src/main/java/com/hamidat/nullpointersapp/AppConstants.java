package com.hamidat.nullpointersapp;

/**
 * Defines application-wide constants
 * These should be used instead of hardcoded numbers or strs
 * Use AppConstants when the value isn't UI related, otherwise use colors.xml or strings.xml
 */
public class AppConstants {
    // Prevent Instantiation
    private AppConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated.");
    }

    // User-related Constants
    public static final String DEFAULT_USERNAME = "GuestUser";
    public static final int MAX_USERNAME_LENGTH = 20;
}
