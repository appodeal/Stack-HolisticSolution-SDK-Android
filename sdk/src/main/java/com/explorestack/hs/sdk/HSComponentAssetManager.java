package com.explorestack.hs.sdk;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class HSComponentAssetManager {

    private static final String TAG = "HSComponentAssetManager";
    private static final String HS_REGULATORS_ASSET_PATH = "hs_regulators";
    private static final String HS_CONNECTORS_ASSET_PATH = "hs_connectors";
    private static final String HS_SERVICES_ASSET_PATH = "hs_services";
    private static final String KEY_CLASSPATH = "classpath";
    private static final String KEY_NAME = "name";
    private static final String KEY_VERSION = "version";

    private static final Map<String, HSComponentAssetParams> HSRegulatorsAssetParamsMap = new ConcurrentHashMap<>();
    private static final Map<String, HSComponentAssetParams> HSConnectorsAssetParamsMap = new ConcurrentHashMap<>();
    private static final Map<String, HSComponentAssetParams> HSServicesAssetParamsMap = new ConcurrentHashMap<>();

    static Map<String, HSComponentAssetParams> getRegulators() {
        return HSRegulatorsAssetParamsMap;
    }

    static Map<String, HSComponentAssetParams> getConnectors() {
        return HSConnectorsAssetParamsMap;
    }

    static Map<String, HSComponentAssetParams> getServices() {
        return HSServicesAssetParamsMap;
    }

    static void findHSComponents(@NonNull Context context) {
        if (HSRegulatorsAssetParamsMap.isEmpty()) {
            findHSComponents(context, HSRegulatorsAssetParamsMap, HS_REGULATORS_ASSET_PATH);
        }
        if (HSConnectorsAssetParamsMap.isEmpty()) {
            findHSComponents(context, HSConnectorsAssetParamsMap, HS_CONNECTORS_ASSET_PATH);
        }
        if (HSServicesAssetParamsMap.isEmpty()) {
            findHSComponents(context, HSServicesAssetParamsMap, HS_SERVICES_ASSET_PATH);
        }
    }

    private static void findHSComponents(@NonNull Context context,
                                         @NonNull Map<String, HSComponentAssetParams> assetParamsMap,
                                         @NonNull String assetPath) {
        try {
            AssetManager assetManager = context.getAssets();
            if (assetManager == null) {
                return;
            }
            for (String fileName : assetManager.list(assetPath)) {
                String filePath = String.format("%s/%s", assetPath, fileName);
                findHSComponent(assetManager, assetParamsMap, filePath);
            }
        } catch (Throwable t) {
            HSLogger.logError(TAG, t);
        }
    }

    private static void findHSComponent(@NonNull AssetManager assetManager,
                                        @NonNull Map<String, HSComponentAssetParams> assetParamsMap,
                                        @NonNull String filePath) {
        HSComponentAssetParams hsComponentAssetParams = createHSComponentParams(assetManager, filePath);
        if (hsComponentAssetParams != null) {
            assetParamsMap.put(hsComponentAssetParams.getName(), hsComponentAssetParams);
        }
    }

    @Nullable
    private static HSComponentAssetParams createHSComponentParams(@NonNull AssetManager assetManager,
                                                                  @NonNull String filePath) {
        try {
            if (TextUtils.isEmpty(filePath)) {
                return null;
            }

            String fileContent = readAssetByFilePath(assetManager, filePath);
            if (TextUtils.isEmpty(fileContent)) {
                return null;
            }

            assert fileContent != null;
            JSONObject networkAssetConfig = new JSONObject(fileContent);
            String name = networkAssetConfig.optString(KEY_NAME);
            String version = networkAssetConfig.optString(KEY_VERSION);
            String classpath = networkAssetConfig.optString(KEY_CLASSPATH);
            if (TextUtils.isEmpty(name)
                    || TextUtils.isEmpty(version)
                    || TextUtils.isEmpty(classpath)) {
                return null;
            }

            return new HSComponentAssetParams(name, version, classpath);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private static String readAssetByFilePath(@NonNull AssetManager assetManager,
                                              @NonNull String filePath) {
        InputStream inputStream = null;
        try {
            inputStream = assetManager.open(filePath);
            return HSCoreUtils.streamToString(inputStream);
        } catch (Throwable t) {
            HSLogger.logError(TAG, t);
        } finally {
            HSCoreUtils.close(inputStream);
        }
        return null;
    }
}