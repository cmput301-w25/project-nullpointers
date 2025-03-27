/**
 * LoginFragment.java
 *
 * Fragment responsible for handling user login interactions in the authentication flow.
 * It supports standard login and a demo guest login used for testing purposes.
 *
 * <p><b>Outstanding issues:</b> None.</p>
 */

package com.hamidat.nullpointersapp.authFragments;

import static com.hamidat.nullpointersapp.utils.authUtils.AuthHelper.giveAuthNotification;
import static com.hamidat.nullpointersapp.utils.authUtils.AuthHelper.validateNoEmptyFields;

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
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;

import java.util.Map;

/**
 * Handles a user login attempt.
 */
public class LoginFragment extends Fragment {

    /**
     * Required empty public constructor.
     */
    public LoginFragment() {
        // Empty constructor required for Fragment instantiation.
    }

    /**
     * Inflates the layout for this fragment and sets up UI listeners.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate views.
     * @param container          The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState A Bundle containing the fragment's previously saved state.
     * @return The root View for the fragment's UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        final EditText etLoginUsername = rootView.findViewById(R.id.etLoginUsername);
        final EditText etLoginPassword = rootView.findViewById(R.id.etLoginPassword);
        final Button btnLogin = rootView.findViewById(R.id.btnLogin);
        final Button btnSkipAuth = rootView.findViewById(R.id.btnSkipAuthForDemo);
        final TextView tvRegisterNow = rootView.findViewById(R.id.tvRegisterNow);

        btnSkipAuth.setOnClickListener( v -> {
            giveAuthNotification(requireContext(), "Request to login as a guest user received");

            // this is only for ease of demo and testing
            // if they're not using auth, just log them in as the guest user
            final String loginUsername = "guestUser";
            final String loginPassword = "123";

            FirestoreHelper firestoreHelper = new FirestoreHelper();
            firestoreHelper.getUserByUsername(loginUsername, new FirestoreHelper.FirestoreCallback() {
                @Override
                public void onSuccess(Object result) {
                    Map<String, Object> userData = (Map<String, Object>) result;
                    String storedPassword = (String) userData.get("password");
                    String userId = (String) userData.get("userId"); // The document ID

                    if (storedPassword != null && storedPassword.equals(loginPassword)) {
                        final Intent intent = new Intent(requireActivity(), MainActivity.class);
                        // Pass the userId of the currently logged in user as an extra
                        intent.putExtra("USER_ID", userId);
                        startActivity(intent);
                        requireActivity().finish();
                    } else {
                        giveAuthNotification(requireContext(), "Incorrect password");
                    }
                }
                @Override
                public void onFailure(Exception e) {
                    giveAuthNotification(requireContext(), "Login failed: " + e.getMessage());
                }
            });
        });


        btnLogin.setOnClickListener(v -> {
            giveAuthNotification(requireContext(), "Request to login received");

            // Validate input fields
            boolean noEmptyFields = validateNoEmptyFields(requireContext(), etLoginUsername, etLoginPassword);
            if (!noEmptyFields) return;

            final String loginUsername = etLoginUsername.getText().toString().trim();
            final String loginPassword = etLoginPassword.getText().toString().trim();

            FirestoreHelper firestoreHelper = new FirestoreHelper();
            firestoreHelper.getUserByUsername(loginUsername, new FirestoreHelper.FirestoreCallback() {
                @Override
                public void onSuccess(Object result) {
                    Map<String, Object> userData = (Map<String, Object>) result;
                    String storedPassword = (String) userData.get("password");
                    String userId = (String) userData.get("userId"); // The document ID


                    if (storedPassword != null && storedPassword.equals(loginPassword)) {
                        final Intent intent = new Intent(requireActivity(), MainActivity.class);
                        // Pass the userId of the currently logged in user as an extra
                        intent.putExtra("USER_ID", userId);
                        startActivity(intent);
                        requireActivity().finish();
                    } else {
                        giveAuthNotification(requireContext(), "Incorrect password");
                    }
                }
                @Override
                public void onFailure(Exception e) {
                    giveAuthNotification(requireContext(), "Login failed: " + e.getMessage());
                }
            });
        });

        tvRegisterNow.setOnClickListener(v ->
                ((AuthActivity) requireActivity()).switchToFragment(new SignUpFragment())
        );

        return rootView;
    }
}
