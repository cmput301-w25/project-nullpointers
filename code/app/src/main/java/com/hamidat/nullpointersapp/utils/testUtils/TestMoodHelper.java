package com.hamidat.nullpointersapp.utils.testUtils;

import android.os.SystemClock;
import android.util.Log;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;
import com.hamidat.nullpointersapp.utils.homeFeedUtils.CommentsBottomSheetFragment;

public class TestMoodHelper {

    public static void insertTestMood(String userId,
                                      String moodType,
                                      String moodDescription,
                                      double latitude,
                                      double longitude,
                                      String socialSituation,
                                      boolean isPrivate) {

        FirestoreHelper firestoreHelper = new FirestoreHelper();

        Mood mood = new Mood(
                moodType,
                moodDescription,
                latitude,
                longitude,
                socialSituation,
                userId,
                isPrivate
        );

        firestoreHelper.addMood(userId, mood, new FirestoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                Log.d("TestMoodHelper", "Test mood added: " + moodDescription);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("TestMoodHelper", "Failed to add test mood: " + e.getMessage());
            }
        });

        SystemClock.sleep(2000); // crude wait for async Firestore op
    }

    public static void deleteMoodByDescription(String userId, String moodDesc) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("moods")
                .whereEqualTo("moodDescription", moodDesc)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    for (var doc : querySnapshots.getDocuments()) {
                        String moodId = doc.getId();

                        db.collection("moods").document(moodId).delete()
                                .addOnSuccessListener(aVoid ->
                                        Log.d("TestDataUtil", "Deleted mood from moods collection: " + moodId))
                                .addOnFailureListener(e ->
                                        Log.e("TestDataUtil", "Failed to delete mood: " + e.getMessage()));

                        db.collection("users").document(userId)
                                .update("moodHistory", FieldValue.arrayRemove(moodId))
                                .addOnSuccessListener(aVoid ->
                                        Log.d("TestDataUtil", "Deleted mood from moodHistory: " + moodId))
                                .addOnFailureListener(e ->
                                        Log.e("TestDataUtil", "Failed to delete from moodHistory: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("TestDataUtil", "Failed to query mood for deletion: " + e.getMessage()));
    }

    public static void insertComment(String moodId,
                                     String userId,
                                     String username,
                                     String commentText) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CommentsBottomSheetFragment.Comment comment = new CommentsBottomSheetFragment.Comment(
                userId,
                username,
                commentText,
                new com.google.firebase.Timestamp(new java.util.Date())
        );

        db.collection("moods").document(moodId)
                .collection("comments")
                .add(comment)
                .addOnSuccessListener(docRef -> {
                    Log.d("TestMoodHelper", "Test comment added to mood: " + moodId);
                })
                .addOnFailureListener(e -> {
                    Log.e("TestMoodHelper", "Failed to add test comment: " + e.getMessage());
                });

        SystemClock.sleep(1500); // crude wait for sync if needed
    }

    public interface MoodIdCallback {
        void onMoodIdFound(String moodId);
        void onError(Exception e);
    }

    public static void getMoodIdByDescription(String userId, String moodDescription, MoodIdCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("moods")
                .whereEqualTo("userId", userId)
                .whereEqualTo("moodDescription", moodDescription)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String moodId = querySnapshot.getDocuments().get(0).getId();
                        callback.onMoodIdFound(moodId);
                    } else {
                        callback.onError(new Exception("Mood not found"));
                    }
                })
                .addOnFailureListener(callback::onError);
    }

}
