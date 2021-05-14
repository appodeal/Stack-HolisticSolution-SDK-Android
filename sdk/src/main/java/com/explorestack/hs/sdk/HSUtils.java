package com.explorestack.hs.sdk;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;

public class HSUtils {

    public static Bundle mapToBundle(@Nullable Map<String, Object> params) {
        Bundle result = new Bundle();
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                putIntoBundle(result, entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    private static void putIntoBundle(@NonNull Bundle bundle,
                                      @Nullable String key,
                                      @Nullable Object value) {
        if (key == null || value == null) {
            return;
        }
        if (value instanceof Integer) {
            bundle.putInt(key, (int) value);
        } else if (value instanceof int[]) {
            bundle.putIntArray(key, (int[]) value);
        } else if (value instanceof Long) {
            bundle.putLong(key, (long) value);
        } else if (value instanceof long[]) {
            bundle.putLongArray(key, (long[]) value);
        } else if (value instanceof Double) {
            bundle.putDouble(key, (double) value);
        } else if (value instanceof double[]) {
            bundle.putDoubleArray(key, (double[]) value);
        } else if (value instanceof String) {
            bundle.putString(key, (String) value);
        } else if (value instanceof String[]) {
            bundle.putStringArray(key, (String[]) value);
        } else if (value instanceof Boolean) {
            bundle.putBoolean(key, (boolean) value);
        } else if (value instanceof boolean[]) {
            bundle.putBooleanArray(key, (boolean[]) value);
        } else {
            bundle.putString(key, value.toString());
        }
    }
}
