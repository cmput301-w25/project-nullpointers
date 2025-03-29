/**
 * AppEventBus.java
 *
 * Provides a wrapper for the EventBus instance.
 *
 * Outstanding Issues: None
 */

package com.hamidat.nullpointersapp.utils.mapUtils;

import org.greenrobot.eventbus.EventBus;

public class AppEventBus {
    private static final EventBus instance = EventBus.getDefault();

    public static EventBus getInstance() {
        return instance;
    }

    public static class MoodAddedEvent {}
}