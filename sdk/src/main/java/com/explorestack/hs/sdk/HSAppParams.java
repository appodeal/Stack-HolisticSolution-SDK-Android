package com.explorestack.hs.sdk;

import androidx.annotation.Nullable;

public interface HSAppParams {

    long getComponentInitializeTimeoutMs();

    boolean isDebugEnabled();

    @Nullable
    String getAppKey();

    @Nullable
    Integer getAdType();
}
