package com.explorestack.hs.sdk;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

class HSConnectorDelegate implements HSConnectorCallback {

    @NonNull
    private final Map<HSComponent, HSConnectorCallback> callbacks = new HashMap<>();
    @NonNull
    private final HSAppInstance app;

    public HSConnectorDelegate(@NonNull HSAppInstance app) {
        this.app = app;
    }

    public void addCallback(@NonNull HSComponent component, @Nullable HSConnectorCallback callback) {
        if (callback != null) {
            callbacks.put(component, callback);
        }
    }

    public void removeCallback(@NonNull HSComponent component) {
        callbacks.remove(component);
    }

    @Override
    public void setAttributionId(@Nullable String key, @Nullable String value) {
        HSLogger.logInfo("setAttributionId", String.format("%s: %s", key, value));
        for (HSConnectorCallback connector : callbacks.values()) {
            connector.setAttributionId(key, value);
        }
    }

    @Override
    public void setConversionData(@Nullable Map<String, Object> data) {
        HSLogger.logInfo("setConversionData", data);
        for (HSConnectorCallback callback : callbacks.values()) {
            callback.setConversionData(data);
        }
    }

    @Override
    public void setExtra(@Nullable String key, @Nullable String value) {
        HSLogger.logInfo("setExtra", String.format("%s: %s", key, value));
        for (HSConnectorCallback callback : callbacks.values()) {
            callback.setExtra(key, value);
        }
    }

    @Override
    public void setExtra(@Nullable Map<String, Object> extra) {
        HSLogger.logInfo("setData", extra);
        for (HSConnectorCallback callback : callbacks.values()) {
            callback.setExtra(extra);
        }
    }

    @Override
    public void trackInApp(@Nullable Context context,
                           @Nullable HSInAppPurchase purchase) {
        HSLogger.logInfo("trackInApp", purchase);
        for (HSConnectorCallback callback : callbacks.values()) {
            callback.trackInApp(context, purchase);
        }
    }

    @Override
    public Map<String, Object> obtainPartnerParams() {
        Map<String, Object> partnerParams = new HashMap<>();
        for (HSConnectorCallback callback : callbacks.values()) {
            partnerParams.putAll(callback.obtainPartnerParams());
        }
        return partnerParams;
    }
}
