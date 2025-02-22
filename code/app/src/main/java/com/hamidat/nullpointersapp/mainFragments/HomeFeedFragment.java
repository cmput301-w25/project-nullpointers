package com.hamidat.nullpointersapp.mainFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.utils.homeFeedUtils.MoodAdapter;

import java.util.ArrayList;
import java.util.List;

public class HomeFeedFragment extends Fragment {

    private RecyclerView moodRecyclerView;
    private MoodAdapter moodAdapter;

    public HomeFeedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.full_mood_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize RecyclerView
        moodRecyclerView = view.findViewById(R.id.rvMoodList);
        moodRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Setup adapter with dummy data
        List<String> dummyMoods = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            dummyMoods.add("Mood Event #" + (i + 1));
        }
        moodAdapter = new MoodAdapter(dummyMoods);
        moodRecyclerView.setAdapter(moodAdapter);

        // Setup filter buttons
        view.findViewById(R.id.btnFilter).setOnClickListener(v ->
                Toast.makeText(getContext(), "Filter clicked", Toast.LENGTH_SHORT).show()
        );

        view.findViewById(R.id.btnFollowing).setOnClickListener(v ->
                Toast.makeText(getContext(), "Following clicked", Toast.LENGTH_SHORT).show()
        );
    }
}