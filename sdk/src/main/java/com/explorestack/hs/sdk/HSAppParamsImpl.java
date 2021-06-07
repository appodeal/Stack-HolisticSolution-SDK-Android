package com.explorestack.hs.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;

class HSAppParamsImpl implements HSAppParams {

    private static final long DEF_COMPONENT_INITIALIZE_TIMEOUT = TimeUnit.SECONDS.toMillis(30);

    private final boolean isDebugEnabled;
    private final boolean isLoggingEnabled;
    private final long componentInitializeTimeout;
    @Nullable
    private final String appKey;
    @Nullable
    private final Integer adType;

    HSAppParamsImpl(@NonNull HSAppConfig appConfig) {
        isDebugEnabled = appConfig.isDebugEnabled();
        isLoggingEnabled = appConfig.isLoggingEnabled();
        componentInitializeTimeout = appConfig.getComponentInitializeTimeout();
        appKey = appConfig.getAppKey();
        adType = appConfig.getAdType();
    }

    @Override
    public boolean isDebugEnabled() {
        return isDebugEnabled;
    }

    @Override
    public boolean isLoggingEnabled() {
        return isLoggingEnabled;
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
}
