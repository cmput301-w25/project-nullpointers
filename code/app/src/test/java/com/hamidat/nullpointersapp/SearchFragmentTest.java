package com.hamidat.nullpointersapp;

import static org.junit.Assert.*;

import com.hamidat.nullpointersapp.mainFragments.SearchFragment;
import com.hamidat.nullpointersapp.utils.testUtils.TestHelper;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for SearchFragment's user filtering logic.
 */
public class SearchFragmentTest {

    private List<SearchFragment.User> userList;

    @Before
    public void setUp() {
        userList = new ArrayList<>();
        userList.add(new SearchFragment.User("1", "Alice"));
        userList.add(new SearchFragment.User("2", "Bob"));
        userList.add(new SearchFragment.User("3", "Charlie"));
        userList.add(new SearchFragment.User("4", "alicia"));
        userList.add(new SearchFragment.User("5", "Albert"));
        userList.add(new SearchFragment.User("6", "Ali"));
        userList.add(new SearchFragment.User("7", "NotIncluded"));
    }

    @Test
    public void testFilterUsers_caseInsensitive_andLimit() {
        List<SearchFragment.User> filtered = TestHelper.filterUsers(userList, "ali", "999", 6);
        assertEquals(4, filtered.size());  // Alice, alicia, Albert, Ali
        assertEquals("Alice", filtered.get(0).username);
        assertEquals("alicia", filtered.get(1).username);
        assertEquals("Albert", filtered.get(2).username);
        assertEquals("Ali", filtered.get(3).username);
    }

    @Test
    public void testFilterUsers_excludesCurrentUser() {
        List<SearchFragment.User> filtered = TestHelper.filterUsers(userList, "ali", "1", 6);
        assertEquals(3, filtered.size());
        for (SearchFragment.User user : filtered) {
            assertNotEquals("1", user.userId);
        }
    }

    @Test
    public void testFilterUsers_emptyQueryReturnsEmptyList() {
        List<SearchFragment.User> filtered = TestHelper.filterUsers(userList, "", "1", 6);
        assertEquals(0, filtered.size());
    }
}
