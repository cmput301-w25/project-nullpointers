/**
 * HomeFeedFragment.java
 *
 * Displays a scrollable list of mood posts from the current user and followed users.
 * Allows viewing mood details, opening a comments dialog, and applying filters.
 * Integrates with Firestore for real-time mood and comment updates.
 *
 * <p><b>Outstanding issues:</b> None.</p>
 */

package com.hamidat.nullpointersapp.mainFragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hamidat.nullpointersapp.MainActivity;
import com.hamidat.nullpointersapp.R;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.models.MoodAdapter;
import com.hamidat.nullpointersapp.models.moodHistory;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;
import com.hamidat.nullpointersapp.utils.homeFeedUtils.CommentsBottomSheetFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fragment that displays a feed of mood posts from the current user and their followed users.
 * It fetches the mood data from Firestore, displays it in a RecyclerView, and handles
 * user interactions such as viewing mood details, commenting, and filtering.
 */
public class HomeFeedFragment extends Fragment {

    private RecyclerView rvMoodList;

    private Button buttonFollowing;
    private MoodAdapter moodAdapter;
    private ArrayList<Mood> allMoods = new ArrayList<>();
    private FirestoreHelper firestoreHelper;
    private String currentUserId;

    // Adding all the storage for HomeFilterHistoryFragment for data persistence
    private Timestamp savedFromTimestamp = null;
    private Timestamp savedToTimestamp = null;
    private String savedDescription = "";
    private List<String> savedCheckedEmotions = new ArrayList<>();

    private boolean savedToggleWeek;
    private boolean savedToggleAscending;

    /**
     * Inflates the layout for this fragment.
     *
     * @param inflater           LayoutInflater object that can be used to inflate views.
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The root View for the fragment's UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate your home feed layout
        View view = inflater.inflate(R.layout.full_mood_event, container, false);
        rvMoodList = view.findViewById(R.id.rvMoodList);
        rvMoodList.setLayoutManager(new LinearLayoutManager(getContext()));

        buttonFollowing = view.findViewById(R.id.tvFollowing);

        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            firestoreHelper = mainActivity.getFirestoreHelper();
            currentUserId = mainActivity.getCurrentUserId();
        }

        if (currentUserId != null) {
            moodAdapter = new MoodAdapter(allMoods, currentUserId, (AppCompatActivity) getActivity());
            rvMoodList.setAdapter(moodAdapter);
        } else {
            Toast.makeText(getContext(), "Error: User ID is null. Restart app.", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    /**
     * Called immediately after {@link #onCreateView}.
     *
     * @param view               The View returned by {@link #onCreateView}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Get Firestore helper and current user ID from MainActivity.
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            firestoreHelper = mainActivity.getFirestoreHelper();
            currentUserId = mainActivity.getCurrentUserId();
        }

        // Query mood events from current user and their followed users.
        fetchMoodData();

        // Attach listener to each child view to handle Comments button clicks.
        rvMoodList.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            /**
             * Called when a child view is attached to the window.
             *
             * @param view The view which has been attached.
             */
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {
                Button btnComment = view.findViewById(R.id.btnComment);
                if (btnComment != null) {
                    btnComment.setOnClickListener(v -> {
                        int pos = rvMoodList.getChildAdapterPosition(view);
                        if (pos != RecyclerView.NO_POSITION) {
                            Mood mood = allMoods.get(pos);
                            openCommentsDialog(mood);
                        }
                    });
                }
            }

            /**
             * Called when a child view is detached from the window.
             *
             * @param view The view which has been detached.
             */
            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {
            }
        });

        // buttonFollowing is the button which displays the HomeFilterHistoryFragment for filtering moods in HomeFeed.
        buttonFollowing.setOnClickListener(v -> {
            HomeFilterHistoryFragment filterFragment = new HomeFilterHistoryFragment(currentUserId, firestoreHelper, savedToTimestamp, savedFromTimestamp, savedDescription, savedCheckedEmotions, savedToggleWeek, savedToggleAscending, new HomeFilterHistoryFragment.MoodFilterCallback() {
                /**
                 * Called when mood filter is applied
                 * @param filteredMoods List of moods that match the applied filters.
                 * @param savingTo Timestamp representing the end time of the filter range.
                 * @param savingFrom Timestamp representing the start time of the filter range.
                 * @param savingDescription String representing the filter description.
                 * @param savingEmotions List of strings representing selected emotions.
                 * @param setToggleWeek Boolean representing if week toggle is set.
                 * @param setOrder Boolean representing the order of mood display.
                 */
                @Override
                public void onMoodFilterApplied(List<Mood> filteredMoods, Timestamp savingTo, Timestamp savingFrom, String savingDescription, List<String> savingEmotions, boolean setToggleWeek, boolean setOrder) {
                    allMoods.clear();
                    allMoods.addAll(filteredMoods);

                    moodAdapter.notifyDataSetChanged();

                    savedToTimestamp = savingTo;
                    savedFromTimestamp = savingFrom;
                    savedDescription = savingDescription;
                    savedCheckedEmotions = savingEmotions;
                    savedToggleWeek = setToggleWeek;
                    savedToggleAscending = setOrder;
                }

                /**
                 * Called when a toast message needs to be displayed.
                 * @param message String representing the message to be displayed in the toast.
                 */
                @Override
                public void onShowToast(String message) {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                }
            });
            filterFragment.show(getChildFragmentManager(), "FilterMoodsSheet");
        });
    }

    /**
     * Fetches mood data from Firestore for the current user and their followed users.
     */
    private void fetchMoodData() {
        if (currentUserId == null || firestoreHelper == null) return;

        firestoreHelper.getUser(currentUserId, new FirestoreHelper.FirestoreCallback() {
            /**
             * Called when the operation succeeds.
             *
             * @param result The result of the operation.
             */
            @Override
            public void onSuccess(Object result) {
                if (result instanceof Map) {
                    Map<String, Object> userData = (Map<String, Object>) result;
                    ArrayList<String> followingIds = (ArrayList<String>) userData.get("following");
                    if (followingIds == null) {
                        followingIds = new ArrayList<>();
                    }
                    // Include current user to see their own moods
                    if (!followingIds.contains(currentUserId)) {
                        followingIds.add(currentUserId);
                    }
                    // Query mood events for these user IDs.
                    firestoreHelper.firebaseToMoodHistory(followingIds, new FirestoreHelper.FirestoreCallback() {
                        /**
                         * Called when the operation succeeds.
                         *
                         * @param result The result of the operation.
                         */
                        @Override
                        public void onSuccess(Object result) {
                            moodHistory history = (moodHistory) result;
                            allMoods.clear();

                            // Filter moods: show public moods and user's own private moods
                            for (Mood mood : history.getMoodArray()) {
                                if (!mood.isPrivate() || mood.getUserId().equals(currentUserId)) {
                                    allMoods.add(mood);
                                }
                            }

                            // Sort moods by newest first
                            java.util.Collections.sort(allMoods, (m1, m2) -> {
                                if (m1.getTimestamp() == null || m2.getTimestamp() == null) {
                                    return 0;
                                }
                                return m2.getTimestamp().compareTo(m1.getTimestamp());
                            });

                            Log.d("HomeFeedFragment", "onSuccess: Mood history fetched, updating UI...");

                            Activity activity = getActivity();
                            if (activity != null && isAdded()) {
                                activity.runOnUiThread(() -> {
                                    Log.d("HomeFeedFragment", "Running notifyDataSetChanged() on UI thread");
                                    moodAdapter.notifyDataSetChanged();
                                });
                            } else {
                                Log.w("HomeFeedFragment", "Fragment not attached, skipping UI update");
                            }
                        }

                        /**
                         * Called when the operation fails.
                         *
                         * @param e The exception that occurred.
                         */
                        @Override
                        public void onFailure(Exception e) {
                            runOnUiThreadIfAttached(() -> {
                                Log.e("HomeFeedFragment", "Error loading moods: " + e.getMessage());
                                Toast.makeText(getContext(), "Error loading moods: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                }
                Log.d("FirestoreDebug", " onSuccess: Firestore returned updated mood list");
            }

            /**
             * Called when the operation fails.
             *
             * @param e The exception that occurred.
             */
            @Override
            public void onFailure(Exception e) {
                runOnUiThreadIfAttached(() -> {
                    Log.e("HomeFeedFragment", "Error fetching user data: " + e.getMessage());
                    Toast.makeText(getContext(), "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * Opens a dialog for viewing and adding comments for the given mood event.
     *
     * @param mood The mood event to open the comments dialog for.
     */
    private void openCommentsDialog(Mood mood) {
        if (mood.getMoodId() == null) {
            Toast.makeText(getContext(), "Cannot load comments: mood id is missing", Toast.LENGTH_SHORT).show();
            return;
        }
        CommentsBottomSheetFragment bottomSheet = CommentsBottomSheetFragment.newInstance(mood.getMoodId(), currentUserId);

        // Update just the comment count
        bottomSheet.setOnDismissListener(() -> {
            FirebaseFirestore.getInstance()
                    .collection("moods")
                    .document(mood.getMoodId())
                    .get()
                    .addOnSuccessListener(doc -> {
                        Long updatedCount = doc.getLong("commentCount");
                        if (updatedCount != null) {
                            mood.setCommentCount(updatedCount.intValue());
                            int index = allMoods.indexOf(mood);
                            if (index != -1) {
                                moodAdapter.notifyItemChanged(index, "commentOnly");
                            }
                        }
                    });
        });

        bottomSheet.show(getChildFragmentManager(), "CommentsBottomSheet");
    }

    /**
     * Runs the given action on the UI thread if the fragment is attached to an activity.
     *
     * @param action The action to run on the UI thread.
     */
    private void runOnUiThreadIfAttached(Runnable action) {
        Activity activity = getActivity();
        if (activity != null && isAdded()) {
            activity.runOnUiThread(action);
        }
    }
}