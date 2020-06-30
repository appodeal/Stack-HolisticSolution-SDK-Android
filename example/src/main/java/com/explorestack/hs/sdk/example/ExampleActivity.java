package com.explorestack.hs.sdk.example;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.appodeal.ads.Appodeal;
import com.explorestack.hs.sdk.HSApp;
import com.explorestack.hs.sdk.HSAppInitializeListener;
import com.explorestack.hs.sdk.HSError;

public class ExampleActivity extends AppCompatActivity {

    private static final String TAG = ExampleActivity.class.getSimpleName();

    private final HSAppInitializeListener hsAppInitializeListener = new HSAppInitializeListener() {
        @Override
        public void onAppInitialized() {
            Log.v(TAG, "HSApp: onAppInitialized");
            // HSApp was successfully initialized and now you can initialize Appodeal SDK
            initializeAppodeal(ExampleActivity.this);
        }

        @Override
        public void onAppInitializationFailed(@NonNull HSError error) {
            Log.e(TAG, String.format("HSApp: onAppInitializationFailed (%s)", error.toString()));
            // HSApp initialization failed, more info can be found in 'error' object
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