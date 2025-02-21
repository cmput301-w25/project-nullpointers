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

public class SettingsFragment extends Fragment {
    private Button btnChangeTheme, btnNotifications, btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind UI elements
        btnChangeTheme = view.findViewById(R.id.btn_change_theme);
        btnNotifications = view.findViewById(R.id.btn_notifications);
        btnLogout = view.findViewById(R.id.btn_logout);

        // Set Click Listeners
        btnChangeTheme.setOnClickListener(v ->
                Toast.makeText(getActivity(), "Change Theme clicked", Toast.LENGTH_SHORT).show());

        btnNotifications.setOnClickListener(v ->
                Toast.makeText(getActivity(), "Notifications clicked", Toast.LENGTH_SHORT).show());

        btnLogout.setOnClickListener(v ->
                Toast.makeText(getActivity(), "Logout clicked", Toast.LENGTH_SHORT).show());
    }
}
