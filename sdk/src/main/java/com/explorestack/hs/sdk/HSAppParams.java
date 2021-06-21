package com.explorestack.hs.sdk;

import androidx.annotation.Nullable;

public interface HSAppParams {

    long getComponentInitializeTimeoutMs();

    boolean isDebugEnabled();

    boolean isLoggingEnabled();

    @Nullable
    String getAppKey();

    @Nullable
    Integer getAdType();

    @Nullable
    String getAdId();
}
