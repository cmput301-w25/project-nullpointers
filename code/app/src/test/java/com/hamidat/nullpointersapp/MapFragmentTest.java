package com.hamidat.nullpointersapp;

import static org.junit.Assert.assertEquals;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;
import com.hamidat.nullpointersapp.mainFragments.MapFragment;
import com.hamidat.nullpointersapp.models.Mood;
import com.hamidat.nullpointersapp.models.moodHistory;
import com.hamidat.nullpointersapp.utils.mapUtils.MoodClusterItem;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * MapFragmentTest
 *
 * Tests the filtering logic in MapFragment by invoking its private methods via reflection.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class MapFragmentTest {

    // A fake ClusterManager to capture items for testing.
    private static class FakeClusterManager extends com.google.maps.android.clustering.ClusterManager<MoodClusterItem> {
        public List<MoodClusterItem> items = new ArrayList<>();

        public FakeClusterManager() {
            super(null, null); // context and map are not used in this fake.
        }

        @Override
        public void clearItems() {
            items.clear();
        }

//        @Override
//        public void addItems(Iterable<MoodClusterItem> items) {
//            for (MoodClusterItem item : items) {
//                this.items.add(item);
//            }
//        }

        @Override
        public void cluster() {
            // No-op for testing.
        }
    }

    private MapFragment mapFragment;

    @Before
    public void setUp() throws Exception {
        // Instantiate the MapFragment.
        mapFragment = new MapFragment();
        // Set a dummy current user ID.
        mapFragment.currentUserId = "testUser";

        // Use reflection to set the private clusterManager field to our FakeClusterManager.
        Field clusterField = MapFragment.class.getDeclaredField("clusterManager");
        clusterField.setAccessible(true);
        clusterField.set(mapFragment, new FakeClusterManager());

        // Initialize the private selectedMoods field.
        Field selectedMoodsField = MapFragment.class.getDeclaredField("selectedMoods");
        selectedMoodsField.setAccessible(true);
        selectedMoodsField.set(mapFragment, new HashSet<String>());
    }

    @Test
    public void testUpdateMapData_filtersOutDetachedLocations() throws Exception {
        // Create one mood with valid location and one with detached location (0,0).
        Mood validMood = new Mood("Happy", "Valid mood", 12.34, 56.78, "Alone", "user1", false);
        validMood.setTimestamp(new Timestamp(new Date()));

        Mood detachedMood = new Mood("Sad", "Detached mood", 0.0, 0.0, "Alone", "user1", false);
        detachedMood.setTimestamp(new Timestamp(new Date()));

        // Prepare a moodHistory object containing both moods.
        moodHistory history = new moodHistory();
        List<Mood> moodList = new ArrayList<>();
        moodList.add(validMood);
        moodList.add(detachedMood);
        history.setMoodArray((ArrayList<Mood>) moodList);

        // Call the private updateMapData(moodHistory) method via reflection.
        Method updateMethod = MapFragment.class.getDeclaredMethod("updateMapData", moodHistory.class);
        updateMethod.setAccessible(true);
        updateMethod.invoke(mapFragment, history);

        // Retrieve the FakeClusterManager instance.
        Field clusterField = MapFragment.class.getDeclaredField("clusterManager");
        clusterField.setAccessible(true);
        FakeClusterManager fakeManager = (FakeClusterManager) clusterField.get(mapFragment);

        // Only the valid mood should be added.
        assertEquals("Only one mood with valid coordinates should be added", 1, fakeManager.items.size());
        MoodClusterItem item = fakeManager.items.get(0);
        assertEquals(12.34, item.getPosition().latitude, 0.001);
        assertEquals(56.78, item.getPosition().longitude, 0.001);
    }

    @Test
    public void testFilterAndDisplayMoodEventsAsync_filtersByProximityAndEmotion() throws Exception {
        // Create two MoodClusterItems: one "Happy" near the current location, and one "Sad" far away.
        String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        MoodClusterItem item1 = new MoodClusterItem(new LatLng(10.0, 10.0), "Happy", todayStr,
                "10:00 AM", "desc1", "Alone", null, "user1");
        MoodClusterItem item2 = new MoodClusterItem(new LatLng(50.0, 50.0), "Sad", todayStr,
                "11:00 AM", "desc2", "Alone", null, "user2");

        // Set the private allDummyItems field to contain both items.
        Field allDummyItemsField = MapFragment.class.getDeclaredField("allDummyItems");
        allDummyItemsField.setAccessible(true);
        List<MoodClusterItem> allItems = new ArrayList<>();
        allItems.add(item1);
        allItems.add(item2);
        allDummyItemsField.set(mapFragment, allItems);

        // Set the private currentLocation field to a location near item1.
        Field currentLocationField = MapFragment.class.getDeclaredField("currentLocation");
        currentLocationField.setAccessible(true);
        LatLng currentLocation = new LatLng(10.0, 10.0);
        currentLocationField.set(mapFragment, currentLocation);

        // Set the private selectedMoods field to include only "Happy".
        Field selectedMoodsField = MapFragment.class.getDeclaredField("selectedMoods");
        selectedMoodsField.setAccessible(true);
        Set<String> selectedMoods = new HashSet<>();
        selectedMoods.add("Happy");
        selectedMoodsField.set(mapFragment, selectedMoods);

        // Replace the clusterManager with a fresh FakeClusterManager.
        Field clusterField = MapFragment.class.getDeclaredField("clusterManager");
        clusterField.setAccessible(true);
        FakeClusterManager fakeManager = new FakeClusterManager();
        clusterField.set(mapFragment, fakeManager);

        // Call the private filterAndDisplayMoodEventsAsync(boolean, LatLng) method via reflection.
        Method filterMethod = MapFragment.class.getDeclaredMethod("filterAndDisplayMoodEventsAsync", boolean.class, LatLng.class);
        filterMethod.setAccessible(true);
        filterMethod.invoke(mapFragment, true, currentLocation);

        // Wait slightly longer than the 300ms post delay.
        Thread.sleep(350);

        // Only the nearby "Happy" item should pass the filter.
        assertEquals("Only one mood item should pass the filter", 1, fakeManager.items.size());
        MoodClusterItem filteredItem = fakeManager.items.get(0);
        assertEquals("Happy", filteredItem.getEmotion());
    }
}
