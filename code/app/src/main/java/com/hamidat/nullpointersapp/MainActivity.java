package com.hamidat.nullpointersapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import com.hamidat.nullpointersapp.firestore.firestoreMoodHistory;
import com.hamidat.nullpointersapp.moodClasses.Mood;
import com.hamidat.nullpointersapp.moodClasses.moodHistory;

import java.util.ArrayList;
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

    }

    public void addUser(moodHistory user) {
//       Note: expecting user to already exist so this function will not be used
        Map<String, Object> userFields = new HashMap<>();
        userFields.put("userName", user.getUserName());
        firestore.collection("Users").add(userFields);
    }
}

