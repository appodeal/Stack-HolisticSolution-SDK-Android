package com.explorestack.hs.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

class HSComponentParamsImpl implements HSComponentParams {

    private final boolean isDebugEnabled;
    private final boolean isLoggingEnabled;
    @NonNull
    private final String trackId;
    @Nullable
    private final String appKey;
    @Nullable
    private final Integer adType;
    @NonNull
    private final JSONObject extra;
    @Nullable
    private final HSAdvertisingProfile adProfile;

    HSComponentParamsImpl(@NonNull HSAppParams appParams,
                          @NonNull JSONObject serverExtra) {
        isDebugEnabled = appParams.isDebugEnabled();
        isLoggingEnabled = appParams.isLoggingEnabled();
        trackId = appParams.getTrackId();
        appKey = appParams.getAppKey();
        adType = appParams.getAdType();
        extra = serverExtra;
        adProfile = appParams.getAdvertisingProfile();
    }

    @Override
    public boolean isDebugEnabled() {
        return isDebugEnabled;
    }

    @Override
    public boolean isLoggingEnabled() {
        return isLoggingEnabled;
    }

    @NonNull
    @Override
    public String getTrackId() {
        return trackId;
    }

    @Nullable
    @Override
    public String getAppKey() {
        return appKey;
    }

    @Nullable
    @Override
    public Integer getAdType() {
        return adType;
    }

    @NonNull
    @Override
    public JSONObject getExtra() {
        return extra;
    }

    @Nullable
    @Override
    public HSAdvertisingProfile getAdvertisingProfile() {
        return adProfile;
    }
}
