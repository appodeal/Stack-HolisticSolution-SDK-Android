package com.explorestack.hs.sdk;

import androidx.annotation.Nullable;

import org.json.JSONObject;

public interface HSComponentParams {

    boolean isDebugEnabled();

    boolean isLoggingEnabled();

    @Nullable
    String getAppKey();

    @Nullable
    Integer getAdType();

    @Nullable
    JSONObject getExtra();
}
