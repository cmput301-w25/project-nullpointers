package com.hamidat.nullpointersapp;

import static org.junit.Assert.*;

import com.hamidat.nullpointersapp.mainFragments.SearchFragment;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for search filtering logic in SearchFragment.
 */
public class SearchFragmentTest {

    private List<SearchFragment.User> mockUsers;

    @Before
    public void setUp() {
        mockUsers = new ArrayList<>();
        mockUsers.add(new SearchFragment.User("u1", "john"));
        mockUsers.add(new SearchFragment.User("u2", "Jane"));
        mockUsers.add(new SearchFragment.User("u3", "alexander"));
        mockUsers.add(new SearchFragment.User("u4", "Alina"));
        mockUsers.add(new SearchFragment.User("u5", "joey"));
        mockUsers.add(new SearchFragment.User("u6", "alice"));
        mockUsers.add(new SearchFragment.User("u7", "Jordan"));
    }

    @Test
    public void testSearchFiltersMatchingUsers_caseInsensitive() {
        List<SearchFragment.User> result = TestHelper.filterUsers(mockUsers, "jo", "u0", 6);
        assertEquals(3, result.size()); // john, joey, Jordan
        assertTrue(result.stream().anyMatch(u -> u.username.equalsIgnoreCase("john")));
        assertTrue(result.stream().anyMatch(u -> u.username.equalsIgnoreCase("joey")));
        assertTrue(result.stream().anyMatch(u -> u.username.equalsIgnoreCase("jordan")));
    }

    @Test
    public void testSearchExcludesCurrentUser() {
        List<SearchFragment.User> result = TestHelper.filterUsers(mockUsers, "jane", "u2", 6);
        assertEquals(0, result.size());
    }

    @Test
    public void testSearchRespectsMaxLimit() {
        List<SearchFragment.User> result = TestHelper.filterUsers(mockUsers, "a", "u0", 3);
        assertEquals(3, result.size());
    }
}
