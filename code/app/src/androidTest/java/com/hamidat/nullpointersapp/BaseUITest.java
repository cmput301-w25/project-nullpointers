package com.hamidat.nullpointersapp;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;

public abstract class BaseUITest {

    protected final String TEST_USER_ID = "EHxg6TEtQFWHaqbnkt5H";
    protected final String TEST_USER_2_ID = "IDoB3Z7dsQmHtgHSW9Oc";
    protected final String TEST_USER_3_ID = "CuUYwxXUvC8OIOlsg5Mr";
    protected final String HAMIHAMI_USER_ID = "gWDMoQr8MYWJO4b39fUa";


    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            android.Manifest.permission.POST_NOTIFICATIONS,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    );

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class)
                    .setAction(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_LAUNCHER)
                    .putExtra("USER_ID", TEST_USER_ID));
}
