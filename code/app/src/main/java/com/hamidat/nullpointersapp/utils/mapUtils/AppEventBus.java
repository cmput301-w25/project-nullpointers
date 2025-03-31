/**
 * AppEventBus.java
 *
 * Provides a wrapper for the EventBus instance.
 *
 * Outstanding Issues: None
 */

package com.hamidat.nullpointersapp.utils.mapUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * Provides a wrapper for the EventBus instance.
 */
public class AppEventBus {
    private static final EventBus instance = EventBus.getDefault();

    /**
     * Returns the singleton instance of EventBus.
     *
     * @return The EventBus instance.
     */
    public static EventBus getInstance() {
        return instance;
    }

    /**
     * Event class to signal that a mood has been added.
     */
    public static class MoodAddedEvent {}
}