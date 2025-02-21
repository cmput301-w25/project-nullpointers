package com.hamidat.nullpointersapp.mainFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.hamidat.nullpointersapp.R;

/**
 * Handles the settings UI for the application.
 */
public class SettingsFragment extends Fragment {

    /**
     * Inflates the fragment layout.
     *
     * @param inflater           LayoutInflater to inflate the view.
     * @param container          The parent view.
     * @param savedInstanceState Saved state data.
     * @return The inflated view.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    /**
     * Binds UI elements and sets click listeners.
     *
     * @param view               The view returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState Saved state data.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind UI elements
        Button btnChangeTheme = view.findViewById(R.id.btn_change_theme);
        Button btnNotifications = view.findViewById(R.id.btn_notifications);
        Button btnLogout = view.findViewById(R.id.btn_logout);

        // Set click listeners with Toast feedback
        btnChangeTheme.setOnClickListener(v ->
                Toast.makeText(getActivity(), "Change Theme clicked", Toast.LENGTH_SHORT).show());

        btnNotifications.setOnClickListener(v ->
                Toast.makeText(getActivity(), "Notifications clicked", Toast.LENGTH_SHORT).show());

        btnLogout.setOnClickListener(v ->
                Toast.makeText(getActivity(), "Logout clicked", Toast.LENGTH_SHORT).show());
    }
}
