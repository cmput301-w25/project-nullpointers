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
import com.hamidat.nullpointersapp.AuthActivity;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.utils.authUtils.AuthHelper;
import com.hamidat.nullpointersapp.utils.authUtils.AuthHelper.UniqueUsernameCallback;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;

/**
 * Fragment for handling user sign up.
 */
public class SignUpFragment extends Fragment {

    /**
     * Required empty public constructor.
     */
    public SignUpFragment() {
        // Empty constructor required for Fragment instantiation.
    }

    /**
     * Inflates the layout for this fragment and sets up UI listeners.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate views.
     * @param container          The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState A Bundle object containing the fragment's previously saved state.
     * @return The root View for the fragment's UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_signup, container, false);

        final EditText etSignupUsername = rootView.findViewById(R.id.etSignupUsername);
        final EditText etSignupPassword = rootView.findViewById(R.id.etSignUpPassword);
        final Button signUpButton = rootView.findViewById(R.id.btnSignUp);
        final TextView alreadyAMemberLink = rootView.findViewById(R.id.tvAlreadyMember);

        signUpButton.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "SignUp Request Received", Toast.LENGTH_SHORT).show();

            final String signupUsername = etSignupUsername.getText().toString().trim();
            final String signUpPassword = etSignupPassword.getText().toString().trim();

            if (!AuthHelper.validateNoEmptyFields(requireContext(), etSignupUsername, etSignupPassword)) {
                return;
            }

            // Check that the username is unique
            AuthHelper.validateUniqueUsername(requireContext(), signupUsername, new UniqueUsernameCallback() {
                @Override
                public void onResult(boolean isUnique) {
                    if (!isUnique) {
                        // Tell the user they need to make a unique username
                        AuthHelper.giveAuthNotification(requireContext(), "Username already exists");
                        return;
                    }
                    // If unique, add the new user to Firestore via AuthHelper
                    AuthHelper.addNewUserToDB(requireContext(), signupUsername, signUpPassword, new FirestoreHelper.FirestoreCallback() {
                        @Override
                        public void onSuccess(Object result) {
                            // If signup was successful, notify the user
                            AuthHelper.giveAuthNotification(requireContext(), "You have been successfully registered");
                            // Move to the loginFragment so the newly signedUp user can login
                            ((AuthActivity) requireActivity()).switchToFragment(new LoginFragment());
                        }
                        @Override
                        public void onFailure(Exception e) {
                            // If the user signUp wasn't successful, then tell the user
                            AuthHelper.giveAuthNotification(requireContext(), "Registration failed: " + e.getMessage());
                        }
                    });
                }
            });
        });

        alreadyAMemberLink.setOnClickListener(v ->
                ((AuthActivity) requireActivity()).switchToFragment(new LoginFragment())
        );

        return rootView;
    }
}
