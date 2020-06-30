package com.explorestack.hs.sdk;

import androidx.annotation.NonNull;

import java.util.concurrent.TimeUnit;

class HSAppParamsImpl implements HSAppParams {

    private static final long DEF_SERVICE_INITIALIZE_TIMEOUT = TimeUnit.SECONDS.toMillis(30);

    private boolean isDebugEnabled;
    private long serviceInitializeTimeout;

    HSAppParamsImpl(@NonNull HSAppConfig appConfig) {
        isDebugEnabled = appConfig.isDebugEnabled();
        serviceInitializeTimeout = appConfig.getServiceInitializeTimeout();
    }

    @Override
    public boolean isDebugEnabled() {
        return isDebugEnabled;
    }

    @Override
    public long getServiceInitializeTimeoutMs() {
        return serviceInitializeTimeout > 0
                ? serviceInitializeTimeout
                : DEF_SERVICE_INITIALIZE_TIMEOUT;
    }
}
