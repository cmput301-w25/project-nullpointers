package com.hamidat.nullpointersapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.hamidat.nullpointersapp.AuthActivity;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.utils.AuthHelpers;

public class SignUpFragment extends Fragment {

    public SignUpFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        // Bind UI elements
        EditText etSignupPassword = view.findViewById(R.id.etSignUpPassword);
        EditText etSignupUsername = view.findViewById(R.id.etSignupUsername);
        Button signUpButton = view.findViewById(R.id.btnSignUp);
        TextView alreadyAMemberLink = view.findViewById(R.id.tvAlreadyMember);

        // Handle Sign Up Button Click
        signUpButton.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "SignUp Request Received", Toast.LENGTH_SHORT).show();

            String signupUsername = etSignupUsername.getText().toString().trim();
            String signUpPassword = etSignupPassword.getText().toString().trim();

            // Validate fields
            if (!AuthHelpers.validateNoEmptyFields(requireContext(), etSignupUsername, etSignupPassword)) return;
            if (!AuthHelpers.validateUniqueUsername(signupUsername)) return;

            // Add valid user to DB
            boolean signupSuccess = AuthHelpers.addNewUserToDB(requireContext(), signupUsername, signUpPassword);
            if (signupSuccess) {
                // Switch back to LoginFragment after successful signup
                ((AuthActivity) requireActivity()).switchToFragment(new LoginFragment());
            }
        });

        // Navigate back to LoginFragment
        alreadyAMemberLink.setOnClickListener(v -> {
            ((AuthActivity) requireActivity()).switchToFragment(new LoginFragment());
        });

        return view;
    }
}
