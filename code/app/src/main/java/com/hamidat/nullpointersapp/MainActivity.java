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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.auth.User;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    public interface MoodHistoryCallback {
        void onSuccess(moodHistory userHistory);
        void onFailure(Exception e);
    }

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

        moodHistory Arden = new moodHistory("Arden");
        moodHistory Hamidat = new moodHistory("Hamidat");



        Mood mood1 = new Mood("Happy", "I am happy");
        Mood mood2 = new Mood("Sad", "I am sad");
        Mood mood3 = new Mood("Angry", "I am angry");

        Arden.addMood(mood1);
        Arden.addMood(mood2);
        Arden.addMood(mood3);

        Hamidat.addMood(mood1);

        moodHistoryToFirebase(Arden);
        moodHistoryToFirebase(Hamidat);

//        moodHistory Arden_Copied = firebaseToMoodHistory("Arden");
//        int numMoods = Arden_Copied.getMoodArray().size();

//        Toast.makeText(MainActivity.this, "There are" + numMoods + "moods", Toast.LENGTH_LONG).show();
//        Toast.makeText(MainActivity.this, "Current Name is " + Arden_Copied.getUserName(), Toast.LENGTH_LONG).show();



    }

    protected void moodHistoryToFirebase(moodHistory userMoodHistory) {
//        adds/edits moodHistory class to Firebase database

//        If user doesn't exist add it with a mood (temporary)
        Map<String, Object> userFields = new HashMap<>();
        userFields.put("userName", userMoodHistory.getUserName());

        firestore.collection("Users").add(userFields).addOnSuccessListener(userDocRef -> {

            CollectionReference historyRef = userDocRef.collection("moodHistory");
            for (Mood mood : userMoodHistory.getMoodArray()) {
                historyRef.add(mood);
            }
        });

    }
    protected moodHistory firebaseToMoodHistory(String userName, MoodHistoryCallback callback) {
//        firebase query to get the userName (can be changed to user ID) from firebase db in users table to moodHistory class which can be edited.
        CollectionReference historyRef = firestore.collection("users")
                .document(userName)
                .collection("moodHistory");
        moodHistory userMoodHistory = new moodHistory(userName);

        historyRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Mood mood = doc.toObject(Mood.class);
                            userMoodHistory.addMood(mood);
                        }
                        callback.onSuccess(userMoodHistory);

                    } else {
                        callback.onFailure(task.getException());
                    }
                });

        return userMoodHistory;
    }

}