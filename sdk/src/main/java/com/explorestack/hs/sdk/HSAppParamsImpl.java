package com.explorestack.hs.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;

class HSAppParamsImpl implements HSAppParams {

    private static final long DEF_COMPONENT_INITIALIZE_TIMEOUT = TimeUnit.SECONDS.toMillis(30);

    private final boolean isDebugEnabled;
    private final long componentInitializeTimeout;
    @NonNull
    private final String trackId;
    @Nullable
    private final String appKey;
    @Nullable
    private final Integer adType;
    @Nullable
    private final HSAdvertisingProfile adProfile;

    HSAppParamsImpl(@NonNull HSAppConfig appConfig,
                    @NonNull HSAppInstance app,
                    @Nullable HSAdvertisingProfile advertisingProfile) {
        isDebugEnabled = appConfig.isDebugEnabled();
        componentInitializeTimeout = appConfig.getComponentInitializeTimeout();
        appKey = appConfig.getAppKey();
        adType = appConfig.getAdType();
        trackId = app.getTrackId();
        adProfile = advertisingProfile;
    }

    @Override
    public boolean isDebugEnabled() {
        return isDebugEnabled;
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

    @Override
    public long getComponentInitializeTimeoutMs() {
        return componentInitializeTimeout > 0
                ? componentInitializeTimeout
                : DEF_COMPONENT_INITIALIZE_TIMEOUT;
    }

    @Nullable
    @Override
    public HSAdvertisingProfile getAdvertisingProfile() {
        return adProfile;
    }
}
