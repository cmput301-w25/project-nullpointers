package com.hamidat.nullpointersapp;

import static org.junit.Assert.assertEquals;

import com.google.android.gms.maps.model.LatLng;
import com.hamidat.nullpointersapp.utils.mapUtils.MoodClusterItem;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Unit tests for verifying map mood filtering logic using TestHelper.
 */
public class MapFragmentTest {

    /**
     * Tests that only moods within 5km and matching emotion are returned.
     */
    @Test
    public void testFilter_byProximityAndEmotion() throws Exception {
        // Setup dummy mood data
        String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        MoodClusterItem item1 = new MoodClusterItem(new LatLng(10.0, 10.0), "Happy", todayStr,
                "10:00 AM", "desc1", "Alone", null, "user1");

        MoodClusterItem item2 = new MoodClusterItem(new LatLng(50.0, 50.0), "Sad", todayStr,
                "11:00 AM", "desc2", "Alone", null, "user2");

        List<MoodClusterItem> allItems = Arrays.asList(item1, item2);
        Set<String> selectedMoods = new HashSet<>(Collections.singletonList("Happy"));
        LatLng currentLocation = new LatLng(10.0, 10.0);

        // Call filter
        List<MoodClusterItem> result = TestHelper.filterMoodItems(
                allItems,
                selectedMoods,
                null,
                null,
                currentLocation,
                true
        );

        assertEquals(1, result.size());
        assertEquals("Happy", result.get(0).getEmotion());
    }

    /**
     * Tests that moods outside of the date range are excluded.
     */
    @Test
    public void testFilter_byDateRange() throws Exception {
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();

        calendar.add(Calendar.DAY_OF_YEAR, -10);
        Date oldDate = calendar.getTime();

        String oldDateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(oldDate);
        String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(today);

        MoodClusterItem oldMood = new MoodClusterItem(new LatLng(10.0, 10.0), "Happy", oldDateStr,
                "10:00 AM", "desc", "Alone", null, "user");

        MoodClusterItem recentMood = new MoodClusterItem(new LatLng(10.0, 10.0), "Happy", todayStr,
                "11:00 AM", "desc", "Alone", null, "user");

        List<MoodClusterItem> allItems = Arrays.asList(oldMood, recentMood);
        Set<String> selectedMoods = new HashSet<>(Collections.singletonList("Happy"));

        calendar = Calendar.getInstance();
        Date to = calendar.getTime(); // today
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        Date from = calendar.getTime(); // 7 days ago

        List<MoodClusterItem> result = TestHelper.filterMoodItems(
                allItems,
                selectedMoods,
                from,
                to,
                null,
                false
        );

        assertEquals(1, result.size());
        assertEquals(todayStr, result.get(0).getDate());
    }

    /**
     * Tests that proximity filter is ignored when `filterNearby` is false.
     */
    @Test
    public void testFilter_ignoresProximityWhenDisabled() throws Exception {
        String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        MoodClusterItem farMood = new MoodClusterItem(new LatLng(50.0, 50.0), "Happy", todayStr,
                "11:00 AM", "desc", "Alone", null, "user");

        List<MoodClusterItem> allItems = Collections.singletonList(farMood);
        Set<String> selectedMoods = new HashSet<>(Collections.singletonList("Happy"));

        LatLng currentLocation = new LatLng(10.0, 10.0); // far away

        List<MoodClusterItem> result = TestHelper.filterMoodItems(
                allItems,
                selectedMoods,
                null,
                null,
                currentLocation,
                false // filterNearby disabled
        );

        assertEquals(1, result.size());
        assertEquals("Happy", result.get(0).getEmotion());
    }
}
