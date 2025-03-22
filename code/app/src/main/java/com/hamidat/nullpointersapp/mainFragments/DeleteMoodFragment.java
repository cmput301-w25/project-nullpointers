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
import com.google.firebase.firestore.FirebaseFirestore;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreDeleteMood;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;

/**
 * Fragment for deleting a mood post.
 *
 * <p>This fragment uses the item_mood_card.xml layout (which includes the delete button)
 * to provide a UI for deletion. It receives the mood's unique ID and owner ID as arguments,
 * constructs a minimal Mood object, and calls the Firestore deletion utility.
 * Ownership or sign‐in checks are removed for testing purposes.</p>
 */
public class DeleteMoodFragment extends Fragment {

    // Keys for arguments
    public static final String ARG_MOOD_ID = "argMoodId";
    public static final String ARG_OWNER_ID = "argOwnerId";


    private String moodId;
    private String ownerId;

    private Mood moodToDelete;

    /**
     * Default constructor.
     */
    public DeleteMoodFragment() {
        // Required empty public constructor.
    }

    /**
     * Creates a new instance of DeleteMoodFragment with the provided mood ID and owner ID.
     *
     * @param moodId  The unique identifier of the mood.
     * @param ownerId The user ID of the mood's owner.
     * @return A new instance of DeleteMoodFragment.
     */
    public static DeleteMoodFragment newInstance(String moodId, String ownerId) {
        DeleteMoodFragment fragment = new DeleteMoodFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MOOD_ID, moodId);
        args.putString(ARG_OWNER_ID, ownerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the mood ID and owner ID from arguments.
        if (getArguments() != null) {
            moodId = getArguments().getString(ARG_MOOD_ID);
            ownerId = getArguments().getString(ARG_OWNER_ID);
        }
        // Create a minimal Mood object for deletion.
        moodToDelete = new Mood();
        moodToDelete.setMoodId(moodId);
        moodToDelete.setUserId(ownerId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the item_mood_card.xml layout for this fragment.
        return inflater.inflate(R.layout.dialog_mood_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Locate the delete button in dialog modal
        Button btnDelete = view.findViewById(R.id.btnDialogDelete);

        btnDelete.setOnClickListener(v -> {
            // Ensure we have a valid mood ID.
            if (moodId == null || moodId.isEmpty()) {
                Toast.makeText(getActivity(), "No mood available for deletion.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            // Proceed with deletion using FirestoreDeleteMood.
            FirestoreDeleteMood deleteUtil = new FirestoreDeleteMood(FirebaseFirestore.getInstance());
            deleteUtil.deleteMood(ownerId, moodToDelete, new FirestoreHelper.FirestoreCallback() {
                @Override
                public void onSuccess(Object result) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getActivity(), "Mood deleted successfully.",
                                        Toast.LENGTH_SHORT).show());
                    }
                    // For testing, simply navigate back.
                    if (getActivity() != null) {
                        getActivity().onBackPressed();
                    }
                }
                @Override
                public void onFailure(Exception e) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getActivity(), "Error: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show());
                    }
                }
            });
        });
    }
}
