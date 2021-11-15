package com.explorestack.hs.sdk.connector.appodeal;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.utils.Log;
import com.explorestack.consent.Consent;
import com.explorestack.hs.sdk.HSComponentCallback;
import com.explorestack.hs.sdk.HSComponentParams;
import com.explorestack.hs.sdk.HSConnector;
import com.explorestack.hs.sdk.HSConnectorCallback;
import com.explorestack.hs.sdk.HSInAppPurchase;
import com.explorestack.hs.sdk.HSLogger;
import com.explorestack.hs.sdk.HSRegulator;
import com.explorestack.hs.sdk.HSUtils;

import java.util.HashMap;
import java.util.Map;

public class HSAppodealConnector extends HSConnector<Consent> {

    public HSAppodealConnector() {
        super("Appodeal", Appodeal.getVersion(), BuildConfig.COMPONENT_VERSION);
    }

    @Override
    public void initialize(@Nullable Activity activity,
                           @NonNull HSComponentParams params,
                           @NonNull HSComponentCallback callback,
                           @Nullable HSRegulator<Consent> regulator) {
        if (activity == null) {
            callback.onFail(buildError("Activity not provided"));
            return;
        }
        String appKey = params.getAppKey();
        if (TextUtils.isEmpty(appKey)) {
            callback.onFail(buildError("AppKey not provided"));
            return;
        }
        Integer adType = params.getAdType();
        if (adType == null || adType < 0) {
            callback.onFail(buildError("AdType not provided"));
            return;
        }
        Appodeal.setLogLevel(HSLogger.isEnabled() ? Log.LogLevel.verbose : Log.LogLevel.none);
        Appodeal.setExtraData("track_id", params.getTrackId());
        assert appKey != null;
        if (regulator == null || regulator.getConsent() == null) {
            Appodeal.initialize(activity, appKey, adType);
        } else {
            Appodeal.initialize(activity, appKey, adType, regulator.getConsent());
        }
        callback.onFinished();
    }

    @Nullable
    @Override
    public HSConnectorCallback createConnectorCallback(@NonNull Context context) {
        return new HSConnectorDelegate();
    }

    private static final class HSConnectorDelegate implements HSConnectorCallback {
        private Map<String, Object> extra = new HashMap<>();

        @Override
        public void setAttributionId(@Nullable String key, @Nullable String value) {
            if (key != null && value != null) {
                Appodeal.setExtraData(key, value);
            }
        }

        @Override
        public void setConversionData(@Nullable Map<String, Object> data) {
            if (data != null) {
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    Appodeal.setCustomFilter(entry.getKey(), String.valueOf(entry.getValue()));
                }
            }
        }

        @Override
        public void setExtra(@Nullable String key, @Nullable String value) {
            if (key != null && value != null) {
                Appodeal.setExtraData(key, value);
                extra.put(key, value);
            }
        }

        @Override
        public void setExtra(@Nullable Map<String, Object> extra) {
            if (extra != null) {
                for (Map.Entry<String, Object> entry : extra.entrySet()) {
                    setExtra(entry.getKey(), String.valueOf(entry.getValue()));
                }
            }
        }

        @Override
        public void trackInApp(@Nullable Context context, @Nullable HSInAppPurchase purchase) {
            if (context != null && purchase != null) {
                String purchasePrice;
                if ((purchasePrice = purchase.getPrice()) != null) {
                    String currency = purchase.getCurrency();
                    Double price = HSUtils.parsePrice(purchasePrice, currency);
                    if (price != null) {
                        Appodeal.trackInAppPurchase(context, price, currency);
                    }
                }
            }
        }

        @Override
        public Map<String, Object> getConnectorData() {
            Map<String, Object> data = new HashMap<>();
            data.put("appodeal_framework", Appodeal.getFrameworkName());
            data.put("appodeal_framework_version", Appodeal.getEngineVersion());
            data.put("appodeal_plugin_version", Appodeal.getPluginVersion());
            data.put("appodeal_sdk_version", Appodeal.getVersion());
            data.put("appodeal_segment_id", Appodeal.getSegmentId());
            data.put("firebase_keywords", extra);
            return data;
        }
    }
}
