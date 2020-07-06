package com.explorestack.hs.sdk.example;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.appodeal.ads.Appodeal;
import com.explorestack.hs.sdk.HSApp;
import com.explorestack.hs.sdk.HSAppInitializeListener;
import com.explorestack.hs.sdk.HSError;

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
            // HSApp was initialized and now you can initialize Appodeal SDK
            initializeAppodeal(ExampleActivity.this);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check, if HSApp is initialized. If 'yes', then you can initialize appropriate SDK,
        // otherwise - subscribe to listen to the initialization state
        if (HSApp.isInitialized()) {
            // HSApp was successfully initialized and now you can initialize required SDK
            initializeAppodeal(this);
        } else {
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
     * Storing and processing Appodeal SDK initialization
     */
    private static boolean isAppodealInitialized = false;

    private static void initializeAppodeal(@NonNull Activity activity) {
        if (!isAppodealInitialized) {
            Appodeal.initialize(activity,
                                "c05de97de46bf68a9ede523a580bef97e42692848736ecad",
                                Appodeal.INTERSTITIAL);
            isAppodealInitialized = true;
        }
    }
}