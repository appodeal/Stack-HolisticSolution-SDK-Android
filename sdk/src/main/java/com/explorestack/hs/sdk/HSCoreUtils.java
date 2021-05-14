package com.explorestack.hs.sdk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.security.NetworkSecurityPolicy;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.webkit.WebSettings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.Flushable;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

class HSCoreUtils {

    private static final String TAG = "HSCoreUtils";
    private static final String UUID_ID = "uuid";
    private static final String SHARED_PREFERENCES_NAME = "ad_core_preferences";

    @NonNull
    private static final Handler backgroundHandler;
    @Nullable
    private static String cachedHttpAgentString = null;

    static {
        HandlerThread thread = new HandlerThread("BackgroundHandlerThread");
        thread.start();
        backgroundHandler = new Handler(thread.getLooper());
    }

    static void onBackgroundThread(Runnable runnable) {
        onBackgroundThread(runnable, 0);
    }

    static void onBackgroundThread(Runnable runnable, long delay) {
        backgroundHandler.postDelayed(runnable, delay);
    }

    static void cancelBackgroundThreadTask(Runnable runnable) {
        backgroundHandler.removeCallbacks(runnable);
    }

    static void startTimeout(long timeoutMs, @NonNull TimerTask callback) {
        new Timer().schedule(callback, timeoutMs);
    }

    static void flush(Flushable flushable) {
        try {
            if (flushable != null) {
                flushable.flush();
            }
        } catch (Exception e) {
            HSLogger.logError(TAG, e);
        }
    }

    static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception e) {
            HSLogger.logError(TAG, e);
        }
    }

    static String getAdvertisingUUID(@NonNull Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        if (sharedPref.contains(UUID_ID)) {
            return sharedPref.getString(UUID_ID, null);
        } else {
            String uuid = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(UUID_ID, uuid);
            editor.apply();
            return uuid;
        }
    }

    @SafeVarargs
    static Object invokeMethodByName(Object object,
                                     String methodName,
                                     Pair<Class<?>, Object>... parameterPairs) throws Exception {
        return invokeMethodByName(object, object.getClass(), methodName, parameterPairs);
    }

    @SafeVarargs
    static Object invokeMethodByName(Object object,
                                     Class<?> clazz,
                                     String methodName,
                                     Pair<Class<?>, Object>... parameterPairs) throws Exception {
        Class<?>[] parameterTypes;
        Object[] parameterObject;

        if (parameterPairs != null) {
            parameterTypes = new Class[parameterPairs.length];
            parameterObject = new Object[parameterPairs.length];

            for (int i = 0; i < parameterPairs.length; i++) {
                parameterTypes[i] = parameterPairs[i].first;
                parameterObject[i] = parameterPairs[i].second;
            }
        } else {
            parameterTypes = null;
            parameterObject = null;
        }

        int maxStep = 10;
        while (maxStep > 0) {
            if (clazz == null) {
                break;
            }

            try {
                Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
                method.setAccessible(true);
                return method.invoke(object, parameterObject);
            } catch (NoSuchMethodException e) {
                clazz = clazz.getSuperclass();
            } catch (IllegalAccessException e) {
                break;
            } catch (InvocationTargetException e) {
                break;
            }

            maxStep--;
        }

        return null;
    }

    static String streamToString(@NonNull InputStream inputStream) throws Exception {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString("UTF-8");
    }

    @SuppressLint("MissingPermission")
    @NonNull
    static HSConnectionData getConnectionData(@NonNull Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        String connectionType = "unknown";
        String connectionSubtype = null;
        boolean fast = false;
        if (info != null && info.isConnected()) {
            connectionType = info.getTypeName();
            connectionSubtype = info.getSubtypeName();
            switch (info.getType()) {
                case ConnectivityManager.TYPE_MOBILE:
                    switch (info.getSubtype()) {
                        case TelephonyManager.NETWORK_TYPE_CDMA: // ~ 14-64 kbps
                        case TelephonyManager.NETWORK_TYPE_IDEN: // ~25 kbps
                        case TelephonyManager.NETWORK_TYPE_1xRTT: // ~ 50-100 kbps
                        case TelephonyManager.NETWORK_TYPE_GPRS: // ~ 100 kbps
                        case TelephonyManager.NETWORK_TYPE_EDGE: // ~ 50-100 kbps
                            fast = false;
                            break;
                        case TelephonyManager.NETWORK_TYPE_EVDO_0: // ~ 400-1000 kbps
                        case TelephonyManager.NETWORK_TYPE_EVDO_A: // ~ 600-1400 kbps
                        case TelephonyManager.NETWORK_TYPE_EVDO_B: // ~ 5 Mbps
                        case TelephonyManager.NETWORK_TYPE_UMTS: // ~ 400-7000 kbps
                        case TelephonyManager.NETWORK_TYPE_EHRPD: // ~ 1-2 Mbps
                        case TelephonyManager.NETWORK_TYPE_HSPA: // ~ 700-1700 kbps
                        case TelephonyManager.NETWORK_TYPE_HSDPA: // ~ 2-14 Mbps
                        case TelephonyManager.NETWORK_TYPE_HSUPA: // ~ 1-23 Mbps
                        case TelephonyManager.NETWORK_TYPE_HSPAP: // ~ 10-20 Mbps
                        case TelephonyManager.NETWORK_TYPE_LTE: // ~ 10+ Mbps
                            fast = true;
                            break;
                        case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                        default:
                            fast = false;
                            break;
                    }
                    break;
                case ConnectivityManager.TYPE_WIFI:
                    fast = true;
                    break;
                case ConnectivityManager.TYPE_WIMAX:
                    fast = true;
                    break;
                case ConnectivityManager.TYPE_ETHERNET:
                    fast = true;
                    break;
                case ConnectivityManager.TYPE_BLUETOOTH:
                    fast = false;
                    break;
                default:
                    fast = false;
                    break;
            }
        }

        if (connectionType != null) {
            if (connectionType.equals("CELLULAR")) {
                connectionType = "MOBILE";
            }
            connectionType = connectionType.toLowerCase(Locale.ENGLISH);
        }
        if (connectionSubtype != null) {
            connectionSubtype = connectionSubtype.toLowerCase(Locale.ENGLISH);
            if (connectionSubtype.isEmpty()) {
                connectionSubtype = null;
            }

        }
        return new HSConnectionData(connectionType, connectionSubtype, fast);
    }

    static boolean hasUsesCleartextTraffic() {
        return Build.VERSION.SDK_INT < 23 || NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted();
    }

    static boolean isTablet(@NonNull Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        double width = metrics.widthPixels / metrics.xdpi;
        double height = metrics.heightPixels / metrics.ydpi;
        double screenSize = Math.sqrt(width * width + height * height);
        return screenSize >= 6.6d;
    }

    @Nullable
    static String getHttpAgentString(@Nullable final Context context) {
        if (cachedHttpAgentString != null) {
            return cachedHttpAgentString;
        }
        if (context == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                cachedHttpAgentString = WebSettings.getDefaultUserAgent(context);
            } catch (Throwable e) {
                HSLogger.logError(TAG, e);
            }
        }
        if (cachedHttpAgentString == null) {
            cachedHttpAgentString = generateHttpAgentString(context);
        }
        if (cachedHttpAgentString == null) {
            cachedHttpAgentString = getSystemHttpAgentString();
        }
        // We shouldn't try to obtain http agent string again after all possible methods has failed
        if (cachedHttpAgentString == null) {
            cachedHttpAgentString = "";
        }
        return cachedHttpAgentString;
    }

    @Nullable
    private static String generateHttpAgentString(@NonNull Context context) {
        try {
            StringBuilder builder = new StringBuilder("Mozilla/5.0");
            builder.append(" (Linux; Android ")
                    .append(Build.VERSION.RELEASE)
                    .append("; ")
                    .append(Build.MODEL)
                    .append(" Build/")
                    .append(Build.ID)
                    .append("; wv)");
            // This AppleWebKit version supported from Chrome 68, and it's probably should for for
            // most devices
            builder.append(" AppleWebKit/537.36 (KHTML, like Gecko)");
            // This version is provided starting from Android 4.0
            builder.append(" Version/4.0");
            PackageManager pm = context.getPackageManager();
            try {
                PackageInfo pi = pm.getPackageInfo("com.google.android.webview", 0);
                builder.append(" Chrome/").append(pi.versionName);
            } catch (Throwable e) {
                HSLogger.logError(TAG, e);
            }
            builder.append(" Mobile");
            try {
                ApplicationInfo appInfo = context.getApplicationInfo();
                PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
                builder.append(" ")
                        .append(appInfo.labelRes == 0
                                ? appInfo.nonLocalizedLabel.toString()
                                : context.getString(appInfo.labelRes))
                        .append("/")
                        .append(packageInfo.versionName);
            } catch (Throwable e) {
                HSLogger.logError(TAG, e);
            }
            return builder.toString();
        } catch (Throwable e) {
            return null;
        }
    }

    private static String getSystemHttpAgentString() {
        String result = null;
        try {
            result = System.getProperty("http.agent", "");
        } catch (Throwable e) {
            HSLogger.logError(TAG, e);
        }
        return result;
    }
}
