package com.explorestack.hs.sdk;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;

public interface HSConnectorCallback {

    void setAttributionId(@Nullable String key, @Nullable String value);

    void setConversionData(@Nullable Map<String, Object> data);

    void setExtra(@Nullable String key, @Nullable String value);

    void setExtra(@Nullable Map<String, Object> extra);

    void trackInApp(@NonNull Context context, @Nullable String price, @Nullable String currency);
}
