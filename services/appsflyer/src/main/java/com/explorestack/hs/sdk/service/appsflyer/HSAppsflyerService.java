package com.explorestack.hs.sdk.service.appsflyer;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.appsflyer.AFLogger;
import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.explorestack.hs.sdk.HSAppParams;
import com.explorestack.hs.sdk.HSConnectorCallback;
import com.explorestack.hs.sdk.HSLogger;
import com.explorestack.hs.sdk.HSService;
import com.explorestack.hs.sdk.HSComponentCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HSAppsflyerService extends HSService {

    @NonNull
    private final String devKey;
    @Nullable
    private final List<String> conversionKeys;
    @Nullable
    private AppsFlyerConversionListener conversionListener;

    public HSAppsflyerService(@NonNull String devKey) {
        this(devKey, null);
    }

    public HSAppsflyerService(@NonNull String devKey, @Nullable List<String> conversionKeys) {
        super("Appsflyer", AppsFlyerLib.getInstance().getSdkVersion());
        this.devKey = devKey;
        this.conversionKeys = conversionKeys;
    }

    @Override
    public void start(@NonNull Context context,
                      @NonNull HSAppParams params,
                      @NonNull HSComponentCallback callback,
                      @NonNull HSConnectorCallback connectorCallback) {
        if (TextUtils.isEmpty(devKey)) {
            callback.onFail(buildError("DevKey not provided"));
            return;
        }
        final AppsFlyerLib appsFlyer = AppsFlyerLib.getInstance();
        appsFlyer.setDebugLog(params.isDebugEnabled());
        if (HSLogger.isEnabled()) {
            appsFlyer.setLogLevel(AFLogger.LogLevel.VERBOSE);
        }
        conversionListener = new ConversionListener(callback, connectorCallback);
        appsFlyer.init(devKey, conversionListener, context);
        appsFlyer.startTracking(context, devKey);

        //Service will not receive conversion data if any activity was started before service initialization
        HSAppsflyerServiceActivityTracker activityTracker =
                HSAppsflyerServiceActivityTracker.getInstance();
        final Activity lastCreatedActivity = activityTracker.getLastCreatedActivity();
        if (lastCreatedActivity != null) {
            appsFlyer.trackAppLaunch(lastCreatedActivity, devKey);
        }
        //We need track started activities state only once
        activityTracker.stop(context);
        connectorCallback.setAttributionId("appsflyer_id", appsFlyer.getAppsFlyerUID(context));
    }

    private final class ConversionListener implements AppsFlyerConversionListener {

        @NonNull
        private final HSComponentCallback callback;
        @NonNull
        private final HSConnectorCallback connectorCallback;

        public ConversionListener(@NonNull HSComponentCallback callback,
                                  @NonNull HSConnectorCallback connectorCallback) {
            this.callback = callback;
            this.connectorCallback = connectorCallback;
        }

        @Override
        public void onConversionDataSuccess(Map<String, Object> map) {
            HSLogger.logInfo("Appsflyer", "onConversionDataSuccess");
            if (map != null && !map.isEmpty()) {
                if (conversionKeys == null || conversionKeys.isEmpty()) {
                    connectorCallback.setConversionData(map);
                } else {
                    Map<String, Object> resultMap = new HashMap<>();
                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                        if (conversionKeys.contains(entry.getKey())) {
                            resultMap.put(entry.getKey(), entry.getValue());
                        }
                    }
                    connectorCallback.setConversionData(resultMap);
                }
            }
            callback.onFinished();
        }

        @Override
        public void onConversionDataFail(String s) {
            HSLogger.logInfo("Appsflyer", "onConversionDataSuccess");
            callback.onFail(buildError(s));
        }

        @Override
        public void onAppOpenAttribution(Map<String, String> map) {
            HSLogger.logInfo("Appsflyer", "onAppOpenAttribution");
        }

        @Override
        public void onAttributionFailure(String s) {
            HSLogger.logInfo("Appsflyer", "onAttributionFailure");
        }
    }
}
