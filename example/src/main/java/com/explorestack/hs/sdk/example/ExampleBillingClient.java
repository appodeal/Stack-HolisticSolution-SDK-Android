package com.explorestack.hs.sdk.example;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

class ExampleBillingClient implements BillingClientStateListener,
                                      SkuDetailsResponseListener,
                                      PurchasesUpdatedListener {

    private static final String TAG = ExampleBillingClient.class.getSimpleName();

    private static final Handler handler = new Handler(Looper.getMainLooper());

    private static final long RECONNECT_TIMER_START_MILLISECONDS = 1000L;
    private static final long RECONNECT_TIMER_MAX_TIME_MILLISECONDS = 1000L * 60L * 15L;

    @NonNull
    private final BillingClient billingClient;
    @NonNull
    private final List<String> knownInappSKU;
    @NonNull
    private final List<String> knownSubscriptionSKU;
    @NonNull
    private final PurchasesUpdatedListener externalPurchasesUpdatedListener;

    private final AtomicBoolean billingSetupComplete = new AtomicBoolean(false);
    private final AtomicBoolean billingFlowInProcess = new AtomicBoolean(false);

    private final Map<String, SkuDetails> skuDetailsMap = new HashMap<>();
    private final Set<Purchase> purchaseConsumptionInProcess = new HashSet<>();

    // how long before the data source tries to reconnect to Google play
    private long reconnectMilliseconds = RECONNECT_TIMER_START_MILLISECONDS;

    ExampleBillingClient(@NonNull Context context,
                         @NonNull String knownInappSKU,
                         @NonNull String knownSubscriptionSKU,
                         @NonNull PurchasesUpdatedListener externalPurchasesUpdatedListener) {
        this.knownInappSKU = Collections.singletonList(knownInappSKU);
        this.knownSubscriptionSKU = Collections.singletonList(knownSubscriptionSKU);
        this.externalPurchasesUpdatedListener = externalPurchasesUpdatedListener;
        this.billingClient = BillingClient.newBuilder(context)
                                          .setListener(this)
                                          .enablePendingPurchases()
                                          .build();
        this.billingClient.startConnection(this);
    }

    @Override
    public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
        int responseCode = billingResult.getResponseCode();
        String debugMessage = billingResult.getDebugMessage();
        Log.d(TAG, "HSApp: onBillingSetupFinished: " + responseCode + " " + debugMessage);
        if (responseCode == BillingClient.BillingResponseCode.OK) {
            // The billing client is ready. You can query purchases here.
            // This doesn't mean that your app is set up correctly in the console -- it just
            // means that you have a connection to the Billing service.
            reconnectMilliseconds = RECONNECT_TIMER_START_MILLISECONDS;
            billingSetupComplete.set(true);
            querySkuDetailsAsync();
            refreshPurchasesAsync();
        } else {
            retryBillingServiceConnectionWithExponentialBackoff();
        }
    }

    @Override
    public void onBillingServiceDisconnected() {
        Log.d(TAG, "HSApp: onBillingServiceDisconnected");
        billingSetupComplete.set(false);
        retryBillingServiceConnectionWithExponentialBackoff();
    }

    private void querySkuDetailsAsync() {
        billingClient.querySkuDetailsAsync(SkuDetailsParams.newBuilder()
                .setType(BillingClient.SkuType.INAPP)
                .setSkusList(knownInappSKU)
                .build(), this);
        billingClient.querySkuDetailsAsync(SkuDetailsParams.newBuilder()
                .setType(BillingClient.SkuType.SUBS)
                .setSkusList(knownSubscriptionSKU)
                .build(), this);
    }

    @Override
    public void onSkuDetailsResponse(@NonNull BillingResult billingResult,
                                     @Nullable List<SkuDetails> skuDetailsList) {
        int responseCode = billingResult.getResponseCode();
        String debugMessage = billingResult.getDebugMessage();
        switch (responseCode) {
            case BillingClient.BillingResponseCode.OK:
                Log.d(TAG, "HSApp: onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                if (skuDetailsList == null || skuDetailsList.isEmpty()) {
                    Log.e(TAG, "HSApp: [Error]: onSkuDetailsResponse: " +
                            "Found null or empty SkuDetails. " +
                            "Check to see if the SKUs you requested are correctly published " +
                            "in the Google Play Console.");
                } else {
                    for (SkuDetails skuDetails : skuDetailsList) {
                        skuDetailsMap.put(skuDetails.getSku(), skuDetails);
                    }
                }
                break;
            case BillingClient.BillingResponseCode.SERVICE_DISCONNECTED:
            case BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE:
            case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
            case BillingClient.BillingResponseCode.ITEM_UNAVAILABLE:
            case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
            case BillingClient.BillingResponseCode.ERROR:
            default:
                Log.e(TAG, "HSApp: [Error]: onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                break;
            case BillingClient.BillingResponseCode.USER_CANCELED:
                Log.d(TAG, "HSApp: onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                break;
            // These response codes are not expected.
            case BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED:
            case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
            case BillingClient.BillingResponseCode.ITEM_NOT_OWNED:
                break;
        }
    }

    private void refreshPurchasesAsync() {
        billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP,
                (billingResult, list) -> {
                    if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                        Log.e(TAG, "HSApp: [Error]: Problem getting purchases: "
                                + billingResult.getDebugMessage());
                    } else {
                        processPurchaseList(list);
                    }
                });
        billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS,
                (billingResult, list) -> {
                    if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                        Log.e(TAG, "HSApp: [Error]: Problem getting subscriptions: "
                                + billingResult.getDebugMessage());
                    } else {
                        processPurchaseList(list);
                    }
                });
        Log.d(TAG, "HSApp: Refreshing purchases started.");
    }

    private void retryBillingServiceConnectionWithExponentialBackoff() {
        handler.postDelayed(() ->
                        billingClient.startConnection(ExampleBillingClient.this),
                reconnectMilliseconds);
        reconnectMilliseconds = Math.min(reconnectMilliseconds * 2,
                RECONNECT_TIMER_MAX_TIME_MILLISECONDS);
    }

    void flow(@NonNull Activity activity, @NonNull String sku) {
        SkuDetails skuDetails = skuDetailsMap.get(sku);
        if (skuDetails != null) {
            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDetails)
                    .build();
            BillingResult billingResult = billingClient.launchBillingFlow(activity, billingFlowParams);
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                billingFlowInProcess.set(true);
            } else {
                Log.e(TAG, "HSApp: [Error]: Billing failed: + " + billingResult.getDebugMessage());
            }
        } else {
            Log.e(TAG, "HSApp: [Error]: SkuDetails not found for: " + sku);
        }
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult,
                                   @Nullable List<Purchase> list) {
        switch (billingResult.getResponseCode()) {
            case BillingClient.BillingResponseCode.OK:
                if (list != null) {
                    processPurchaseList(list);
                } else {
                    Log.d(TAG, "HSApp: Null Purchase List Returned from OK response!");
                }
                break;
            case BillingClient.BillingResponseCode.USER_CANCELED:
                Log.d(TAG, "HSApp: onPurchasesUpdated: User canceled the purchase");
                break;
            case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                Log.d(TAG, "HSApp: onPurchasesUpdated: The user already owns this item");
                break;
            case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
                Log.e(TAG, "HSApp: [Error]: onPurchasesUpdated: Developer error means that Google Play " +
                        "does not recognize the configuration. If you are just getting started, " +
                        "make sure you have configured the application correctly in the " +
                        "Google Play Console. The SKU product ID must match and the APK you " +
                        "are using must be signed with release keys.");
                break;
            default:
                Log.d(TAG, "HSApp: BillingResult [" + billingResult.getResponseCode() + "]: "
                        + billingResult.getDebugMessage());
                break;
        }
        billingFlowInProcess.set(false);
        externalPurchasesUpdatedListener.onPurchasesUpdated(billingResult, list);
    }

    private void processPurchaseList(@Nullable List<Purchase> purchases) {
        if (purchases != null) {
            for (final Purchase purchase : purchases) {
                int purchaseState = purchase.getPurchaseState();
                if (purchaseState == Purchase.PurchaseState.PURCHASED) {
                    boolean isConsumable = false;
                    for (String sku : purchase.getSkus()) {
                        if (knownInappSKU.contains(sku)) {
                            isConsumable = true;
                        } else {
                            if (isConsumable) {
                                Log.e(TAG, "HSApp: [Error]: Purchase cannot contain a mixture of consumable" +
                                        "and non-consumable items: " + purchase.getSkus().toString());
                                isConsumable = false;
                                break;
                            }
                        }
                    }
                    if (isConsumable) {
                        consumePurchase(purchase);
                    } else if (!purchase.isAcknowledged()) {
                        acknowledgePurchase(purchase);
                    }
                }
            }
        } else {
            Log.d(TAG, "HSApp: Empty purchase list.");
        }
    }

    private void consumePurchase(@NonNull Purchase purchase) {
        if (purchaseConsumptionInProcess.contains(purchase)) {
            return;
        }
        purchaseConsumptionInProcess.add(purchase);
        billingClient.consumeAsync(ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build(), (billingResult, s) -> {
            purchaseConsumptionInProcess.remove(purchase);
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "HSApp: Consumption successful. Delivering entitlement.");
            } else {
                Log.e(TAG, "HSApp: [Error]: Error while consuming: " + billingResult.getDebugMessage());
            }
            Log.d(TAG, "HSApp: End consumption flow.");
        });
    }

    private void acknowledgePurchase(@NonNull Purchase purchase) {
        billingClient.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build(), billingResult -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "HSApp: Acknowledge successful.");
            } else {
                Log.e(TAG, "HSApp: [Error]: Error while acknowledge: " + billingResult.getDebugMessage());
            }
            Log.d(TAG, "HSApp: End acknowledge flow.");
        });
    }

    @Nullable
    SkuDetails getSkuDetails(@Nullable String sku) {
        if (!TextUtils.isEmpty(sku)) {
            return skuDetailsMap.get(sku);
        }
        return null;
    }

    void resume() {
        Log.d(TAG, "HSApp: Billing Resume");
        if (billingSetupComplete.get() && !billingFlowInProcess.get()) {
            refreshPurchasesAsync();
        }
    }
}
