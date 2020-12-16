package com.explorestack.hs.sdk.connector.appodeal;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.appodeal.ads.Appodeal;
import com.explorestack.hs.sdk.HSAppParams;
import com.explorestack.hs.sdk.HSComponentCallback;
import com.explorestack.hs.sdk.HSConnector;
import com.explorestack.hs.sdk.HSInAppPurchase;

import java.util.Map;

public class HSAppodealConnector extends HSConnector {

    public HSAppodealConnector() {
        super("Appodeal", Appodeal.getVersion());
    }

    @Override
    public void initialize(@NonNull Context context,
                           @NonNull HSAppParams params,
                           @NonNull HSComponentCallback callback) {
        callback.onFinished();
    }

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
                Appodeal.setSegmentFilter(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
    }

    @Override
    public void setExtra(@Nullable String key, @Nullable String value) {
        if (key != null && value != null) {
            Appodeal.setExtraData(key, value);
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
            try {
                double doublePrice = Double.parseDouble(purchase.getPrice());
                Appodeal.trackInAppPurchase(context, doublePrice, purchase.getCurrency());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }
}
