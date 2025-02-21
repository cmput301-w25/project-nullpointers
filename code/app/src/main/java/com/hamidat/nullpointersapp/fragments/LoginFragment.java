package com.hamidat.nullpointersapp.fragments;

import static com.hamidat.nullpointersapp.utils.AuthHelpers.giveAuthNotification;
import static com.hamidat.nullpointersapp.utils.AuthHelpers.validateNoEmptyFields;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.hamidat.nullpointersapp.AuthActivity;
import com.hamidat.nullpointersapp.MainActivity;
import com.hamidat.nullpointersapp.R;

public class LoginFragment extends Fragment {

    public LoginFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Bind UI elements
        EditText etLoginUsername = view.findViewById(R.id.etLoginUsername);
        EditText etLoginPassword = view.findViewById(R.id.etLoginPassword);
        Button btnLogin = view.findViewById(R.id.btnLogin);
        TextView tvRegisterNow = view.findViewById(R.id.tvRegisterNow);

        // Handle Login Button Click
        btnLogin.setOnClickListener(v -> {
            giveAuthNotification(requireContext(), "Request to login received");

            // Validate input fields
            boolean noEmptyFields = validateNoEmptyFields(requireContext(), etLoginUsername, etLoginPassword);
            if (!noEmptyFields) return;

            String loginUsername = etLoginUsername.getText().toString().trim();
            String loginPassword = etLoginPassword.getText().toString().trim();

            // Attempt to login
            boolean loginSuccess = loginUser(loginUsername, loginPassword);
            if (loginSuccess) {
                // Switch to MainActivity instead of trying to load ProfileFragment inside AuthActivity
                Intent intent = new Intent(requireActivity(), MainActivity.class);
                startActivity(intent);
                requireActivity().finish(); // Close AuthActivity
            }
        });

        // Navigate to SignUpFragment instead of opening SignUpActivity
        tvRegisterNow.setOnClickListener(v -> {
            ((AuthActivity) requireActivity()).switchToFragment(new SignUpFragment());
        });

        return view;
    }

    private boolean loginUser(String username, String password) {
        // TODO - Replace with real authentication logic
        return true;
    }
}
