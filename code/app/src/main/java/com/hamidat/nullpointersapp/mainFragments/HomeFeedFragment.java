package com.hamidat.nullpointersapp.mainFragments;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hamidat.nullpointersapp.MainActivity;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.models.MoodAdapter;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;

import java.util.ArrayList;
import java.util.List;

// Placeholder
public class HomeFeedFragment extends Fragment {
    private RecyclerView rvMoodList;
    private MoodAdapter moodAdapter;
    private List<Mood> allMoods = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate your home feed layout (e.g. full_mood_event.xml)
        View view = inflater.inflate(R.layout.full_mood_event, container, false);

        // RecyclerView
        rvMoodList = view.findViewById(R.id.rvMoodList);
        rvMoodList.setLayoutManager(new LinearLayoutManager(getContext()));

        // Create an adapter with an *initial* list (which can be empty or loaded from Firestore)
        moodAdapter = new MoodAdapter(allMoods);
        rvMoodList.setAdapter(moodAdapter);

        // Check if there's a newly passed Mood in the arguments
        if (getArguments() != null && getArguments().containsKey("NEW_MOOD")) {
            Mood newMood = (Mood) getArguments().getSerializable("NEW_MOOD");
            if (newMood != null) {
                // Insert at the top of the list so it appears first
                allMoods.add(0, newMood);
                moodAdapter.notifyItemInserted(0);
                rvMoodList.scrollToPosition(0);
            }
        }

        return view;
    }
}


