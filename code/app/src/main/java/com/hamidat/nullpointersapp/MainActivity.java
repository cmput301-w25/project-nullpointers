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

import java.util.Collection;
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

//        Adding arden as a user
//        addUser(Arden);

//        Adding to the moodHistory Note: requires for a new User to already of been created.
//        moodHistoryToFirebase("mGAucpHaF9S8GyfGzHTa", Arden);


        moodHistory Arden_Copied = firebaseToMoodHistory("mGAucpHaF9S8GyfGzHTa", new MoodHistoryCallback() {
            @Override
            public void onSuccess(moodHistory userHistory) {
                // Now the data is loaded, so you can use the moods here:
                int numMoods = userHistory.getMoodArray().size();
                Toast.makeText(MainActivity.this,
                        "There are " + numMoods + " moods. Current Name is " + userHistory.getUserName(),
                        Toast.LENGTH_LONG).show();
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MainActivity.this, "Error loading mood history", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void addUser(moodHistory user) {
//        Note: expecting user to already exist so this function will not be used
        Map<String, Object> userFields = new HashMap<>();
        userFields.put("userName", user.getUserName());

        firestore.collection("Users").add(userFields);
    }

    protected void moodHistoryToFirebase(String userID, moodHistory userMoodHistory) {
//        Adds the moodHistory to the firebase database

        CollectionReference moodReference = firestore.collection("Users").document(userID).collection("moodHistory");
//        Check database to see if this works
        for (Mood mood : userMoodHistory.getMoodArray()) {
                moodReference.add(mood);
            }
    }

    protected moodHistory firebaseToMoodHistory(String userID, MoodHistoryCallback callback) {
//        firebase query to convert a document into the moodHistory class, this is for editing/deleting.
//        Idea: Considered using Snapshot for real time, however since this would be for editing/deleting fields we can call a UI automatic update.
        moodHistory userMoodHistory = new moodHistory();

        DocumentReference userFields = firestore.collection("Users").document(userID);
        userFields.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String userName = documentSnapshot.getString("userName");
                userMoodHistory.setUserName(userName);

            } else {
                Log.d("Firestore", "No such document exists");
            }
        });


        CollectionReference moodHistoryRef = firestore.collection("Users").document(userID).collection("moodHistory");

        moodHistoryRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
//                        For document inside collection we can change it to Mood object and add it.
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Mood mood = doc.toObject(Mood.class);
                            userMoodHistory.addMood(mood);
                        }
//                        Callback for doing operations once we've retrieved all the moods (eg adding/editing)
                        callback.onSuccess(userMoodHistory);

                    } else {
                        callback.onFailure(task.getException());
                    }
                });

        return userMoodHistory;
    }

}