package com.explorestack.hs.sdk;

import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class HSEventsDelegate {

    private static final String TAG = HSEventsDelegate.class.getSimpleName();

    @NonNull
    private HSAppInstance app;
    @NonNull
    private List<HSEventsCallback> eventsCallbacks = new ArrayList<>();
    @Nullable
    private List<Pair<String, Map<String, Object>>> pendingEvents;

    public HSEventsDelegate(@NonNull HSAppInstance app) {
        this.app = app;
    }

    void dispatchPendingEvents() {
        if (pendingEvents != null) {
            for (Pair<String, Map<String, Object>> events : pendingEvents) {
                logEvent(events.first, events.second);
            }
            pendingEvents = null;
        }
    }

    public void addCallback(@Nullable HSEventsCallback callback) {
        if (callback != null) {
            eventsCallbacks.add(callback);
        }
    }

    public void removeCallback(@NonNull HSEventsCallback callback) {
        eventsCallbacks.remove(callback);
    }

    public void logEvent(@NonNull String eventName, @Nullable Map<String, Object> params) {
        if (app.isInitialized()) {
            for (HSEventsCallback callback : eventsCallbacks) {
                HSLogger.logInfo(TAG, "[DelegateEvent]: to " + callback);
                callback.onEvent(eventName, params);
            }
        } else {
            if (pendingEvents == null) {
                pendingEvents = new ArrayList<>();
            }
            pendingEvents.add(Pair.create(eventName, params));
        }
    }
}
