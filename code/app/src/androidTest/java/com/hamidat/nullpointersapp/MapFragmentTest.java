package com.hamidat.nullpointersapp;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.gms.maps.model.LatLng;
import com.hamidat.nullpointersapp.mainFragments.MapFragment;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.models.moodHistory;
import com.hamidat.nullpointersapp.utils.firebaseUtils.FirestoreHelper;
import com.hamidat.nullpointersapp.utils.mapUtils.MoodClusterItem;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Instrumented tests for MapFragment.
 *
 * This test class uses FragmentScenario to launch MapFragment in isolation and uses a
 * real Context from the device/emulator via InstrumentationRegistry. It uses Mockito
 * to stub FirestoreHelper asynchronous calls.
 *
 * Note: Methods like updateMapData, showInfoWindow, and slideDownInfoWindow are assumed
 * to have package‑private visibility so they can be accessed from tests.
 */
@RunWith(AndroidJUnit4.class)
public class MapFragmentTest {

    private MapFragment mapFragment;
    private FirestoreHelper mockFirestoreHelper;
    private final String testUserId = "user123";
    private Context context;

    @Before
    public void setUp() {
        // Obtain a real Context from the instrumentation.
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // Launch MapFragment using FragmentScenario.
        FragmentScenario<MapFragment> scenario = FragmentScenario.launchInContainer(MapFragment.class);
        scenario.onFragment(fragment -> {
            mapFragment = fragment;
            // Inject a mock FirestoreHelper
            mockFirestoreHelper = mock(FirestoreHelper.class);
            mapFragment.firestoreHelper = mockFirestoreHelper;
            mapFragment.currentUserId = testUserId;
        });
    }

    @Test
    public void testFragmentInflatesLayout() {
        // Ensure the fragment's view is not null.
        View view = mapFragment.getView();
        assertNotNull("Fragment view should not be null", view);
        // Check that the info window exists.
        View infoWindow = view.findViewById(R.id.info_window);
        assertNotNull("Info window should be present in the layout", infoWindow);
    }

//    @Test
//    public void testUpdateMapDataWithValidMoods() {
//        // Prepare a dummy mood history with one valid Mood.
//        moodHistory testHistory = new moodHistory();
//        ArrayList<Mood> moods = new ArrayList<>();
//
//        Mood testMood = new Mood();
//        testMood.setLatitude(37.7749);
//        testMood.setLongitude(-122.4194);
//        testMood.setTimestamp(new com.google.firebase.Timestamp(new Date()));
//        testMood.setMood("Happy");
//        testMood.setMoodDescription("Test mood description");
//        testMood.setSocialSituation("Alone");
//        testMood.setImageBase64("dummyBase64"); // Dummy value for testing.
//        testMood.setUserId(testUserId);
//        moods.add(testMood);
//        testHistory.setMoodArray(moods);
//
//        // Call updateMapData (assumed package‑private for testing).
//        mapFragment.updateMapData(testHistory);
//
//        // Verify that the fragment's internal list has been updated.
//        assertNotNull("allDummyItems should not be null", mapFragment.allDummyItems);
//        assertEquals("There should be one MoodClusterItem", 1, mapFragment.allDummyItems.size());
//    }
//
//    @Test
//    public void testShowInfoWindowDisplaysCorrectData() {
//        // Create a dummy MoodClusterItem with test data.
//        MoodClusterItem dummyItem = new MoodClusterItem(
//                new LatLng(37.4219983, -122.084),
//                "Happy",
//                "2025-03-03",
//                "12:00 PM",
//                "Feeling great!",
//                "Alone",
//                "dummyBase64",
//                testUserId
//        );
//        // Stub FirestoreHelper.getUser to immediately return dummy user data.
//        doAnswer(invocation -> {
//            FirestoreHelper.FirestoreCallback callback = invocation.getArgument(1);
//            Map<String, Object> dummyUserData = new HashMap<>();
//            dummyUserData.put("username", "TestUser");
//            callback.onSuccess(dummyUserData);
//            return null;
//        }).when(mockFirestoreHelper).getUser(anyString(), any(FirestoreHelper.FirestoreCallback.class));
//
//        // Call showInfoWindow (assumed package‑private).
//        mapFragment.showInfoWindow(dummyItem);
//
//        // Wait for asynchronous operations to complete.
//        mapFragment.getView().postDelayed(() -> {
//            View infoWindow = mapFragment.getView().findViewById(R.id.info_window);
//            assertEquals("Info window should be visible", View.VISIBLE, infoWindow.getVisibility());
//
//            TextView tvUsername = infoWindow.findViewById(R.id.tvUsername);
//            assertEquals("TestUser", tvUsername.getText().toString());
//
//            TextView tvTime = infoWindow.findViewById(R.id.tvTime);
//            assertEquals("12:00 PM", tvTime.getText().toString());
//
//            // Check custom labels for emotion and social situation.
//            TextView tvEmotion = infoWindow.findViewById(R.id.tvEmotion);
//            assertTrue("Emotion field should contain 'is feeling:'",
//                    tvEmotion.getText().toString().contains("is feeling:"));
//
//            TextView tvSocialSituation = infoWindow.findViewById(R.id.tvSocialSituation);
//            assertTrue("Social situation field should contain 'Social Situation:'",
//                    tvSocialSituation.getText().toString().contains("Social Situation:"));
//        }, 500);
//    }
//
//    @Test
//    public void testSlideDownInfoWindowAnimatesOffScreen() {
//        // Create a dummy MoodClusterItem.
//        MoodClusterItem dummyItem = new MoodClusterItem(
//                new LatLng(37.4219983, -122.084),
//                "Happy",
//                "2025-03-03",
//                "12:00 PM",
//                "Feeling great!",
//                "Alone",
//                "dummyBase64",
//                testUserId
//        );
//        doAnswer(invocation -> {
//            FirestoreHelper.FirestoreCallback callback = invocation.getArgument(1);
//            Map<String, Object> dummyUserData = new HashMap<>();
//            dummyUserData.put("username", "TestUser");
//            callback.onSuccess(dummyUserData);
//            return null;
//        }).when(mockFirestoreHelper).getUser(anyString(), any(FirestoreHelper.FirestoreCallback.class));
//
//        // Show the info window.
//        mapFragment.showInfoWindow(dummyItem);
//        // Now simulate the slide-down animation.
//        mapFragment.slideDownInfoWindow();
//
//        // Wait for the animation to complete.
//        mapFragment.getView().postDelayed(() -> {
//            View infoWindow = mapFragment.getView().findViewById(R.id.info_window);
//            assertEquals("After sliding down, info window should be GONE", View.GONE, infoWindow.getVisibility());
//        }, 500);
//    }
}
