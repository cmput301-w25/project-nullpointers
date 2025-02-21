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


import java.util.ArrayList;
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

//
//        moodHistory Arden = new moodHistory();
//        Arden.setUserName("Arden");
//        moodHistory Hamidat = new moodHistory();
//        Hamidat.setUserName("Hamidat");
//
//        Mood mood1 = new Mood("Happy", "I am happy");
//        Mood mood2 = new Mood("Sad", "I am sad");
//        Mood mood3 = new Mood("Angry", "I am angry");
//        Mood mood4 = new Mood("Silly", "I am silly");
//
//        Mood mood5 = new Mood("Happy", "I am currently feeling good");
//        Mood mood6 = new Mood("Intrigued", "I am currently feeling intrigued");
//
//        Arden.addMood(mood1);
//        Arden.addMood(mood2);
//        Arden.addMood(mood3);
//        Arden.addMood(mood4);
//
//        Hamidat.addMood(mood5);
//        Hamidat.addMood(mood6);

    }

    public void addUser(moodHistory user) {
//        Note: expecting user to already exist so this function will not be used
        Map<String, Object> userFields = new HashMap<>();
        userFields.put("userName", user.getUserName());
        firestore.collection("Users").add(userFields);
    }
}

