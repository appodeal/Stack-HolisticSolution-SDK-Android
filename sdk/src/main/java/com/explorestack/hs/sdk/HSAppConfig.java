package com.explorestack.hs.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class HSAppConfig {

    private long componentInitializeTimeout;
    private boolean isDebugEnabled;
    @Nullable
    private String appKey;
    @Nullable
    private Integer adType;

    @NonNull
    public HSAppConfig setComponentInitializeTimeout(long componentInitializeTimeout) {
        this.componentInitializeTimeout = componentInitializeTimeout;
        return this;
    }

    long getComponentInitializeTimeout() {
        return componentInitializeTimeout;
    }

    @NonNull
    public HSAppConfig setDebugEnabled(boolean debugEnabled) {
        HSLogger.setEnabled(debugEnabled);
        isDebugEnabled = debugEnabled;
        return this;
    }

    boolean isDebugEnabled() {
        return isDebugEnabled;
    }

    @NonNull
    public HSAppConfig setAppKey(@NonNull String appKey) {
        this.appKey = appKey;
        return this;
    }

    @Nullable
    String getAppKey() {
        return appKey;
    }

    @NonNull
    public HSAppConfig setAdType(@NonNull Integer adType) {
        this.adType = adType;
        return this;
    }

    @Nullable
    Integer getAdType() {
        return adType;
    }
}
