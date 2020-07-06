package com.explorestack.hs.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;

public interface HSEventsCallback {

    void onEvent(@NonNull String eventName, @Nullable Map<String, Object> params);
}
