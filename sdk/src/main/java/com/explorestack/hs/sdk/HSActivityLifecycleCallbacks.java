package com.explorestack.hs.sdk;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

class HSActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        // ignore
    }

    @Override
    public void onActivityStarted(Activity activity) {
        HSAppInstance.get().setTopActivity(activity);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        HSAppInstance.get().setTopActivity(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        // ignore
    }

    @Override
    public void onActivityStopped(Activity activity) {
        // ignore
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        // ignore
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        // ignore
    }

}