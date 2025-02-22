package com.hamidat.nullpointersapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

    public List<Mood> getMoodListSorted() {
        List<Mood> sortedList = new ArrayList<>(moodList);
        Collections.sort(sortedList, new Comparator<Mood>() {
            @Override
            public int compare(Mood m1, Mood m2) {
                return extractYear(m2.getDate()) - extractYear(m1.getDate()); // Sort descending
            }
        });
        return sortedList;
    }
    private int extractYear(String date) {
        try {
            return Integer.parseInt(date.substring(date.lastIndexOf("-") + 1));
        } catch (Exception e) {
            return 0; // Default if parsing fails
        }
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

