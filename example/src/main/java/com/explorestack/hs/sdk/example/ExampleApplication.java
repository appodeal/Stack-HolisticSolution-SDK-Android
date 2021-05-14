package com.explorestack.hs.sdk.example;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.multidex.MultiDexApplication;

import com.appodeal.ads.Appodeal;
import com.explorestack.hs.sdk.HSApp;
import com.explorestack.hs.sdk.HSAppConfig;
import com.explorestack.hs.sdk.HSAppInitializeListener;
import com.explorestack.hs.sdk.HSError;

import java.util.List;

public class ExampleApplication extends MultiDexApplication {

    private static final String TAG = ExampleApplication.class.getSimpleName();

    private static boolean isInitializingHsApp = false;

    @Override
    public void onCreate() {
        super.onCreate();
        //Initialize HSApp
        initializeHSApp(this);
    }

    static void initializeHSApp(@NonNull Context context) {
        if (isInitializingHsApp) {
            return;
        }
        isInitializingHsApp = true;

        //Create HSApp configuration
        HSAppConfig appConfig = new HSAppConfig()
                .setDebugEnabled(true)
                .setLoggingEnabled(true)
                .setAppKey("c05de97de46bf68a9ede523a580bef97e42692848736ecad")
                .setAdType(Appodeal.INTERSTITIAL);

        //Initialize HSApp
        HSApp.initialize(context, appConfig, new HSAppInitializeListener() {
            @Override
            public void onAppInitialized(@Nullable List<HSError> errors) {
                if (errors != null) {
                    for (HSError error : errors) {
                        Log.e(TAG, "HSApp: [Error]: " + error.toString());
                    }
                }
                //HSApp initialization finished, now you can initialize required SDK
                isInitializingHsApp = false;
            }
        });
    }
}
