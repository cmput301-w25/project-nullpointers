package com.hamidat.nullpointersapp.authFragments;

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

/**
 * Handles a user login attempt
 */
public class LoginFragment extends Fragment {

    /**
     * Required empty public constructor.
     */
    public LoginFragment() {
        // Empty constructor required for Fragment instantiation.
    }

    /**
     * Inflates the fragment layout and initializes UI components.
     *
     * @param inflater           LayoutInflater object to inflate views.
     * @param container          Parent view that the fragment's UI should attach to.
     * @param savedInstanceState Bundle containing saved state data.
     * @return The inflated view for the fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Bind UI elements
        final EditText etLoginUsername = view.findViewById(R.id.etLoginUsername);
        final EditText etLoginPassword = view.findViewById(R.id.etLoginPassword);
        final Button btnLogin = view.findViewById(R.id.btnLogin);
        final TextView tvRegisterNow = view.findViewById(R.id.tvRegisterNow);

        // Handle Login Button Click
        btnLogin.setOnClickListener(v -> {
            giveAuthNotification(requireContext(), "Request to login received");

            // Validate input fields
            boolean noEmptyFields = validateNoEmptyFields(requireContext(), etLoginUsername, etLoginPassword);
            if (!noEmptyFields) return;

            final String loginUsername = etLoginUsername.getText().toString().trim();
            final String loginPassword = etLoginPassword.getText().toString().trim();

            // Attempt to login
            boolean isLoginSuccessful = loginUser(loginUsername, loginPassword);
            if (isLoginSuccessful) {
                // Switch to MainActivity instead of trying to load ProfileFragment inside AuthActivity
                Intent intent = new Intent(requireActivity(), MainActivity.class);
                startActivity(intent);
                requireActivity().finish(); // Close AuthActivity
            }
        });

        // Navigate to SignUpFragment
        tvRegisterNow.setOnClickListener(v -> {
            ((AuthActivity) requireActivity()).switchToFragment(new SignUpFragment());
        });

        return view;
    }

    /**
     * Attempts to authenticate a user with the provided credentials.
     *
     * @param username User's username.
     * @param password User's password.
     * @return True if authentication is successful, false otherwise.
     */
    private boolean loginUser(String username, String password) {
        // TODO - Replace with real authentication logic
        return true;
    }
}
