package com.explorestack.hs.sdk;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public class HSApp {

    public static boolean isInitialized() {
        return HSAppInstance.getInstance().isInitialized();
    }

    public static void initialize(@NonNull Context context, @NonNull HSAppConfig config) {
        initialize(context, config, null);
    }

    public static void initialize(@NonNull Context context,
                                  @NonNull HSAppConfig config,
                                  @Nullable final HSAppInitializeListener listener) {
        HSAppInstance.getInstance().initialize(context, config, listener);
    }

    public static void addInitializeListener(@NonNull HSAppInitializeListener listener) {
        HSAppInstance.getInstance().addInitializeListener(listener);
    }

    public static void removeInitializeListener(@NonNull HSAppInitializeListener listener) {
        HSAppInstance.getInstance().removeInitializeListener(listener);
    }

    public static void logEvent(@NonNull String eventName) {
        logEvent(eventName, null);
    }

    public static void logEvent(@NonNull String eventName, @Nullable Map<String, Object> params) {
        HSAppInstance.getInstance().logEvent(eventName, params);
    }

    public static void validateInAppPurchase(String publicKey,
                                             String signature,
                                             String purchaseData,
                                             String price,
                                             String currency,
                                             HashMap<String, String> additionalParameters,
                                             @Nullable HSInAppPurchaseValidateListener listener) {
        HSInAppPurchase purchase = HSInAppPurchase.newBuilder()
                .withPublicKey(publicKey)
                .withSignature(signature)
                .withPurchaseData(purchaseData)
                .withPrice(price)
                .withCurrency(currency)
                .withAdditionalParams(additionalParameters)
                .build();
        validateInAppPurchase(purchase, listener);
    }

    public static void validateInAppPurchase(@NonNull HSInAppPurchase purchase,
                                             @Nullable HSInAppPurchaseValidateListener listener) {
        HSAppInstance.getInstance().validateInAppPurchase(purchase, listener);
    }

    private HSApp() {
    }

}
