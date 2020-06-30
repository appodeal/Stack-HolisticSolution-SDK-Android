package com.explorestack.hs.sdk;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class HSLogger {

    private static final String TAG = "HSApp";

    private static boolean isEnabled = false;

    public static void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public static boolean isEnabled() {
        return isEnabled;
    }

    public static void logInfo(@NonNull String tag, @Nullable Object object) {
        if (isEnabled) {
            Log.i(TAG, String.format("[%s]: %s", tag, object != null ? object.toString() : null));
        }
    }
}
