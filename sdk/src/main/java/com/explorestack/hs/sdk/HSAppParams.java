package com.explorestack.hs.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface HSAppParams {

    long getComponentInitializeTimeoutMs();

    boolean isDebugEnabled();

    @NonNull
    String getTrackId();

    @Nullable
    String getAppKey();

    @Nullable
    Integer getAdType();

    @Nullable
    HSAdvertisingProfile getAdvertisingProfile();
}
