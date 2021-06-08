package com.explorestack.hs.sdk.service.adjust;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustAttribution;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.AdjustEvent;
import com.adjust.sdk.AdjustPlayStoreSubscription;
import com.adjust.sdk.LogLevel;
import com.adjust.sdk.OnAttributionChangedListener;
import com.adjust.sdk.purchase.ADJPConfig;
import com.adjust.sdk.purchase.ADJPLogLevel;
import com.adjust.sdk.purchase.ADJPVerificationInfo;
import com.adjust.sdk.purchase.AdjustPurchase;
import com.adjust.sdk.purchase.OnADJPVerificationFinished;
import com.explorestack.hs.sdk.HSComponentCallback;
import com.explorestack.hs.sdk.HSComponentParams;
import com.explorestack.hs.sdk.HSConnectorCallback;
import com.explorestack.hs.sdk.HSError;
import com.explorestack.hs.sdk.HSEventsHandler;
import com.explorestack.hs.sdk.HSIAPValidateCallback;
import com.explorestack.hs.sdk.HSIAPValidateHandler;
import com.explorestack.hs.sdk.HSInAppPurchase;
import com.explorestack.hs.sdk.HSLogger;
import com.explorestack.hs.sdk.HSService;
import com.explorestack.hs.sdk.HSUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class HSAdjustService extends HSService {

    @Nullable
    private static OnAttributionChangedListener externalAttributionListener;
    @Nullable
    private static OnADJPVerificationFinished externalPurchaseValidatorListener;
    private boolean isTracked;

    public HSAdjustService() {
        super("adjust", Adjust.getSdkVersion());
    }

    public static void setAttributionChangedListener(@Nullable OnAttributionChangedListener listener) {
        externalAttributionListener = listener;
    }

    public static void setAdjustInAppPurchaseValidatorListener(@Nullable OnADJPVerificationFinished listener) {
        externalPurchaseValidatorListener = listener;
    }

    @Override
    public void start(@NonNull Context context,
                      @NonNull HSComponentParams params,
                      @NonNull HSComponentCallback callback,
                      @NonNull HSConnectorCallback connectorCallback) {
        JSONObject extra = params.getExtra();
        isTracked = extra.optBoolean("tracking", true);
        String appToken = extra.optString("app_token");
        if (TextUtils.isEmpty(appToken)) {
            callback.onFail(buildError("AppToken not provided"));
            return;
        }
        String environment = extra.optString("environment");
        if (TextUtils.isEmpty(environment)) {
            callback.onFail(buildError("Environment not provided"));
            return;
        }
        AdjustConfig adjustConfig = new AdjustConfig(context, appToken, environment);
        adjustConfig.setLogLevel(params.isLoggingEnabled() ? LogLevel.VERBOSE : LogLevel.INFO);

        OnAttributionChangedListener attributionListener =
                new AttributionChangedListener(connectorCallback, externalAttributionListener);
        adjustConfig.setOnAttributionChangedListener(attributionListener);
        if (context instanceof Application) {
            ((Application) context).registerActivityLifecycleCallbacks(new AdjustLifecycleCallbacks());
        }
        Adjust.onCreate(adjustConfig);
        Adjust.onResume();

        ADJPConfig adjustPurchaseConfig = new ADJPConfig(appToken, environment);
        adjustPurchaseConfig.setLogLevel(params.isLoggingEnabled() ? ADJPLogLevel.VERBOSE : ADJPLogLevel.INFO);
        AdjustPurchase.init(adjustPurchaseConfig);

        connectorCallback.setAttributionId("attribution_id", Adjust.getAdid());
        callback.onFinished();
    }

    @Nullable
    @Override
    public HSEventsHandler createEventsHandler(@NonNull Context context) {
        return new HSEventsDelegate();
    }

    @Nullable
    @Override
    public HSIAPValidateHandler createIAPValidateHandler(@NonNull Context context) {
        return new HSIAPValidateDelegate();
    }

    private void trackEvent(AdjustEvent event) {
        if (isTracked) {
            Adjust.trackEvent(event);
        }
    }

    private static final class AttributionChangedListener implements OnAttributionChangedListener {

        @NonNull
        private final HSConnectorCallback connectorCallback;
        @Nullable
        private final OnAttributionChangedListener externalAttributionListener;

        public AttributionChangedListener(@NonNull HSConnectorCallback connectorCallback,
                                          @Nullable OnAttributionChangedListener externalAttributionListener) {
            this.connectorCallback = connectorCallback;
            this.externalAttributionListener = externalAttributionListener;
        }

        @Override
        public void onAttributionChanged(AdjustAttribution attribution) {
            HSLogger.logInfo("Adjust", "onAttributionChanged");
            if (attribution != null) {
                // TODO: 15.05.2021 set conversion data
                Map<String, Object> data = AdjustUtils.convertAttributionDataToMap(attribution);
                connectorCallback.setConversionData(data);
            }
            if (externalAttributionListener != null) {
                externalAttributionListener.onAttributionChanged(attribution);
            }
        }
    }

    private static final class AdjustLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
        @Override
        public void onActivityCreated(@NonNull Activity activity,
                                      @Nullable Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
            Adjust.onResume();
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
            Adjust.onPause();
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity,
                                                @NonNull Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
        }
    }

    private final class HSEventsDelegate implements HSEventsHandler {

        @Override
        public void onEvent(@NonNull String eventName,
                            @Nullable Map<String, Object> params) {
            AdjustEvent adjustEvent = new AdjustEvent(eventName);
            if (params != null && params.size() > 0) {
                for (Map.Entry<String, Object> param : params.entrySet()) {
                    // TODO: 15.05.2021 additional params
                    adjustEvent.addCallbackParameter(param.getKey(), String.valueOf(param.getValue()));
                    adjustEvent.addPartnerParameter(param.getKey(), String.valueOf(param.getValue()));
                }
            }
            trackEvent(adjustEvent);
        }
    }

    private final class HSIAPValidateDelegate implements HSIAPValidateHandler, OnADJPVerificationFinished {

        @Nullable
        private HSIAPValidateCallback pendingCallback;
        @Nullable
        private HSInAppPurchase pendingPurchase;

        @Override
        public void onValidateInAppPurchase(@NonNull HSInAppPurchase purchase,
                                            @NonNull HSIAPValidateCallback callback) {
            pendingCallback = callback;
            pendingPurchase = purchase;
            if (purchase != null) {
                switch (purchase.getType()) {
                    case PURCHASE:
                        if (isTracked) {
                            AdjustPurchase.verifyPurchase(purchase.getSku(),
                                  purchase.getPurchaseToken(),
                                  purchase.getPurchaseData(),
                                  this);
                        }
                        return;
                    case SUBSCRIPTION:
                        validateSubscribtion(purchase);
                }
            }
        }

        private void validateSubscribtion(HSInAppPurchase purchase) {
            if (purchase.getPrice() != null && isTracked) {
                AdjustPlayStoreSubscription subscription = new AdjustPlayStoreSubscription(
                        Long.parseLong(purchase.getPrice()),
                        purchase.getCurrency(),
                        purchase.getSku(),
                        purchase.getOrderId(),
                        purchase.getSignature(),
                        purchase.getPurchaseToken());
                subscription.setPurchaseTime(purchase.getPurchaseTimestamp());
                Adjust.trackPlayStoreSubscription(subscription);
            }
        }

        @Override
        public void onVerificationFinished(ADJPVerificationInfo info) {
            if (info == null) {
                onFail(buildError("Adjust purchase verification info not provided"));
            } else {
                switch (info.getVerificationState()) {
                    case ADJPVerificationStatePassed: {
                        if (pendingPurchase != null) {
                            switch (pendingPurchase.getType()) {
                                case PURCHASE:
                                    trackPurchase();
                                    return;
                                case SUBSCRIPTION:
                                    validateSubscribtion(pendingPurchase);
                            }
                        } else {
                            onFail(buildError("Purchase not provided"));
                        }
                        break;
                    }
                    case ADJPVerificationStateFailed: {
                        AdjustEvent event = new AdjustEvent("{RevenueEventFailedToken}");
                        trackEvent(event);
                        onFail(buildError("Adjust purchase verification state failed"));
                        break;
                    }
                    case ADJPVerificationStateUnknown: {
                        AdjustEvent event = new AdjustEvent("{RevenueEventUnknownToken}");
                        trackEvent(event);
                        onFail(buildError("Adjust purchase verification state unknown"));
                        break;
                    }
                    default: {
                        AdjustEvent event = new AdjustEvent("{RevenueEventNotVerifiedToken}");
                        trackEvent(event);
                        onFail(buildError("Adjust purchase not verified"));
                        break;
                    }
                }
            }
            if (externalPurchaseValidatorListener != null) {
                externalPurchaseValidatorListener.onVerificationFinished(info);
            }
        }

        private void trackPurchase() {
            String purchasePrice;
            if ((purchasePrice = pendingPurchase.getPrice()) != null) {
                String currency = pendingPurchase.getCurrency();
                Double price = HSUtils.parsePrice(purchasePrice, currency);
                if (price != null) {
                    AdjustEvent event = new AdjustEvent("{RevenueEventPassedToken}");
                    event.setRevenue(price, currency);
                    trackEvent(event);
                    onSuccess();
                }
            }
        }

        private void onSuccess() {
            if (pendingCallback != null) {
                pendingCallback.onSuccess();
                pendingCallback = null;
            }
        }

        private void onFail(@NonNull HSError error) {
            if (pendingCallback != null) {
                pendingCallback.onFail(error);
                pendingCallback = null;
            }
        }
    }

    private static final class AdjustUtils {

        private static Map<String, Object> convertAttributionDataToMap(AdjustAttribution attribution) {
            Map<String, Object> data = new HashMap<>();
            data.put("tracker_token", attribution.trackerToken);
            data.put("tracker_name", attribution.trackerName);
            data.put("network", attribution.network);
            data.put("campaign", attribution.campaign);
            data.put("adgroup", attribution.adgroup);
            data.put("creative", attribution.creative);
            data.put("click_label", attribution.clickLabel);
            data.put("adid", attribution.adid);
            data.put("cost_type", attribution.costType);
            data.put("cost_amount", attribution.costAmount);
            data.put("cost_currency", attribution.costCurrency);
            return data;
        }
    }
}
