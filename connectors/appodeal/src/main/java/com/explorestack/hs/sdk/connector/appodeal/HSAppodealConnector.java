package com.explorestack.hs.sdk.connector.appodeal;

import androidx.annotation.Nullable;

import com.appodeal.ads.Appodeal;
import com.explorestack.hs.sdk.HSConnector;

import java.util.Map;

public class HSAppodealConnector extends HSConnector {

    public HSAppodealConnector() {
        super("Appodeal", Appodeal.getVersion());
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
}
