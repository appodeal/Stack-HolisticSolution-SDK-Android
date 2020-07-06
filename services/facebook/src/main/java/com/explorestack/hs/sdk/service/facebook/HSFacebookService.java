package com.explorestack.hs.sdk.service.facebook;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.explorestack.hs.sdk.HSAppParams;
import com.explorestack.hs.sdk.HSComponentCallback;
import com.explorestack.hs.sdk.HSConnectorCallback;
import com.explorestack.hs.sdk.HSEventsCallback;
import com.explorestack.hs.sdk.HSService;
import com.explorestack.hs.sdk.HSUtils;
import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import com.facebook.appevents.AppEventsLogger;

import java.util.Map;

public class HSFacebookService extends HSService {

    private static final String NOT_REPLACED_ID_PLACEHOLDER = "HS_FB_NOT_REPLACED";

    @Nullable
    private AppEventsLogger eventsLogger;

    public HSFacebookService() {
        super("Facebook", FacebookSdk.getSdkVersion());
    }

    public void setEventsLogger(@Nullable AppEventsLogger eventsLogger) {
        this.eventsLogger = eventsLogger;
    }

    @Override
    public void start(@NonNull Context context,
                      @NonNull HSAppParams params,
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
            String fbLoginProtocolScheme = context.getString(R.string.fb_login_protocol_scheme);
            if (TextUtils.isEmpty(fbLoginProtocolScheme)
                    || NOT_REPLACED_ID_PLACEHOLDER.equals(fbLoginProtocolScheme)) {
                callback.onFail(
                        buildError(
                                "R.string.fb_login_protocol_scheme string resource not overridden"));
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
    public HSEventsCallback getEventsCallback(@NonNull Context context) {
        return new HSEventsDelegate();
    }

    private final class HSEventsDelegate implements HSEventsCallback {

        @Override
        public void onEvent(@NonNull String eventName, @Nullable Map<String, Object> params) {
            if (eventsLogger != null) {
                eventsLogger.logEvent(eventName, HSUtils.mapToBundle(params));
            }
        }
    }
}
