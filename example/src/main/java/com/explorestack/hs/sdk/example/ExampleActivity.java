package com.explorestack.hs.sdk.example;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.explorestack.hs.sdk.HSApp;
import com.explorestack.hs.sdk.HSAppInitializeListener;
import com.explorestack.hs.sdk.HSError;
import com.explorestack.hs.sdk.HSInAppPurchase;
import com.explorestack.hs.sdk.HSInAppPurchaseValidateListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExampleActivity extends AppCompatActivity {

    private static final String TAG = ExampleActivity.class.getSimpleName();

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
    public void validatePurchase(View view) {
        String price = "10";
        String currency = "USD";
        Map<String, String> additionalEventValues = new HashMap<>();
        additionalEventValues.put("some_parameter", "some_value");

        // Create new HSInAppPurchase
        HSInAppPurchase purchase = HSInAppPurchase.newBuilder()
                .withPublicKey("YOUR_PUBLIC_KEY")
                .withSignature("YOUR_SIGNATURE") //e.g: purchase.getSignature()
                .withPurchaseData("YOUR_PURCHASE_DATA") //e.g: purchase.getOriginalJson()
                .withPrice(price)
                .withCurrency(currency)
                .withAdditionalParams(additionalEventValues)
                .build();

        // Validate InApp purchase
        HSApp.validateInAppPurchase(purchase, new HSInAppPurchaseValidateListener() {
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
}