package com.explorestack.hs.sdk;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Map;

class HSConnectorDelegate implements HSConnectorCallback {

    @NonNull
    private final List<HSConnector> children;

    public HSConnectorDelegate(@NonNull List<HSConnector> children) {
        this.children = children;
    }

    @Override
    public void setAttributionId(@Nullable String key, @Nullable String value) {
        HSLogger.logInfo("setAttributionId", String.format("%s: %s", key, value));
        for (HSConnector connector : children) {
            connector.setAttributionId(key, value);
        }
    }

    @Override
    public void setConversionData(@Nullable Map<String, Object> data) {
        HSLogger.logInfo("setConversionData", data);
        for (HSConnector connector : children) {
            connector.setConversionData(data);
        }
    }

    @Override
    public void setExtra(@Nullable String key, @Nullable String value) {
        HSLogger.logInfo("setExtra", String.format("%s: %s", key, value));
        for (HSConnector connector : children) {
            connector.setExtra(key, value);
        }
    }

    @Override
    public void setExtra(@Nullable Map<String, Object> extra) {
        HSLogger.logInfo("setData", extra);
        for (HSConnector connector : children) {
            connector.setExtra(extra);
        }
    }

    @Override
    public void trackInApp(@Nullable Context context,
                           @Nullable HSInAppPurchase purchase) {
        HSLogger.logInfo("trackInApp", purchase);
        for (HSConnector connector : children) {
            connector.trackInApp(context, purchase);
        }
    }
}
