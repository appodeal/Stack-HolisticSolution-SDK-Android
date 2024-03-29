package com.explorestack.hs.sdk.service.adjust;

import static com.explorestack.hs.sdk.HSUtils.mergeMap;

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
import com.explorestack.hs.sdk.HSAdvertisingProfile;
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

    @Nullable
    private Map<String, String> eventTokens = null;
    @Nullable
    private HSConnectorCallback connectorCallback = null;

    public HSAdjustService() {
        super("Adjust", Adjust.getSdkVersion(), BuildConfig.COMPONENT_VERSION);
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
        if (extra.has("events")) {
            this.eventTokens = HSUtils.jsonToMap(extra.optJSONObject("events"));
        }
        this.connectorCallback = connectorCallback;
        AdjustConfig adjustConfig = new AdjustConfig(context, appToken, environment);
        adjustConfig.setLogLevel(HSLogger.isEnabled() ? LogLevel.VERBOSE : LogLevel.INFO);
        adjustConfig.setOnAttributionChangedListener(new AttributionChangedListener(callback, connectorCallback));

        HSAdvertisingProfile advertisingProfile = params.getAdvertisingProfile();
        if (advertisingProfile != null && advertisingProfile.isZero()) {
            String idfa = advertisingProfile.getId(context);
            Adjust.addSessionPartnerParameter("externalDeviceId", idfa);
            Adjust.addSessionCallbackParameter("externalDeviceId", idfa);
            adjustConfig.setExternalDeviceId(idfa);
        }
        Map<String, Object> partnerParams = connectorCallback.getPartnerParams();
        if (partnerParams != null && partnerParams.size() > 0) {
            for (Map.Entry<String, Object> param : partnerParams.entrySet()) {
                String key = param.getKey();
                String value = String.valueOf(param.getValue());
                Adjust.addSessionPartnerParameter(key, value);
                Adjust.addSessionCallbackParameter(key, value);
            }
        }
        if (context instanceof Application) {
            ((Application) context).registerActivityLifecycleCallbacks(new AdjustLifecycleCallbacks());
        }
        Adjust.onCreate(adjustConfig);
        Adjust.onResume();

        ADJPConfig adjustPurchaseConfig = new ADJPConfig(appToken, environment);
        adjustPurchaseConfig.setLogLevel(HSLogger.isEnabled() ? ADJPLogLevel.VERBOSE : ADJPLogLevel.INFO);
        AdjustPurchase.init(adjustPurchaseConfig);

        connectorCallback.setExtra("mmp", "adjust");
        AdjustAttribution attribution = Adjust.getAttribution();
        if (attribution != null && !TextUtils.isEmpty(attribution.adid)) {
            connectorCallback.setAttributionId("attribution_id", attribution.adid);
            callback.onFinished();
        }
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

    @Nullable
    private String getEventToken(@NonNull String eventName) {
        String eventToken;
        if (eventTokens != null) {
            if (((eventToken = eventTokens.get(eventName)) != null) || eventTokens.containsKey(eventName)) {
                return eventToken;
            } else {
                return eventTokens.get("hs_sdk_unknown");
            }
        }
        return null;
    }

    private static final class AttributionChangedListener implements OnAttributionChangedListener {

        @NonNull
        private final HSComponentCallback callback;
        @NonNull
        private final HSConnectorCallback connectorCallback;

        public AttributionChangedListener(@NonNull HSComponentCallback callback,
                                          @NonNull HSConnectorCallback connectorCallback) {
            this.callback = callback;
            this.connectorCallback = connectorCallback;
        }

        @Override
        public void onAttributionChanged(AdjustAttribution attribution) {
            HSLogger.logInfo("Adjust", "onAttributionChanged");
            if (attribution != null) {
                if (!TextUtils.isEmpty(attribution.adid)) {
                    connectorCallback.setAttributionId("attribution_id", attribution.adid);
                }
                connectorCallback.setConversionData(convertAttributionToMap(attribution));
            }
            callback.onFinished();
            if (externalAttributionListener != null) {
                externalAttributionListener.onAttributionChanged(attribution);
            }
        }

        private Map<String, Object> convertAttributionToMap(@NonNull AdjustAttribution attribution) {
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
            String eventToken = getEventToken(eventName);
            AdjustEvent adjustEvent = new AdjustEvent(eventToken);
            Map<String, Object> partnerParams = null;
            if (connectorCallback != null) {
                partnerParams = connectorCallback.getPartnerParams();
            }
            Map<String, Object> eventParams = mergeMap(params, partnerParams);
            if (eventParams.size() > 0) {
                for (Map.Entry<String, Object> param : eventParams.entrySet()) {
                    String key = param.getKey();
                    String value = String.valueOf(param.getValue());
                    adjustEvent.addPartnerParameter(key, value);
                    adjustEvent.addCallbackParameter(key, value);
                }
            }
            Adjust.trackEvent(adjustEvent);
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
            HSInAppPurchase.PurchaseType type;
            if (pendingPurchase != null && (type = pendingPurchase.getType()) != null) {
                switch (type) {
                    case INAPP:
                        AdjustPurchase.verifyPurchase(purchase.getSku(),
                                                      purchase.getPurchaseToken(),
                                                      purchase.getDeveloperPayload(),
                                                      this);
                        break;
                    case SUBS:
                        trackSubscription(pendingPurchase);
                        break;
                    default:
                        onFail(buildError("Purchase type not provided"));
                        break;
                }
            } else {
                onFail(buildError("Purchase not provided"));
            }
        }

        @Override
        public void onVerificationFinished(ADJPVerificationInfo info) {
            if (info == null) {
                onFail(buildError("Adjust purchase verification info not provided"));
            } else {
                switch (info.getVerificationState()) {
                    case ADJPVerificationStatePassed: {
                        assert pendingPurchase != null;
                        trackInApp(pendingPurchase);
                        break;
                    }
                    case ADJPVerificationStateFailed: {
                        AdjustEvent event = new AdjustEvent(getEventToken("hs_sdk_purchase_error"));
                        Adjust.trackEvent(event);
                        onFail(buildError("Adjust purchase verification state failed"));
                        break;
                    }
                    case ADJPVerificationStateUnknown: {
                        AdjustEvent event = new AdjustEvent(getEventToken("hs_sdk_purchase_error"));
                        Adjust.trackEvent(event);
                        onFail(buildError("Adjust purchase verification state unknown"));
                        break;
                    }
                    default: {
                        AdjustEvent event = new AdjustEvent(getEventToken("hs_sdk_purchase_error"));
                        Adjust.trackEvent(event);
                        onFail(buildError("Adjust purchase not verified"));
                        break;
                    }
                }
            }
            if (externalPurchaseValidatorListener != null) {
                externalPurchaseValidatorListener.onVerificationFinished(info);
            }
        }

        private void trackSubscription(@NonNull HSInAppPurchase purchase) {
            String purchasePrice;
            if ((purchasePrice = purchase.getPrice()) != null) {
                String currency = purchase.getCurrency();
                Double price = HSUtils.parsePrice(purchasePrice, currency);
                if (price != null) {
                    AdjustPlayStoreSubscription subscription =
                            new AdjustPlayStoreSubscription(price.longValue(),
                                                            currency,
                                                            purchase.getSku(),
                                                            purchase.getOrderId(),
                                                            purchase.getSignature(),
                                                            purchase.getPurchaseToken());
                    subscription.setPurchaseTime(purchase.getPurchaseTimestamp());
                    Map<String, Object> partnerParams = null;
                    if (connectorCallback != null) {
                        partnerParams = connectorCallback.getPartnerParams();
                    }
                    Map<String, String> purchaseParams = purchase.getAdditionalParameters();
                    Map<String, Object> subsParams = mergeMap(purchaseParams, partnerParams);
                    if (subsParams.size() > 0) {
                        for (Map.Entry<String, Object> param : subsParams.entrySet()) {
                            String key = param.getKey();
                            String value = String.valueOf(param.getValue());
                            subscription.addPartnerParameter(key, value);
                            subscription.addCallbackParameter(key, value);
                        }
                    }
                    Adjust.trackPlayStoreSubscription(subscription);
                    onSuccess();
                    return;
                }
            }
            onFail(buildError("Adjust subscription track failed"));
        }

        private void trackInApp(@NonNull HSInAppPurchase purchase) {
            String purchasePrice;
            if ((purchasePrice = purchase.getPrice()) != null) {
                String currency = purchase.getCurrency();
                Double price = HSUtils.parsePrice(purchasePrice, currency);
                if (price != null) {
                    AdjustEvent inapp = new AdjustEvent(getEventToken("hs_sdk_purchase"));
                    inapp.setRevenue(price, currency);
                    Map<String, Object> partnerParams = null;
                    if (connectorCallback != null) {
                        partnerParams = connectorCallback.getPartnerParams();
                    }
                    Map<String, String> purchaseParams = purchase.getAdditionalParameters();
                    Map<String, Object> inappParams = mergeMap(purchaseParams, partnerParams);
                    if (inappParams.size() > 0) {
                        for (Map.Entry<String, Object> param : inappParams.entrySet()) {
                            String key = param.getKey();
                            String value = String.valueOf(param.getValue());
                            inapp.addPartnerParameter(key, value);
                            inapp.addCallbackParameter(key, value);
                        }
                    }
                    Adjust.trackEvent(inapp);
                    onSuccess();
                    return;
                }
            }
            onFail(buildError("Adjust in-app track failed"));
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
}
