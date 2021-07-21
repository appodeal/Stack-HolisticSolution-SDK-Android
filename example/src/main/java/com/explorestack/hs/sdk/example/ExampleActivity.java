package com.explorestack.hs.sdk.example;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.explorestack.hs.sdk.HSApp;
import com.explorestack.hs.sdk.HSAppInitializeListener;
import com.explorestack.hs.sdk.HSError;
import com.explorestack.hs.sdk.HSInAppPurchase;
import com.explorestack.hs.sdk.HSInAppPurchaseValidateListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.explorestack.hs.sdk.HSInAppPurchase.PurchaseType;

public class ExampleActivity extends AppCompatActivity {

    private static final String TAG = ExampleActivity.class.getSimpleName();

    private static final String SKU_INFINITE_ACCESS_MONTHLY = "infinite_access_monthly";
    private static final String SKU_COINS = "coins";

    private ExampleBillingClient billingClient;

    private final HSAppInitializeListener hsAppInitializeListener = new HSAppInitializeListener() {
        @Override
        public void onAppInitialized(@Nullable List<HSError> errors) {
            Log.v(TAG, "HSApp: onAppInitialized");
            if (errors != null) {
                for (HSError error : errors) {
                    Log.e(TAG, "HSApp: [Error]: " + error.toString());
                }
            }
        }
    };

    private final PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(@NonNull BillingResult billingResult,
                                       @Nullable List<Purchase> purchaseList) {
            Log.v(TAG, "HSApp: onPurchasesUpdated");
            int responseCode = billingResult.getResponseCode();
            if (responseCode == BillingClient.BillingResponseCode.OK && purchaseList != null) {
                for (Purchase purchase : purchaseList) {
                    validatePurchase(purchase);
                }
            } else {
                Log.d(TAG, "HSApp: [Error]: Null Purchase List Returned from OK response!");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check, if HSApp is initialized. If 'yes', then you can initialize appropriate SDK,
        // otherwise - subscribe to listen to the initialization state
        if (!HSApp.isInitialized()) {
            // Attach HSApp initialization listener
            HSApp.addInitializeListener(hsAppInitializeListener);
            // Start HSApp initialization if it's not started yet
            ExampleApplication.initializeHSApp(getApplicationContext());
        }

        billingClient = new ExampleBillingClient(getApplicationContext(),
                                                 SKU_COINS,
                                                 SKU_INFINITE_ACCESS_MONTHLY,
                                                 purchasesUpdatedListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        billingClient.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove HSApp initialize listener, since HSApp store strong reference to provided listener
        HSApp.removeInitializeListener(hsAppInitializeListener);
    }

    /**
     * Example of events logging
     */
    public void logEvent(View view) {
        // Create map of event parameters if required
        Map<String, Object> params = new HashMap<>();
        params.put("example_param_1", "Param1 value");
        params.put("example_param_2", 123);

        // Send event to all connected analytics services
        HSApp.logEvent("hs_sdk_example_test_event", params);
    }

    /**
     * Example of purchase validating
     */
    public void flowInAppPurchase(View view) {
        billingClient.flow(this, SKU_COINS);
    }

    public void flowSubsPurchase(View view) {
        billingClient.flow(this, SKU_INFINITE_ACCESS_MONTHLY);
    }

    private void validatePurchase(@NonNull Purchase purchase) {
        String sku = firstOrNull(purchase.getSkus());
        SkuDetails skuDetails = billingClient.getSkuDetails(sku);
        if (skuDetails == null) {
            Log.d(TAG, "HSApp: [Error]: SkuDetails is null");
            return;
        }

        String price = skuDetails.getPrice();
        String currency = skuDetails.getPriceCurrencyCode();
        Map<String, String> additionalEventValues = new HashMap<>();
        additionalEventValues.put("some_parameter", "some_value");

        // Create new HSInAppPurchase
        PurchaseType purchaseType = PurchaseType.valueOf(skuDetails.getType().toUpperCase());
        HSInAppPurchase hsPurchase = HSInAppPurchase.newBuilder(purchaseType)
                .withPublicKey("YOUR_PUBLIC_KEY")
                .withSignature(purchase.getSignature())
                .withPurchaseData(purchase.getOriginalJson())
                .withPurchaseToken(purchase.getPurchaseToken())
                .withPurchaseTimestamp(purchase.getPurchaseTime())
                .withOrderId(purchase.getOrderId())
                .withSku(sku)
                .withPrice(price)
                .withCurrency(currency)
                .withAdditionalParams(additionalEventValues)
                .build();

        // Validate InApp purchase
        HSApp.validateInAppPurchase(hsPurchase, new HSInAppPurchaseValidateListener() {
            @Override
            public void onInAppPurchaseValidateSuccess(@NonNull HSInAppPurchase purchase,
                                                       @Nullable List<HSError> errors) {
                Log.v(TAG, "HSApp: onInAppPurchaseValidateSuccess");
                if (errors != null) {
                    for (HSError error : errors) {
                        Log.e(TAG, "HSApp: [onInAppPurchaseValidateFail]: " + error.toString());
                    }
                }
            }

            @Override
            public void onInAppPurchaseValidateFail(@NonNull List<HSError> errors) {
                Log.v(TAG, "HSApp: onInAppPurchaseValidateFail");
                for (HSError error : errors) {
                    Log.e(TAG, "HSApp: [onInAppPurchaseValidateFail]: " + error.toString());
                }
            }
        });
    }

    @Nullable
    private <T> T firstOrNull(@Nullable List<T> list) {
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }
}