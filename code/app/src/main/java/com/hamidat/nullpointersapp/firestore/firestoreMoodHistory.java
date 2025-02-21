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


//  ALL QUERYING RELATED FUNCTIONS
    public void attachSnapshotListener(Query query, String userID, MoodHistoryCallback callback) {
//       attachSnapshotListener is a function that attaches a snapshotListener to each firebase query called, allows for DRY code amongst queries.
//       Args: Query, String, MoodHistoryCallback

        moodHistory filteredMoodHistory = new moodHistory();
        filteredMoodHistory.setUserID(userID);

        query.addSnapshotListener((moodSnapshot, error) -> {
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

    public Query toggleOrder(Query query, boolean ascending) {
        Log.d("FilterTest", "toggleOrder Executed");
        if (ascending) {
            query = query.orderBy("timestamp", Query.Direction.ASCENDING);
            Log.d("FilterTest", "IF STATEMENT EXECUTED");
        } else {
            query = query.orderBy("timestamp", Query.Direction.DESCENDING);
            Log.d("FilterTest", "ELSE EXECUTED");
        }

        return query;
    }

    public void firebaseToMoodHistory(String userID, MoodHistoryCallback callback) {
//        firebaseToMoodHistory is a function that queries the entire moodHistory collection for a specific user in the Firebase db.
//        Args: String userID, MoodHistoryCallback callback

//      Retrieving the moodHistory Reference from firestore
        CollectionReference moodHistoryRef = firestore.collection("Users").document(userID).collection("moodHistory");
        attachSnapshotListener(moodHistoryRef, userID, callback);
    }

    public void firebaseQueryEmotional(String userID, String moodType, MoodHistoryCallback callback) {
//       firebaseQueryEmotional is a function to query a specific mood requested by the user, updates in real time.
//       Args: String userID, String moodType, MoodHistoryCallback callback

        CollectionReference moodHistoryRef = firestore.collection("Users").document(userID).collection("moodHistory");
        Query queryMoodType = moodHistoryRef.whereEqualTo("mood", moodType);

        attachSnapshotListener(queryMoodType, userID, callback);
    }

    public void firebaseQueryTime(String userID, Boolean sevenDays, Boolean ascending, MoodHistoryCallback callback) {
//       firebaseQueryTime is a function for querying moods in the moodHistory collection that deal with time constraints
//       Args: String userID, String moodType, MoodHistoryCallback callback
        CollectionReference moodHistoryRef = firestore.collection("Users").document(userID).collection("moodHistory");

        Calendar calendar = Calendar.getInstance();
        Date now = new Date();
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        Date oneWeekAgo = calendar.getTime();

        Timestamp nowTimestamp = new Timestamp(now);
        Timestamp oneWeekAgoTimestamp = new Timestamp(oneWeekAgo);

//      Query within 7 days and no data can have a date higher than present date.
        Query queryTime = moodHistoryRef;

        if (sevenDays) {
            queryTime = moodHistoryRef.whereGreaterThanOrEqualTo("timestamp", oneWeekAgoTimestamp).whereLessThanOrEqualTo("timestamp", nowTimestamp);
        }
//      Function for toggling ascending or descending.
        queryTime = toggleOrder(queryTime, ascending);

        attachSnapshotListener(queryTime, userID, callback);
    }

}

//        Testing Functionality.

//        Adding arden as a user
//        addUser(Arden);
//        addUser(Hamidat);

//        Adding to the moodHistory Note: requires for a new User to already of been created.
//        firestoreHistory.moodHistoryToFirebase("v1YZAtyUPIHFJd4p0cwN", Arden);
//        firestoreHistory.moodHistoryToFirebase("Ou0s0fzTO28CCn7CmHJb", Hamidat);

//        Callback function success we can display the moods to the UI
//        firestoreHistory.firebaseToMoodHistory("Ou0s0fzTO28CCn7CmHJb", new  firestoreMoodHistory.MoodHistoryCallback() {
//            @Override
//            public void onSuccess(moodHistory userMoodHistory) {
//
//                ArrayList<Mood> filtered =  userMoodHistory.filterByText("feeling good");
//                int numMoods = filtered.size();
//
////              int numMoods = userMoodHistory.getMoodArray().size();
//                Toast.makeText(MainActivity.this,
//                        "There are " + numMoods + " moods. Current ID is " + userMoodHistory.getUserID(),
//                        Toast.LENGTH_LONG).show();
//
//                for (Mood mood : filtered) {
//                    Log.d("FilterTest", "Mood Description's after filtered: " + mood.getMoodDescription());
//                }
//
//            }
//            @Override
//            public void onFailure(Exception e) {
//                Log.e("MainActivity", "Failed to load mood history", e);
//            }
//        });


//        firestoreHistory.firebaseQueryEmotional("v1YZAtyUPIHFJd4p0cwN", "Sad", new firestoreMoodHistory.MoodHistoryCallback() {
//            @Override
//            public void onSuccess(moodHistory userMoodHistory) {
//                int numMoods = userMoodHistory.getMoodArray().size();
//                Toast.makeText(MainActivity.this,
//                        "There are " + numMoods + " moods. Current ID is " + userMoodHistory.getUserID(),
//                        Toast.LENGTH_LONG).show();
//
//                for (Mood mood : userMoodHistory.getMoodArray()) {
//                    Log.d("FilterTest", "Array items gotten for SAD Query " + mood.getMoodDescription());
//                }
//
//            }
//            @Override
//            public void onFailure(Exception e) {
//                Toast.makeText(MainActivity.this, "Failed to get queried Moods",Toast.LENGTH_LONG).show();
//            }
//        });

//        firestoreHistory.firebaseQueryTime("v1YZAtyUPIHFJd4p0cwN", new firestoreMoodHistory.MoodHistoryCallback() {
//            @Override
//            public void onSuccess(moodHistory userMoodHistory) {
//                int numMoods = userMoodHistory.getMoodArray().size();
//                Toast.makeText(MainActivity.this,
//                        "There are " + numMoods + " moods. Current ID is " + userMoodHistory.getUserID(),
//                        Toast.LENGTH_LONG).show();
//            }
//            @Override
//            public void onFailure(Exception e) {
//                Toast.makeText(MainActivity.this, "Failed to get queried Moods",Toast.LENGTH_LONG).show();
//            }
//        });

//        Boolean seven_days = true;
//        Boolean ascending = false;
//
//        firestoreHistory.firebaseQueryTime("v1YZAtyUPIHFJd4p0cwN", seven_days, ascending, new firestoreMoodHistory.MoodHistoryCallback() {
//            @Override
//            public void onSuccess(moodHistory userMoodHistory) {
//                int numMoods = userMoodHistory.getMoodArray().size();
//                Toast.makeText(MainActivity.this,
//                        "There are " + numMoods + " moods. Current ID is " + userMoodHistory.getUserID(),
//                        Toast.LENGTH_LONG).show();
//
//                for (Mood mood : userMoodHistory.getMoodArray()) {
//                    Log.d("FilterTest", "Mood Description and Timestamp " + mood.getMoodDescription());
//                }
//
//            }
//            @Override
//            public void onFailure(Exception e) {
//                Toast.makeText(MainActivity.this, "Failed to get queried Moods",Toast.LENGTH_LONG).show();
//            }
//        });

// git add .
// git commit -m
// git push origin feature/moodHistory