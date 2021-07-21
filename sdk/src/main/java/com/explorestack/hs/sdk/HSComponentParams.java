package com.explorestack.hs.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

public interface HSComponentParams {

    boolean isDebugEnabled();

    @NonNull
    String getTrackId();

    @Nullable
    String getAppKey();

    @Nullable
    Integer getAdType();

    @NonNull
    JSONObject getExtra();

    @Nullable
    HSAdvertisingProfile getAdvertisingProfile();
}
