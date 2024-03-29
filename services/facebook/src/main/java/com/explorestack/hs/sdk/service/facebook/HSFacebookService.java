package com.explorestack.hs.sdk.service.facebook;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.explorestack.hs.sdk.HSComponentCallback;
import com.explorestack.hs.sdk.HSComponentParams;
import com.explorestack.hs.sdk.HSConnectorCallback;
import com.explorestack.hs.sdk.HSEventsHandler;
import com.explorestack.hs.sdk.HSService;
import com.explorestack.hs.sdk.HSUtils;
import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import com.facebook.appevents.AppEventsLogger;

import java.util.Map;

public class HSFacebookService extends HSService {

    private static final String NOT_REPLACED_ID_PLACEHOLDER = "HS_FB_NOT_REPLACED";

    @Nullable
    private static AppEventsLogger eventsLogger;

    public HSFacebookService() {
        super("Facebook", FacebookSdk.getSdkVersion(), BuildConfig.COMPONENT_VERSION);
    }

    public static void setEventsLogger(@Nullable AppEventsLogger logger) {
        eventsLogger = logger;
    }

    @Override
    public void start(@NonNull Context context,
                      @NonNull HSComponentParams params,
                      @NonNull HSComponentCallback callback,
                      @NonNull HSConnectorCallback connectorCallback) {
        try {
            ApplicationInfo applicationInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = applicationInfo.metaData;
            String fbAppId = bundle.getString("com.facebook.sdk.ApplicationId");
            if (TextUtils.isEmpty(fbAppId)
                    || NOT_REPLACED_ID_PLACEHOLDER.equals(fbAppId)) {
                callback.onFail(buildError("R.string.facebook_app_id string resource not overridden"));
                return;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            callback.onFail(buildError("Unknown error"));
            return;
        }
        if (params.isDebugEnabled()) {
            FacebookSdk.setIsDebugEnabled(true);
            FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS);
        }
        if (eventsLogger == null) {
            eventsLogger = AppEventsLogger.newLogger(context);
        }
        callback.onFinished();
    }

    @Nullable
    @Override
    public HSEventsHandler createEventsHandler(@NonNull Context context) {
        return new HSEventsDelegate();
    }

    private static final class HSEventsDelegate implements HSEventsHandler {

        @Override
        public void onEvent(@NonNull String eventName, @Nullable Map<String, Object> params) {
            if (eventsLogger != null) {
                eventsLogger.logEvent(eventName, HSUtils.mapToBundle(params));
            }
        }
    }
}
