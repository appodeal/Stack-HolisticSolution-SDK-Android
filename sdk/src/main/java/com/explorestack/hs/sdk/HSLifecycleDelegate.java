package com.explorestack.hs.sdk;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static android.app.Application.ActivityLifecycleCallbacks;

class HSLifecycleDelegate implements ActivityLifecycleCallbacks {

    private static final Map<HSComponent, ActivityLifecycleCallbacks> lifecycleCallbacks = new ConcurrentHashMap<>();

    public void addCallback(@NonNull HSComponent component,
                            @Nullable ActivityLifecycleCallbacks callbacks) {
        if (callbacks != null) {
            lifecycleCallbacks.put(component, callbacks);
        }
    }

    public void removeCallback(@NonNull HSComponent component) {
        lifecycleCallbacks.remove(component);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        for (ActivityLifecycleCallbacks callbacks : lifecycleCallbacks.values()) {
            callbacks.onActivityCreated(activity, savedInstanceState);
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
        HSAppInstance.get().setTopActivity(activity);
        for (ActivityLifecycleCallbacks callbacks : lifecycleCallbacks.values()) {
            callbacks.onActivityStarted(activity);
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        HSAppInstance.get().setTopActivity(activity);
        for (ActivityLifecycleCallbacks callbacks : lifecycleCallbacks.values()) {
            callbacks.onActivityResumed(activity);
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        for (ActivityLifecycleCallbacks callbacks : lifecycleCallbacks.values()) {
            callbacks.onActivityPaused(activity);
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        for (ActivityLifecycleCallbacks callbacks : lifecycleCallbacks.values()) {
            callbacks.onActivityStopped(activity);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        for (ActivityLifecycleCallbacks callbacks : lifecycleCallbacks.values()) {
            callbacks.onActivitySaveInstanceState(activity, outState);
        }
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        for (ActivityLifecycleCallbacks callbacks : lifecycleCallbacks.values()) {
            callbacks.onActivityDestroyed(activity);
        }
    }

}