package com.explorestack.hs.sdk.example;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class ExampleBillingClient implements BillingClientStateListener {

    private static final List<String> skuInappList = new ArrayList<>();
    private static final List<String> skuSubsList = new ArrayList<>();

    private final PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (Purchase purchase : purchases) {
                    handlePurchase(purchase);
                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle an error caused by a user cancelling the purchase flow.
            } else {
                // Handle any other error codes.
            }
        }
    };

    private static final long RECONNECT_TIMER_START_MILLISECONDS = 1L * 1000L;
    private static final long RECONNECT_TIMER_MAX_TIME_MILLISECONDS = 1000L * 60L * 15L; // 15 mins
    private static final long SKU_DETAILS_REQUERY_TIME = 1000L * 60L * 60L * 4L; // 4 hours
    private static final Handler handler = new Handler(Looper.getMainLooper());
    private boolean billingSetupComplete = false;

    // Observables that are used to communicate state.
    final private Set<Purchase> purchaseConsumptionInProcess = new HashSet<>();
    final private MutableLiveData<Boolean> billingFlowInProcess = new MutableLiveData<>();
    // how long before the data source tries to reconnect to Google play
    private long reconnectMilliseconds = RECONNECT_TIMER_START_MILLISECONDS;
    // when was the last successful SkuDetailsResponse?
    private long skuDetailsResponseTime = -SKU_DETAILS_REQUERY_TIME;


    // Billing client, connection, cached data
    @NonNull
    private final BillingClient billingClient;
    // known SKUs (used to query sku data and validate responses)
    @NonNull
    private final String knownInappSKU;
    @NonNull
    private final String knownSubscriptionSKU;

    ExampleBillingClient(@NonNull Context context,
                         @NonNull String knownInappSKU,
                         @NonNull String knownSubscriptionSKU) {
        this.knownInappSKU = knownInappSKU;
        this.knownSubscriptionSKU = knownSubscriptionSKU;
        billingClient = BillingClient.newBuilder(context)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();
        billingClient.startConnection(this);
    }

    @Override
    public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

    }

    @Override
    public void onBillingServiceDisconnected() {
        billingSetupComplete = false;
        retryBillingServiceConnectionWithExponentialBackoff();
    }

    /**
     * Retries the billing service connection with exponential backoff, maxing out at the time
     * specified by RECONNECT_TIMER_MAX_TIME_MILLISECONDS.
     */
    private void retryBillingServiceConnectionWithExponentialBackoff() {
        handler.postDelayed(() ->
                        billingClient.startConnection(ExampleBillingClient.this),
                reconnectMilliseconds);
        reconnectMilliseconds = Math.min(reconnectMilliseconds * 2,
                RECONNECT_TIMER_MAX_TIME_MILLISECONDS);
    }


    void start() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                    params.setSkusList(skuInappList).setType(BillingClient.SkuType.INAPP);
                    billingClient.querySkuDetailsAsync(params.build(),
                            new SkuDetailsResponseListener() {
                                @Override
                                public void onSkuDetailsResponse(@NonNull BillingResult billingResult,
                                                                 List<SkuDetails> skuDetailsList) {
                                    // Process the result.
                                }
                            });
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });
    }

    void flow(@NonNull Activity activity, @NonNull SkuDetails skuDetails) {
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build();
        int responseCode = billingClient.launchBillingFlow(activity, billingFlowParams).getResponseCode();
    }

//    void flow(@NonNull SkuDetails skuDetails) {
//        billingClient.queryPurchasesAsync(skuDetails, new PurchasesResponseListener() {
//            @Override
//            public void onQueryPurchasesResponse(@NonNull BillingResult billingResult,
//                                                 @NonNull List<Purchase> list) {
//
//            }
//        });
//    }

    void handlePurchase(Purchase purchase) {
        // Verify the purchase.
        // Ensure entitlement was not already granted for this purchaseToken.
        // Grant entitlement to the user.
        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();

        ConsumeResponseListener listener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(BillingResult billingResult, @NonNull String purchaseToken) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // Handle the success of the consume operation.
                }
            }
        };
        billingClient.consumeAsync(consumeParams, listener);
    }

}
