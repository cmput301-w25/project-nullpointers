package com.hamidat.nullpointersapp.utils.testUtils;

import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;

public class FakeFirestoreHelper extends FirestoreHelper {
    @Override
    public void updateMood(Mood mood, FirestoreCallback callback) {
        callback.onSuccess(null);
    }
}
