package com.explorestack.hs.sdk;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class HSAdvertisingProfile {

    private static final String DEFAULT_ADVERTISING_ID = "00000000-0000-0000-0000-000000000000";

    @NonNull
    private final String name;

    @Nullable
    String id;
    boolean limitAdTrackingEnabled;

    public HSAdvertisingProfile(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public boolean isZero() {
        return (TextUtils.isEmpty(id) || DEFAULT_ADVERTISING_ID.equals(id));
    }

    @Nullable
    public String getId(@NonNull Context context) {
        if (isZero()) {
            return HSCoreUtils.getAdvertisingUUID(context);
        } else {
            return id;
        }
    }

    public boolean isLimitAdTrackingEnabled() {
        return limitAdTrackingEnabled;
    }

    abstract void extractParams(Context context) throws Throwable;

    abstract boolean isEnabled(@NonNull Context context) throws Throwable;
}
