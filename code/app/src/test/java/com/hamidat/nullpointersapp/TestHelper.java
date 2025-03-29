package com.hamidat.nullpointersapp;

import android.util.Base64;

import com.hamidat.nullpointersapp.mainFragments.FollowingFragment;
import com.hamidat.nullpointersapp.mainFragments.SearchFragment;
import com.hamidat.nullpointersapp.models.Mood;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for common test-related helper methods.
 */
public class TestHelper {
    /**
     * Creates a Mood object based on provided parameters.
     */
    public static Mood createMood(String reasonText, String moodType, String socialSituation,
                                  double latitude, double longitude, String currentUserId,
                                  boolean isPrivate, String base64Image) {
        if (base64Image != null) {
            return new Mood(moodType, reasonText, base64Image, latitude, longitude, socialSituation, currentUserId, isPrivate);
        } else {
            return new Mood(moodType, reasonText, latitude, longitude, socialSituation, currentUserId, isPrivate);
        }
    }

    /**
     * Updates the given Mood object with new values.
     *
     * @param mood                The mood to update.
     * @param newDescription      The new mood description.
     * @param newMoodType         The new mood type.
     * @param newSocialSituation  The new social situation.
     * @param newLatitude         The new latitude value.
     * @param newLongitude        The new longitude value.
     * @param newImageBase64      Optional new image Base64 string.
     */
    public static void updateMoodObject(Mood mood, String newDescription, String newMoodType,
                                        String newSocialSituation, double newLatitude,
                                        double newLongitude, String newImageBase64) {
        mood.setMoodDescription(newDescription);
        mood.setMood(newMoodType);
        mood.setSocialSituation(newSocialSituation);
        mood.setLatitude(newLatitude);
        mood.setLongitude(newLongitude);
        if (newImageBase64 != null) {
            mood.setImageBase64(newImageBase64);
        }
        mood.setEdited(true);
    }
    /**
     * Checks if a username is valid.
     * A valid username is non-null and not empty (after trimming whitespace).
     *
     * @param username The username to validate.
     * @return {@code true} if the username is valid, {@code false} otherwise.
     */
    public static boolean isUsernameValid(String username) {
        return username != null && !username.trim().isEmpty();
    }

    /**
     * Checks if a user exists in the given following list.
     *
     * @param userId        The ID of the user to check.
     * @param followingList The list of followed users.
     * @return {@code true} if the user is in the following list, {@code false} otherwise.
     */
    public static boolean isUserFollowed(String userId, List<FollowingFragment.User> followingList) {
        if (userId == null || followingList == null) return false;
        for (FollowingFragment.User user : followingList) {
            if (user.userId.equals(userId)) return true;
        }
        return false;
    }

    /**
     * Determines whether a follow/unfollow action is valid.
     * <ul>
     *     <li>If {@code isFollowing} is {@code true}, the user must already be followed.</li>
     *     <li>If {@code isFollowing} is {@code false}, the user must not be followed.</li>
     * </ul>
     *
     * @param userId        The ID of the user to check.
     * @param followingList The list of followed users.
     * @param isFollowing   {@code true} if checking for a valid unfollow action, {@code false} for a follow action.
     * @return {@code true} if the action is valid, {@code false} otherwise.
     */
    public static boolean isFollowActionValid(String userId, List<FollowingFragment.User> followingList, boolean isFollowing) {
        if (userId == null || followingList == null) return false;
        return isFollowing ? isUserFollowed(userId, followingList) : !isUserFollowed(userId, followingList);
    }

    /**
     * Filters moods to show only public moods or the user's private moods.
     *
     * @param moods List of all moods.
     * @param currentUserId The ID of the logged-in user.
     * @return A filtered list of moods.
     */
    public static List<Mood> filterMoods(List<Mood> moods, String currentUserId) {
        List<Mood> filteredMoods = new ArrayList<>();
        for (Mood mood : moods) {
            if (!mood.isPrivate() || mood.getUserId().equals(currentUserId)) {
                filteredMoods.add(mood);
            }
        }
        return filteredMoods;
    }

    /**
     * Sorts moods by timestamp, newest first.
     *
     * @param moods List of moods.
     */
    public static void sortMoodsByTimestamp(List<Mood> moods) {
        Collections.sort(moods, (m1, m2) -> {
            if (m1.getTimestamp() == null || m2.getTimestamp() == null) {
                return 0;
            }
            return m2.getTimestamp().compareTo(m1.getTimestamp()); // Newest first
        });
    }

    /**
     * Checks if the mood object is valid for deletion.
     *
     * @param mood The Mood object to validate.
     * @return true if the mood has a non-null and non-empty ID; false otherwise.
     */
    public static boolean isValidMoodForDeletion(Mood mood) {
        return mood != null && mood.getMoodId() != null && !mood.getMoodId().isEmpty();
    }

    /**
     * Calculates the most frequent mood from a list.
     *
     * @param moods List of Mood objects.
     * @return Most frequent mood string, or "N/A" if list is empty or null.
     */
    public static String getMostFrequentMood(List<Mood> moods) {
        if (moods == null || moods.isEmpty()) return "N/A";

        Map<String, Integer> freqMap = new HashMap<>();
        for (Mood mood : moods) {
            String moodType = mood.getMood();
            if (moodType != null) {
                freqMap.put(moodType, freqMap.getOrDefault(moodType, 0) + 1);
            }
        }

        String mostFrequent = "N/A";
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : freqMap.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostFrequent = entry.getKey();
            }
        }
        return mostFrequent;
    }

    /**
     * Filters a list of Mood objects based on selected mood categories.
     *
     * @param moods       List of all moods to filter.
     * @param showAll     If true, return all moods.
     * @param happy       If true, include "Happy" moods.
     * @param sad         If true, include "Sad" moods.
     * @param angry       If true, include "Angry" moods.
     * @param chill       If true, include "Chill" moods.
     * @return Filtered list of Mood objects.
     */
    public static List<Mood> filterMoods(List<Mood> moods, boolean showAll, boolean happy, boolean sad, boolean angry, boolean chill) {
        if (showAll) return new ArrayList<>(moods); // Return all if "Show All" is checked

        List<Mood> filteredMoods = new ArrayList<>();
        for (Mood mood : moods) {
            if (mood == null || mood.getMood() == null) continue;

            if ((happy && mood.getMood().equals("Happy")) ||
                    (sad && mood.getMood().equals("Sad")) ||
                    (angry && mood.getMood().equals("Angry")) ||
                    (chill && mood.getMood().equals("Chill"))) {
                filteredMoods.add(mood);
            }
        }
        return filteredMoods;
    }

    /**
     * Returns a "time ago" string based on the given timestamp in milliseconds.
     *
     * @param timeMillis Timestamp in milliseconds.
     * @return A human-readable "time ago" string.
     */
    public static String getTimeAgo(long timeMillis) {
        long now = System.currentTimeMillis();
        long diff = now - timeMillis;

        if (diff < TimeUnit.MINUTES.toMillis(1)) {
            return "Just now";
        } else if (diff < TimeUnit.HOURS.toMillis(1)) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
        } else if (diff < TimeUnit.DAYS.toMillis(1)) {
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        } else if (diff < TimeUnit.DAYS.toMillis(7)) {
            long days = TimeUnit.MILLISECONDS.toDays(diff);
            return days + " day" + (days == 1 ? "" : "s") + " ago";
        } else {
            long weeks = TimeUnit.MILLISECONDS.toDays(diff) / 7;
            return weeks + " week" + (weeks == 1 ? "" : "s") + " ago";
        }
    }
    /**
     * Decodes a base64 string into a byte array.
     *
     * @param base64 The base64-encoded string.
     * @return The decoded byte array, or null if decoding fails.
     */
    public static byte[] decodeBase64(String base64) {
        try {
            return Base64.decode(base64, Base64.DEFAULT);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Formats a username display string.
     *
     * @param username The username.
     * @return Formatted string like "My Username: John".
     */
    public static String formatUsernameDisplay(String username) {
        return "My Username: " + username;
    }

    /**
     * Filters a list of users by username (case-insensitive) and limits the result to a max count.
     *
     * @param users List of users to filter.
     * @param query Search query (case-insensitive).
     * @param currentUserId ID of current user (excluded from result).
     * @param maxResults Maximum number of results to return.
     * @return Filtered and limited list of users.
     */
    public static List<SearchFragment.User> filterUsers(List<SearchFragment.User> users, String query, String currentUserId, int maxResults) {
        List<SearchFragment.User> result = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (SearchFragment.User user : users) {
            if (user.userId.equals(currentUserId)) continue;
            if (user.username.toLowerCase().contains(lowerQuery)) {
                result.add(user);
            }
            if (result.size() >= maxResults) break;
        }

        return result;
    }

    /**
     * Filters a list of Mood objects based on whether their description contains a given keyword.
     *
     * @param moods   The list of Mood objects.
     * @param keyword The keyword to search for (case-insensitive).
     * @return A filtered list of Mood objects where the description contains the keyword.
     */
    public static ArrayList<Mood> filterMoodsByDescription(ArrayList<Mood> moods, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return moods;

        ArrayList<Mood> filteredList = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();

        for (Mood mood : moods) {
            String description = mood.getMoodDescription() != null ? mood.getMoodDescription().toLowerCase() : "";
            if (description.contains(lowerKeyword)) {
                filteredList.add(mood);
            }
        }
        return filteredList;
    }

}
