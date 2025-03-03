package com.hamidat.nullpointersapp.mainFragments;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.hamidat.nullpointersapp.R;
// Placeholder
public class HomeFeedFragment extends Fragment {

    public HomeFeedFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate full_mood_event.xml instead of a separate layout file
        return inflater.inflate(R.layout.full_mood_event, container, false);
    }
}
