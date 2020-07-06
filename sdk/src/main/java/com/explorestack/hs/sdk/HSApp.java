package com.explorestack.hs.sdk;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;

public class HSApp {

    public static boolean isInitialized() {
        return HSAppInstance.getInstance().isInitialized();
    }

    public static void initialize(@NonNull Context context, @NonNull HSAppConfig config) {
        initialize(context, config, null);
    }

    public static void initialize(@NonNull Context context,
                                  @NonNull HSAppConfig config,
                                  @Nullable final HSAppInitializeListener listener) {
        HSAppInstance.getInstance().initialize(context, config, listener);
    }

    public static void addInitializeListener(@NonNull HSAppInitializeListener listener) {
        HSAppInstance.getInstance().addInitializeListener(listener);
    }

    public static void removeInitializeListener(@NonNull HSAppInitializeListener listener) {
        HSAppInstance.getInstance().removeInitializeListener(listener);
    }

    public static void logEvent(@NonNull String eventName) {
        logEvent(eventName, null);
    }

    public static void logEvent(@NonNull String eventName, @Nullable Map<String, Object> params) {
        HSAppInstance.getInstance().logEvent(eventName, params);
    }

    private HSApp() {
    }

}
