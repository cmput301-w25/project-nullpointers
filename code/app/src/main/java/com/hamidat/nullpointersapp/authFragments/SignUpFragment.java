package com.hamidat.nullpointersapp.authFragments;

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

import com.google.firebase.Firebase;
import com.hamidat.nullpointersapp.AuthActivity;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.utils.authUtils.AuthHelpers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import androidx.annotation.NonNull;

/**
 * Handles a user sign-up attempt.
 */
public class SignUpFragment extends Fragment {

    /**
     * Required empty public constructor.
     */
    public SignUpFragment() {
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_signup, container, false);

        // Bind UI elements
        final EditText etSignupUsername = view.findViewById(R.id.etSignupUsername);
        final EditText etSignupPassword = view.findViewById(R.id.etSignUpPassword);
        final Button signUpButton = view.findViewById(R.id.btnSignUp);
        final TextView alreadyAMemberLink = view.findViewById(R.id.tvAlreadyMember);

        // Firestore Auth creation
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Handle Sign Up Button Click
        signUpButton.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "SignUp Request Received", Toast.LENGTH_SHORT).show();

            final String signupUsername = etSignupUsername.getText().toString().trim();
            final String signUpPassword = etSignupPassword.getText().toString().trim();

            // Validate fields
            if (!AuthHelpers.validateNoEmptyFields(requireContext(), etSignupUsername, etSignupPassword)) {
                return;
            }
            if (!AuthHelpers.validateUniqueUsername(signupUsername)) {
                return;
            }

            // Add valid user to DB
            boolean isSignUpSuccessful = AuthHelpers.addNewUserToDB(requireContext(), signupUsername, signUpPassword, auth, firestore);
            if (isSignUpSuccessful) {
                // Switch back to LoginFragment after successful signup
                ((AuthActivity) requireActivity()).switchToFragment(new LoginFragment());
            }
        });

        // Navigate back to LoginFragment so the newly signed up user can login
        alreadyAMemberLink.setOnClickListener(v ->
                ((AuthActivity) requireActivity()).switchToFragment(new LoginFragment())
        );

        return view;
    }
}
