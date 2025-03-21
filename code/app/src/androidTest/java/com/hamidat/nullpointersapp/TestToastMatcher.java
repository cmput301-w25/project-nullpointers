package com.hamidat.nullpointersapp;

import android.view.View;
import android.view.WindowManager;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class TestToastMatcher extends TypeSafeMatcher<View> {
    @Override
    public void describeTo(Description description) {
        description.appendText("is toast");
    }

    @Override
    public boolean matchesSafely(View view) {
        if (view.getWindowToken() == null) {
            return false;
        }
        int type = view.getLayoutParams() instanceof WindowManager.LayoutParams
                ? ((WindowManager.LayoutParams) view.getLayoutParams()).type : -1;
        return type == WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG
                || type == WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
    }
}