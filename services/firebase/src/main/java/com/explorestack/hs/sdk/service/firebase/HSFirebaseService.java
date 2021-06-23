package com.explorestack.hs.sdk.service.firebase;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.explorestack.hs.sdk.HSComponentCallback;
import com.explorestack.hs.sdk.HSComponentParams;
import com.explorestack.hs.sdk.HSConnectorCallback;
import com.explorestack.hs.sdk.HSEventsHandler;
import com.explorestack.hs.sdk.HSService;
import com.explorestack.hs.sdk.HSUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class HSFirebaseService extends HSService {

    @Nullable
    private static FirebaseAnalytics firebaseAnalytics;
    @Nullable
    private static String cachedVersion;

    @Nullable
    private List<String> configKeys = null;

    public HSFirebaseService() {
        super("Firebase", getFirebaseVersion(), BuildConfig.COMPONENT_VERSION);
    }

    public static void setFirebaseAnalytics(@Nullable FirebaseAnalytics analytics) {
        firebaseAnalytics = analytics;
    }

    @NonNull
    private static String getFirebaseVersion() {
        if (cachedVersion == null) {
            try {
                Properties properties = new Properties();
                ClassLoader classLoader = HSFirebaseService.class.getClassLoader();
                if (classLoader != null) {
                    properties.load(classLoader.getResourceAsStream("firebase-analytics.properties"));
                    cachedVersion = properties.getProperty("firebase-analytics_client");
                    if (cachedVersion == null) {
                        cachedVersion = properties.getProperty("version");
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        if (cachedVersion == null) {
            cachedVersion = BuildConfig.COMPONENT_SDK_VERSION;
        }
        return cachedVersion;
    }

    @Override
    public void start(@NonNull Context context,
                      @NonNull HSComponentParams params,
                      @NonNull final HSComponentCallback callback,
                      @NonNull final HSConnectorCallback connectorCallback) {
        final FirebaseApp firebaseApp = FirebaseApp.initializeApp(context);
        if (firebaseApp == null) {
            callback.onFail(buildError("Failed to retrieve FirebaseApp"));
            return;
        }
        JSONObject extra = params.getExtra();
        Long minimumFetchIntervalInSeconds = null;
        if (extra.has("expiration_duration")) {
            minimumFetchIntervalInSeconds = extra.optLong("expiration_duration");
        }
        if (extra.has("config_keys")) {
            configKeys = HSUtils.jsonArrayToList(extra.optJSONArray("config_keys"));
        }
        final boolean isConfigKeysProvided = configKeys != null && !configKeys.isEmpty();
        final FirebaseRemoteConfig firebaseRemoteConfig =
                FirebaseRemoteConfig.getInstance(firebaseApp);
        final FirebaseRemoteConfigSettings.Builder configSettingsBuilder =
                new FirebaseRemoteConfigSettings.Builder();
        if (minimumFetchIntervalInSeconds != null) {
            configSettingsBuilder.setMinimumFetchIntervalInSeconds(minimumFetchIntervalInSeconds);
        }
        final FirebaseRemoteConfigSettings configSettings = configSettingsBuilder.build();
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        firebaseRemoteConfig
                .fetchAndActivate()
                .addOnCompleteListener(new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        List<String> keywords = new ArrayList<>();
                        if (isConfigKeysProvided) {
                            for (String key : configKeys) {
                                FirebaseRemoteConfigValue value = firebaseRemoteConfig.getValue(key);
                                keywords.add(value.asString());
                            }
                        } else {
                            for (FirebaseRemoteConfigValue value : firebaseRemoteConfig.getAll().values()) {
                                keywords.add(value.asString());
                            }
                        }
                        connectorCallback.setExtra("keywords", TextUtils.join(", ", keywords));
                        callback.onFinished();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFail(buildError("" + e.getMessage()));
                    }
                });
        if (firebaseAnalytics == null) {
            firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        }
    }

    @Nullable
    @Override
    public HSEventsHandler createEventsHandler(@NonNull Context context) {
        return new HSEventsDelegate();
    }

    private static final class HSEventsDelegate implements HSEventsHandler {

        @Override
        public void onEvent(@NonNull String eventName, @Nullable Map<String, Object> params) {
            if (firebaseAnalytics != null) {
                firebaseAnalytics.logEvent(eventName, HSUtils.mapToBundle(params));
            }
        }
    }
}
