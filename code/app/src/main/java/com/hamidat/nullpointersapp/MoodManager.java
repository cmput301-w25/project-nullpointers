package com.hamidat.nullpointersapp; //gg

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

    public List<Mood> getMoodList() {
        return moodList;
    }

    public List<Mood> getMoodListSorted() {
        List<Mood> sortedList = new ArrayList<>(moodList);
        Collections.sort(sortedList, new Comparator<Mood>() {
            @Override
            public int compare(Mood m1, Mood m2) {
                int yearDiff = m2.getYear() - m1.getYear();
                if (yearDiff != 0) return yearDiff;

                int monthDiff = m2.getMonth() - m1.getMonth();
                if (monthDiff != 0) return monthDiff;

                int dayDiff = m2.getDay() - m1.getDay();
                if (dayDiff != 0) return dayDiff;

                int hourDiff = m2.getHour() - m1.getHour();
                if (hourDiff != 0) return hourDiff;

                return m2.getMinute() - m1.getMinute();
            }
        });
        return sortedList;
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