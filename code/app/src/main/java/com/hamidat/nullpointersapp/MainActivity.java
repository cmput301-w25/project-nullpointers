package com.hamidat.nullpointersapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.auth.User;

import com.hamidat.nullpointersapp.firestore.firestoreMoodHistory;


import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
//        Setting up firebase and adding data
        firestore = FirebaseFirestore.getInstance();

        firestoreMoodHistory firestoreHistory = new firestoreMoodHistory(firestore);

        moodHistory Arden = new moodHistory("Arden");
        moodHistory Hamidat = new moodHistory("Hamidat");



        Mood mood1 = new Mood("Happy", "I am happy");
        Mood mood2 = new Mood("Sad", "I am sad");
        Mood mood3 = new Mood("Angry", "I am angry");
        Mood mood4 = new Mood("Silly", "I am silly");

        Arden.addMood(mood1);
        Arden.addMood(mood2);
        Arden.addMood(mood3);
        Arden.addMood(mood4);

//        Adding arden as a user
//        addUser(Arden);

//        Adding to the moodHistory Note: requires for a new User to already of been created.
//        firestoreHistory.moodHistoryToFirebase("F7k2T55RobIC7dhIXmTY", Arden);

//        Callback function success we can display the moods to the UI
//        firestoreHistory.firebaseToMoodHistory("F7k2T55RobIC7dhIXmTY", new  firestoreMoodHistory.MoodHistoryCallback() {
//            @Override
//            public void onSuccess(moodHistory userMoodHistory) {
//                int numMoods = userMoodHistory.getMoodArray().size();
//                Toast.makeText(MainActivity.this,
//                        "There are " + numMoods + " moods. Current Name is " + userMoodHistory.getUserID(),
//                        Toast.LENGTH_LONG).show();
//            }
//            @Override
//            public void onFailure(Exception e) {
//                Log.e("MainActivity", "Failed to load mood history", e);
//            }
//        });

//        firestoreHistory.firebaseQueryEmotional("F7k2T55RobIC7dhIXmTY", "happy", new firestoreMoodHistory.MoodHistoryCallback() {
//            @Override
//            public void onSuccess(moodHistory userMoodHistory) {
//                int numMoods = userMoodHistory.getMoodArray().size();
//                Toast.makeText(MainActivity.this,
//                        "There are " + numMoods + " moods. Current Name is " + userMoodHistory.getUserID(),
//                        Toast.LENGTH_LONG).show();
//            }
//            @Override
//            public void onFailure(Exception e) {
//                Toast.makeText(MainActivity.this, "Failed to get queried Moods",Toast.LENGTH_LONG).show();
//            }
//        });

        firestoreHistory.firebaseQueryRecentWeek("F7k2T55RobIC7dhIXmTY", new firestoreMoodHistory.MoodHistoryCallback() {
            @Override
            public void onSuccess(moodHistory userMoodHistory) {
                int numMoods = userMoodHistory.getMoodArray().size();
                Toast.makeText(MainActivity.this,
                        "There are " + numMoods + " moods. Current ID is " + userMoodHistory.getUserID(),
                        Toast.LENGTH_LONG).show();
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MainActivity.this, "Failed to get queried Moods",Toast.LENGTH_LONG).show();
            }
        });

    }

    public void addUser(moodHistory user) {
//        Note: expecting user to already exist so this function will not be used
        Map<String, Object> userFields = new HashMap<>();
        userFields.put("userName", user.getUserName());
        firestore.collection("Users").add(userFields);
    }
}

