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






/** * HomeFeedFragment displays a list of Mood events in a RecyclerView.
 * It fetches the data from Firestore using the current user's ID
 * and updates the UI with the latest mood entries.
 *
 * @author
 *  (Salim Soufi)
 * @version 1.0
 * @since 2025-03-03
 */

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

        rvMoodList = view.findViewById(R.id.rvMoodList);
        rvMoodList.setLayoutManager(new LinearLayoutManager(getContext()));

        moodAdapter = new MoodAdapter(allMoods);
        rvMoodList.setAdapter(moodAdapter);

        // 1) Grab the entire mood list from MainActivity
        List<Mood> cachedMoods = ((MainActivity) requireActivity()).getMoodCache();

        // 2) Replace our local list with the full cache
        allMoods.clear();
        allMoods.addAll(cachedMoods);

        // 3) Notify the adapter so it displays all moods
        moodAdapter.notifyDataSetChanged();

        return view;
    }
}


