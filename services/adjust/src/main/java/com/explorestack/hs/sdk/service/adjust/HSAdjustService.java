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
import com.adjust.sdk.LogLevel;
import com.adjust.sdk.OnAttributionChangedListener;
import com.explorestack.hs.sdk.HSAppParams;
import com.explorestack.hs.sdk.HSComponentCallback;
import com.explorestack.hs.sdk.HSConnectorCallback;
import com.explorestack.hs.sdk.HSEventsHandler;
import com.explorestack.hs.sdk.HSIAPValidateCallback;
import com.explorestack.hs.sdk.HSIAPValidateHandler;
import com.explorestack.hs.sdk.HSInAppPurchase;
import com.explorestack.hs.sdk.HSLogger;
import com.explorestack.hs.sdk.HSService;
import com.explorestack.hs.sdk.HSUtils;

import java.util.HashMap;
import java.util.Map;

public class HSAdjustService extends HSService {

    @NonNull
    private final String appToken;
    @NonNull
    private final String environment;
    @Nullable
    private OnAttributionChangedListener externalAttributionListener;

    public HSAdjustService(@NonNull String appToken) {
        this(appToken, AdjustConfig.ENVIRONMENT_PRODUCTION);
    }

    public HSAdjustService(@NonNull String appToken, @NonNull String environment) {
        super("Adjust", Adjust.getSdkVersion());
        this.appToken = appToken;
        this.environment = environment;
    }

    public void setAttributionChangedListener(@Nullable OnAttributionChangedListener listener) {
        this.externalAttributionListener = listener;
    }

    @Override
    public void start(@NonNull Context context,
                      @NonNull HSAppParams params,
                      @NonNull HSComponentCallback callback,
                      @NonNull HSConnectorCallback connectorCallback) {
        if (TextUtils.isEmpty(appToken)) {
            callback.onFail(buildError("AppToken not provided"));
            return;
        }
        if (TextUtils.isEmpty(environment)) {
            callback.onFail(buildError("Environment not provided"));
            return;
        }
        AdjustConfig config = new AdjustConfig(context, appToken, environment);
        config.setLogLevel(HSLogger.isEnabled() ? LogLevel.VERBOSE : LogLevel.INFO);
        OnAttributionChangedListener attributionListener =
                new AttributionChangedListener(callback,
                                               connectorCallback,
                                               externalAttributionListener);
        config.setOnAttributionChangedListener(attributionListener);
        if (context instanceof Application) {
            ((Application) context).registerActivityLifecycleCallbacks(new AdjustLifecycleCallbacks());
        }
        Adjust.onCreate(config);
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

    private static final class AttributionChangedListener implements OnAttributionChangedListener {
        @NonNull
        private final HSComponentCallback callback;
        @NonNull
        private final HSConnectorCallback connectorCallback;
        @Nullable
        private final OnAttributionChangedListener externalAttributionListener;

        public AttributionChangedListener(@NonNull HSComponentCallback callback,
                                          @NonNull HSConnectorCallback connectorCallback,
                                          @Nullable OnAttributionChangedListener externalAttributionListener) {
            this.callback = callback;
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
            callback.onFinished();
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

    private static final class HSEventsDelegate implements HSEventsHandler {
        @NonNull
        private final Context context;

        public HSEventsDelegate(@NonNull Context context) {
            this.context = context;
        }

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
            Adjust.trackEvent(adjustEvent);
        }
    }

    private final class HSIAPValidateDelegate implements HSIAPValidateHandler {
        @NonNull
        private final Context context;
        @Nullable
        private HSIAPValidateCallback pendingCallback;

        public HSIAPValidateDelegate(@NonNull Context context) {
            this.context = context;
        }

        @Override
        public void onValidateInAppPurchase(@NonNull HSInAppPurchase purchase,
                                            @NonNull HSIAPValidateCallback callback) {
            pendingCallback = callback;
            if (purchase != null) {
                String purchasePrice;
                if ((purchasePrice = purchase.getPrice()) != null) {
                    String currency = purchase.getCurrency();
                    Double price = HSUtils.parsePrice(purchasePrice, currency);
                    if (price != null) {
                        // TODO: 15.05.2021 event name
                        AdjustEvent adjustEvent = new AdjustEvent(purchase.toString());
                        adjustEvent.setRevenue(price, currency);
                        adjustEvent.isValid();
                        Adjust.trackEvent(adjustEvent);
                    }
                }
            }
        }
    }

    private static final class AdjustUtils{

        private static Map<String, Object> convertAttributionDataToMap(AdjustAttribution attribution) {
            Map<String, Object> data = new HashMap<>();
            data.put("trackerToken", attribution.trackerToken);
            data.put("trackerName", attribution.trackerName);
            data.put("network", attribution.network);
            data.put("campaign", attribution.campaign);
            data.put("adgroup", attribution.adgroup);
            data.put("creative", attribution.creative);
            data.put("clickLabel", attribution.clickLabel);
            data.put("adid", attribution.adid);
            data.put("costType", attribution.costType);
            data.put("costAmount", attribution.costAmount);
            data.put("costCurrency", attribution.costCurrency);
            return data;
        }
    }
}
