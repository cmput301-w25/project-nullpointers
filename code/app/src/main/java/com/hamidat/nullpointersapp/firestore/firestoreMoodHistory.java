package com.hamidat.nullpointersapp.firestore;

import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.Timestamp;
import com.hamidat.nullpointersapp.MainActivity;
import com.hamidat.nullpointersapp.Mood;
import com.hamidat.nullpointersapp.moodHistory;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


import org.w3c.dom.Document;

// Class for interacting with firebase
public class firestoreMoodHistory {
    protected FirebaseFirestore firestore;

    public firestoreMoodHistory(FirebaseFirestore firestore_ref) {
        this.firestore = firestore_ref;
    }

    public interface MoodHistoryCallback {
        void onSuccess(moodHistory userHistory);
        void onFailure(Exception e);
    }

    public void moodHistoryToFirebase(String userID, moodHistory userMoodHistory) {
//        moodHistoryToFirebase is a function that adds all values from a moodHistory to firebase, this is mostly for test adding data.
//        Args: userID, moodHistory userMoodHistory (current users moods).

        CollectionReference moodReference = this.firestore.collection("Users").document(userID).collection("moodHistory");
//      Check database to see if this works
        for (Mood mood : userMoodHistory.getMoodArray()) {
            moodReference.add(mood);
        }
    }

    public void firebaseToMoodHistory(String userID, MoodHistoryCallback callback) {
//        firebaseToMoodHistory is a function that queries the entire moodHistory collection for a specific user in the Firebase db.
//        Args: String userID, MoodHistoryCallback callback

        moodHistory userMoodHistory = new moodHistory(userID);

//        Retrieving the moodHistory Reference from firestore
        CollectionReference moodHistoryRef = firestore.collection("Users").document(userID).collection("moodHistory");

        moodHistoryRef.addSnapshotListener((moodSnapshot, error) -> {
            if (moodSnapshot != null) {
                for(QueryDocumentSnapshot doc : moodSnapshot) {
                    Mood mood = doc.toObject(Mood.class);
                    userMoodHistory.addMood(mood);
                }
                callback.onSuccess(userMoodHistory);
            }
        });
    }
    public void firebaseQueryRecentWeek(String userID, MoodHistoryCallback callback) {
//        firebaseQueryRecentWeek is a function for querying moods in the moodHistory collection that are less than a week old.
//       Args: String userID, String moodType, MoodHistoryCallback callback
        CollectionReference moodHistoryRef = firestore.collection("Users").document(userID).collection("moodHistory");

        Calendar calendar = Calendar.getInstance();
        Date now = new Date();
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        Date oneWeekAgo = calendar.getTime();

        Timestamp oneWeekAgoTimestamp = new Timestamp(oneWeekAgo);

        moodHistory filteredMoodHistory = new moodHistory(userID);

        Query queryRecentWeek = moodHistoryRef.whereGreaterThanOrEqualTo("formattedDate", oneWeekAgoTimestamp);
        queryRecentWeek.addSnapshotListener((moodSnapshot, error) -> {
            if (error != null) {
                callback.onFailure(error);
                return;
            }
            if (moodSnapshot != null) {
                for(QueryDocumentSnapshot doc : moodSnapshot) {
                    Mood mood = doc.toObject(Mood.class);
                    filteredMoodHistory.addMood(mood);
                }
                callback.onSuccess(filteredMoodHistory);
            }

        });

    }

    public void firebaseQueryEmotional(String userID, String moodType, MoodHistoryCallback callback) {
//       firebaseQueryEmotional is a function to query a specific mood requested by the user, updates in real time.
//       Args: String userID, String moodType, MoodHistoryCallback callback

        moodHistory filteredMoodHistory = new moodHistory(userID);

        CollectionReference moodHistoryRef = firestore.collection("Users").document(userID).collection("moodHistory");
        Query queryMoodType = moodHistoryRef.whereEqualTo("mood", moodType);

        queryMoodType.addSnapshotListener((moodSnapshot, error) -> {
            if (error != null) {
                callback.onFailure(error);
                return;
            }
            if (moodSnapshot != null) {
              for(QueryDocumentSnapshot doc : moodSnapshot) {
                  Mood mood = doc.toObject(Mood.class);
                  filteredMoodHistory.addMood(mood);
              }
              callback.onSuccess(filteredMoodHistory);
            }
        });
    }

//    public moodHistory setQueriedUserFields(String userID) {
////        setQueriedUserFields is a function that takes in a userID and retrieves all sets all the important fields for the moodHistory class
////        will be subject to change as moodHistory changes.
//
//        moodHistory userMoodHistory = new moodHistory();
//
////      Retrieving all the key value pairs for the document of the User ID.
//        DocumentReference userFields = firestore.collection("Users").document(userID);
//
////      Setting the moodHistory class in java to have the important user Fields
//        userFields.addSnapshotListener((documentSnapshot, error) -> {
//            if (documentSnapshot != null && documentSnapshot.exists()) {
////              Setting fields for moodHistory
//                String userName = documentSnapshot.getString("userName");
//                userMoodHistory.setUserName(userName);
//            }
//        });
//
//        return userMoodHistory;
//    }

}

// git add .
// git commit -m
// git push origin feature/moodHistory