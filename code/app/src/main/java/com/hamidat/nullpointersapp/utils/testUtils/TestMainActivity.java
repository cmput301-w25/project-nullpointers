package com.hamidat.nullpointersapp.utils.testUtils;

import com.hamidat.nullpointersapp.MainActivity;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;

public class TestMainActivity extends MainActivity {
    @Override
    public FirestoreHelper getFirestoreHelper() {
        return new FakeFirestoreHelper();
    }
}