package com.explorestack.hs.sdk.service.appsflyer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class HSAppsflyerServiceActivityTracker implements Application.ActivityLifecycleCallbacks {

    @SuppressLint("StaticFieldLeak")
    private static HSAppsflyerServiceActivityTracker instance;

    public static HSAppsflyerServiceActivityTracker getInstance() {
        if (instance == null) {
            instance = new HSAppsflyerServiceActivityTracker();
        }
        return instance;
    }

    @Nullable
    private Activity lastCreatedActivity;

    private boolean isStarted = false;

    public void start(@Nullable Context context) {
        if (context != null && !isStarted) {
            isStarted = true;
            final Application application = (Application) context.getApplicationContext();
            application.registerActivityLifecycleCallbacks(this);
        }
    }

    public void stop(@Nullable Context context) {
        if (context != null) {
            final Application application = (Application) context.getApplicationContext();
            application.unregisterActivityLifecycleCallbacks(this);
            isStarted = false;
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        lastCreatedActivity = activity;
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        //ignore
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        //ignore
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        //ignore
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        //ignore
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        //ignore
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        if (activity == lastCreatedActivity) {
            lastCreatedActivity = null;
        }
    }

    @Nullable
    public Activity getLastCreatedActivity() {
        return lastCreatedActivity;
    }
}
