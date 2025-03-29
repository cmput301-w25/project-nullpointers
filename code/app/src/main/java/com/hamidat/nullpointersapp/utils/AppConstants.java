/**
 * AppConstants.java
 *
 * Defines application-wide constants used across the app.
 * Prefer using AppConstants for non-UI related values.
 * For UI values, use colors.xml or strings.xml.
 *
 * Outstanding Issues: None
 */

package com.hamidat.nullpointersapp.utils;

/**
 * Defines application-wide constants.
 * These should be used instead of hardcoded numbers or strings.
 * Use AppConstants when the value isn't UI related, otherwise use colors.xml or strings.xml.
 */
public class AppConstants {
    // Prevent Instantiation
    private AppConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated.");
    }

    // User-related Constants
    /**
     * The maximum length allowed for usernames.
     */
    public static final int MAX_USERNAME_LENGTH = 20;
}