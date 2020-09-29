package com.explorestack.hs.sdk.service.appsflyer;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.appsflyer.AFLogger;
import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerInAppPurchaseValidatorListener;
import com.appsflyer.AppsFlyerLib;
import com.explorestack.hs.sdk.HSAppParams;
import com.explorestack.hs.sdk.HSComponentCallback;
import com.explorestack.hs.sdk.HSConnectorCallback;
import com.explorestack.hs.sdk.HSEventsHandler;
import com.explorestack.hs.sdk.HSIAPValidateCallback;
import com.explorestack.hs.sdk.HSIAPValidateHandler;
import com.explorestack.hs.sdk.HSInAppPurchase;
import com.explorestack.hs.sdk.HSLogger;
import com.explorestack.hs.sdk.HSService;

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
    @Nullable
    private AppsFlyerConversionListener externalConversionListener;
    @Nullable
    private AppsFlyerInAppPurchaseValidatorListener externalPurchaseValidatorListener;

    public HSAppsflyerService(@NonNull String devKey) {
        this(devKey, null);
    }

    public HSAppsflyerService(@NonNull String devKey, @Nullable List<String> conversionKeys) {
        super("Appsflyer", AppsFlyerLib.getInstance().getSdkVersion());
        this.devKey = devKey;
        this.conversionKeys = conversionKeys;
    }

    public void setAppsFlyerConversionListener(@Nullable AppsFlyerConversionListener listener) {
        this.externalConversionListener = listener;
    }

    public void setAppsFlyerInAppPurchaseValidatorListener(@Nullable AppsFlyerInAppPurchaseValidatorListener listener) {
        this.externalPurchaseValidatorListener = listener;
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
        appsFlyer.trackEvent(context, null, null);
        appsFlyer.registerConversionListener(context, conversionListener);
        appsFlyer.startTracking(context, devKey);
        connectorCallback.setAttributionId("attribution_id", appsFlyer.getAppsFlyerUID(context));
    }

    @Nullable
    @Override
    public HSEventsHandler createEventsHandler(@NonNull Context context) {
        return new HSEventsDelegate(context);
    }

    @Nullable
    @Override
    public HSIAPValidateHandler createIAPValidateHandler(@NonNull Context context) {
        return new HSIAPValidateDelegate(context);
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
            if (externalConversionListener != null) {
                externalConversionListener.onConversionDataSuccess(map);
            }
        }

        @Override
        public void onConversionDataFail(String s) {
            HSLogger.logInfo("Appsflyer", "onConversionDataSuccess: " + s);
            callback.onFail(buildError(s));
            if (externalConversionListener != null) {
                externalConversionListener.onConversionDataFail(s);
            }
        }

        @Override
        public void onAppOpenAttribution(Map<String, String> map) {
            HSLogger.logInfo("Appsflyer", "onAppOpenAttribution");
            if (externalConversionListener != null) {
                externalConversionListener.onAppOpenAttribution(map);
            }
        }

        @Override
        public void onAttributionFailure(String s) {
            HSLogger.logInfo("Appsflyer", "onAttributionFailure: " + s);
            if (externalConversionListener != null) {
                externalConversionListener.onAttributionFailure(s);
            }
        }
    }

    private final class HSEventsDelegate implements HSEventsHandler {

        @NonNull
        private final Context context;

        public HSEventsDelegate(@NonNull Context context) {
            this.context = context;
        }

        @Override
        public void onEvent(@NonNull String eventName,
                            @Nullable Map<String, Object> params) {
            AppsFlyerLib.getInstance().trackEvent(context, eventName, params);
        }
    }

    private final class HSIAPValidateDelegate implements HSIAPValidateHandler, AppsFlyerInAppPurchaseValidatorListener {
        @NonNull
        private final Context context;
        @Nullable
        private HSIAPValidateCallback pendingCallback;

        public HSIAPValidateDelegate(@NonNull Context context) {
            this.context = context;
            AppsFlyerLib.getInstance().registerValidatorListener(context, this);
        }

        @Override
        public void onValidateInAppPurchase(@NonNull HSInAppPurchase purchase,
                                            @NonNull HSIAPValidateCallback callback) {
            pendingCallback = callback;
            AppsFlyerLib.getInstance().validateAndTrackInAppPurchase(
                    context, purchase.getPublicKey(), purchase.getSignature(),
                    purchase.getPurchaseData(), purchase.getPrice(),
                    purchase.getCurrency(), purchase.getAdditionalParameters());
        }

        @Override
        public void onValidateInApp() {
            if (pendingCallback != null) {
                pendingCallback.onSuccess();
                pendingCallback = null;
            }
            if (externalPurchaseValidatorListener != null) {
                externalPurchaseValidatorListener.onValidateInApp();
            }
        }

        @Override
        public void onValidateInAppFailure(String s) {
            if (pendingCallback != null) {
                pendingCallback.onFail(buildError(s));
                pendingCallback = null;
            }
            if (externalPurchaseValidatorListener != null) {
                externalPurchaseValidatorListener.onValidateInAppFailure(s);
            }
        }
    }
}
