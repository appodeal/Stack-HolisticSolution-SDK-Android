package com.explorestack.hs.sdk.service.firebase;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.explorestack.hs.sdk.HSAppParams;
import com.explorestack.hs.sdk.HSConnectorCallback;
import com.explorestack.hs.sdk.HSService;
import com.explorestack.hs.sdk.HSServiceCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HSFirebaseService extends HSService {

    @Nullable
    private final Map<String, Object> targetValuesKeys;

    private final Long minimumFetchIntervalInSeconds;

    public HSFirebaseService() {
        this(null, null);
    }

    public HSFirebaseService(@Nullable Map<String, Object> targetValuesKeys) {
        this(targetValuesKeys, null);
    }

    public HSFirebaseService(@Nullable Map<String, Object> targetValuesKeys,
                             @Nullable Long minimumFetchIntervalInSeconds) {
        super("Firebase", null);
        this.targetValuesKeys = targetValuesKeys;
        this.minimumFetchIntervalInSeconds = minimumFetchIntervalInSeconds;
    }

    @Override
    public void start(@NonNull Context context,
                      @NonNull HSAppParams params,
                      @NonNull final HSServiceCallback callback,
                      @NonNull final HSConnectorCallback connectorCallback) {
        final FirebaseApp firebaseApp = FirebaseApp.initializeApp(context);
        if (firebaseApp == null) {
            callback.onFail(buildError("Failed to retrieve FirebaseApp"));
            return;
        }
        final boolean isTargetValuesKeysProvided =
                targetValuesKeys != null && !targetValuesKeys.isEmpty();
        final FirebaseRemoteConfig firebaseRemoteConfig =
                FirebaseRemoteConfig.getInstance(firebaseApp);
        final FirebaseRemoteConfigSettings.Builder configSettingsBuilder =
                new FirebaseRemoteConfigSettings.Builder();
        if (minimumFetchIntervalInSeconds != null) {
            configSettingsBuilder.setMinimumFetchIntervalInSeconds(minimumFetchIntervalInSeconds);
        }
        final FirebaseRemoteConfigSettings configSettings = configSettingsBuilder.build();
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        if (isTargetValuesKeysProvided) {
            firebaseRemoteConfig.setDefaultsAsync(targetValuesKeys);
        }
        firebaseRemoteConfig
                .fetchAndActivate()
                .addOnCompleteListener(new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        List<String> keywords = new ArrayList<>();
                        if (isTargetValuesKeysProvided) {
                            for (String key : targetValuesKeys.keySet()) {
                                FirebaseRemoteConfigValue value = firebaseRemoteConfig.getValue(key);
                                keywords.add(value.asString());
                            }
                        } else {
                            for (FirebaseRemoteConfigValue value
                                    : firebaseRemoteConfig.getAll().values()) {
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
    }
}
