package com.explorestack.hs.sdk;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.explorestack.hs.sdk.HSCoreUtils.invokeMethodByName;

class HSAdvertisingInfo {

    private static final String TAG = "HSAdvertisingInfo";

    private static final List<HSAdvertisingProfile> supportedAdvertisingProfiles = new ArrayList<>();

    static {
        supportedAdvertisingProfiles.add(new AndroidAdvertisingProfile());
        supportedAdvertisingProfiles.add(new AmazonAdvertisingProfile());
        supportedAdvertisingProfiles.add(new HMSAdvertisingProfile());
    }

    @Nullable
    static HSAdvertisingProfile updateInfo(@NonNull Context context) {
        for (HSAdvertisingProfile profile : supportedAdvertisingProfiles) {
            HSLogger.logInfo(TAG, "Trying: " + profile.getName());
            try {
                if (profile.isEnabled(context)) {
                    profile.extractParams(context);
                    if (!TextUtils.isEmpty(profile.getId(context))) {
                        HSLogger.logInfo(TAG, "Success: " + profile.getName());
                        return profile;
                    } else {
                        HSLogger.logInfo(TAG, "Fail (id not retrieved): " + profile.getName());
                    }
                } else {
                    HSLogger.logInfo(TAG, "Not available: " + profile.getName());
                }
            } catch (Throwable ignore) {
                HSLogger.logInfo(TAG, "Not available: " + profile.getName());
            }
        }
        HSLogger.logInfo(TAG, "Error: no matching profiles");
        return null;
    }

    private static final class AndroidAdvertisingProfile extends HSAdvertisingProfile {

        private static final String ADVERTISING_CLIENT_CLASS = "com.google.android.gms.ads.identifier.AdvertisingIdClient";

        private Class<?> advertisingClientClass;

        AndroidAdvertisingProfile() {
            super("Google Play Services");
        }

        @Override
        void extractParams(Context context) throws Throwable {
            Object advertisingIdInfoObject =
                    invokeMethodByName(advertisingClientClass,
                                       advertisingClientClass,
                                       "getAdvertisingIdInfo",
                                       new Pair<Class<?>, Object>(Context.class, context));
            if (advertisingIdInfoObject != null) {
                id = (String) invokeMethodByName(advertisingIdInfoObject, "getId");
                limitAdTrackingEnabled = (boolean) invokeMethodByName(advertisingIdInfoObject,
                                                                      "isLimitAdTrackingEnabled");
            }
        }

        @Override
        boolean isEnabled(@NonNull Context context) throws Throwable {
            advertisingClientClass = Class.forName(ADVERTISING_CLIENT_CLASS);
            return true;
        }
    }

    private static final class AmazonAdvertisingProfile extends HSAdvertisingProfile {

        AmazonAdvertisingProfile() {
            super("Amazon");
        }

        @Override
        void extractParams(Context context) throws Throwable {
            ContentResolver contentResolver = context.getContentResolver();
            id = Settings.Secure.getString(contentResolver, "advertising_id");
            limitAdTrackingEnabled = Settings.Secure.getInt(contentResolver, "limit_ad_tracking") != 0;
        }

        @Override
        boolean isEnabled(@NonNull Context context) throws Throwable {
            return "Amazon".equals(Build.MANUFACTURER);
        }
    }

    private static final class HMSAdvertisingProfile extends HSAdvertisingProfile {

        private static final String ADVERTISING_CLIENT_CLASS = "com.huawei.hms.ads.identifier.AdvertisingIdClient";
        private static final String ADVERTISING_CLIENT_INFO_CLASS = ADVERTISING_CLIENT_CLASS + "$Info";

        private Class<?> advertisingClientClass;

        HMSAdvertisingProfile() {
            super("HMS");
        }

        @Override
        void extractParams(Context context) throws Throwable {
            Object advertisingIdInfoObject =
                    invokeMethodByName(advertisingClientClass,
                                       advertisingClientClass,
                                       "getAdvertisingIdInfo",
                                       new Pair<Class<?>, Object>(Context.class, context));
            if (advertisingIdInfoObject != null) {
                Class<?> infoClass = Class.forName(ADVERTISING_CLIENT_INFO_CLASS);
                id = (String) invokeMethodByName(advertisingIdInfoObject, infoClass, "getId");
                limitAdTrackingEnabled = (boolean) invokeMethodByName(advertisingIdInfoObject,
                                                                      infoClass,
                                                                      "isLimitAdTrackingEnabled");
            }
        }

        @Override
        boolean isEnabled(@NonNull Context context) throws Throwable {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                advertisingClientClass = Class.forName(ADVERTISING_CLIENT_CLASS);
                return true;
            } else {
                return false;
            }
        }
    }

}