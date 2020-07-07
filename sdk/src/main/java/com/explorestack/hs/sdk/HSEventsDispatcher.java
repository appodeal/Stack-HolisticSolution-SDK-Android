package com.explorestack.hs.sdk;

import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class HSEventsDispatcher {

    private static final String TAG = HSEventsDispatcher.class.getSimpleName();

    @NonNull
    private HSAppInstance app;
    @NonNull
    private Map<HSComponent, HSEventsHandler> handlers = new HashMap<>();
    @Nullable
    private List<Pair<String, Map<String, Object>>> pendingEvents;

    public HSEventsDispatcher(@NonNull HSAppInstance app) {
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

    public void addHandler(@NonNull HSComponent component, @Nullable HSEventsHandler handler) {
        if (handler != null) {
            handlers.put(component, handler);
        }
    }

    public void removeHandler(@NonNull HSComponent component) {
        handlers.remove(component);
    }

    public void logEvent(@NonNull String eventName, @Nullable Map<String, Object> params) {
        if (app.isInitialized()) {
            for (Map.Entry<HSComponent, HSEventsHandler> entry : handlers.entrySet()) {
                HSLogger.logInfo(TAG, "[DispatchEvent]: to " + entry.getKey().getName());
                entry.getValue().onEvent(eventName, params);
            }
        } else {
            if (pendingEvents == null) {
                pendingEvents = new ArrayList<>();
            }
            pendingEvents.add(Pair.create(eventName, params));
        }
    }
}
