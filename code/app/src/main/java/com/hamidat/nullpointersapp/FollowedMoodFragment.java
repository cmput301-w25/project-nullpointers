package com.hamidat.nullpointersapp;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.MoodAdapter;
import com.hamidat.nullpointersapp.Mood;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class FollowedMoodFragment extends Fragment {

    private RecyclerView recyclerView;
    private MoodAdapter moodAdapter;
    private List<Mood> moodList;

    public FollowedMoodFragment() {
        super(R.layout.fragment_followed_moods);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        moodList = getDummyMoods();
        Collections.sort(moodList, (m1, m2) -> m2.getDateTime().compareTo(m1.getDateTime())); // Sort by date

        moodAdapter = new MoodAdapter(moodList);
        recyclerView.setAdapter(moodAdapter);
    }

    private List<Mood> getDummyMoods() {
        List<Mood> moods = new ArrayList<>();
        moods.add(new Mood("Jenny: Whats Up World", "I should be working on 301m", "Happiness", new Date(2025, 1, 2, 12, 15)));
        moods.add(new Mood("James: SUP EVERYONE IM SAD", "Idk I just feel sad sometimes", "Sadness", new Date(2025, 1, 1, 12, 15)));
        moods.add(new Mood("BestEver04: Hi I'm Hamidat", "Because I'm Hamidat", "Joy", new Date(2025, 1, 1, 12, 15)));
        return moods;
    }
}

