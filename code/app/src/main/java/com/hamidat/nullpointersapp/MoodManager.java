package com.hamidat.nullpointersapp;

import java.util.ArrayList;
import java.util.List;

public class MoodManager {
    private static MoodManager instance;
    private final List<Mood> moodList;
    private MoodUpdateListener listener;

    private MoodManager() {
        moodList = new ArrayList<>();
    }

    public static MoodManager getInstance() {
        if (instance == null) {
            instance = new MoodManager();
        }
        return instance;
    }

    public void setListener(MoodUpdateListener listener) {
        this.listener = listener;
    }

    public List<Mood> getMoodList() {
        return moodList;
    }

    public void addMood(Mood mood, int index) {
        if (index >= 0 && index < moodList.size()) {
            moodList.set(index, mood);
        } else {
            moodList.add(mood);
        }
        if (listener != null) {
            listener.onMoodListUpdated();
        }
    }

    public interface MoodUpdateListener {
        void onMoodListUpdated();
    }
}

