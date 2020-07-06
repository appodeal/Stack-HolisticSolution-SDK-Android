package com.explorestack.hs.sdk;

import androidx.annotation.NonNull;

import java.util.concurrent.TimeUnit;

class HSAppParamsImpl implements HSAppParams {

    private static final long DEF_COMPONENT_INITIALIZE_TIMEOUT = TimeUnit.SECONDS.toMillis(30);

    private boolean isDebugEnabled;
    private long componentInitializeTimeout;

    HSAppParamsImpl(@NonNull HSAppConfig appConfig) {
        isDebugEnabled = appConfig.isDebugEnabled();
        componentInitializeTimeout = appConfig.getComponentInitializeTimeout();
    }

    @Override
    public boolean isDebugEnabled() {
        return isDebugEnabled;
    }

    @Override
    public long getComponentInitializeTimeoutMs() {
        return componentInitializeTimeout > 0
                ? componentInitializeTimeout
                : DEF_COMPONENT_INITIALIZE_TIMEOUT;
    }
}
