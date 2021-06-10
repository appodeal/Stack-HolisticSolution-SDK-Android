package com.explorestack.hs.sdk;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

class HSComponentAssetManager {

    private static final String TAG = "HSComponentAssetManager";
    private static final String HS_REGULATORS_ASSET_PATH = "hs_regulators";
    private static final String HS_CONNECTORS_ASSET_PATH = "hs_connectors";
    private static final String HS_SERVICES_ASSET_PATH = "hs_services";
    private static final String KEY_CLASSPATH = "classpath";
    private static final String KEY_NAME = "name";
    private static final String KEY_SDK = "sdk";
    private static final String KEY_VERSION = "version";

    private static final List<HSComponentAssetParams> HSRegulatorsAssetParams = new ArrayList<>();
    private static final List<HSComponentAssetParams> HSConnectorsAssetParams = new ArrayList<>();
    private static final List<HSComponentAssetParams> HSServicesAssetParams = new ArrayList<>();

    static List<HSComponentAssetParams> getRegulators() {
        return HSRegulatorsAssetParams;
    }

    static List<HSComponentAssetParams> getConnectors() {
        return HSConnectorsAssetParams;
    }

    static List<HSComponentAssetParams> getServices() {
        return HSServicesAssetParams;
    }

    static void findHSComponents(@NonNull Context context) {
        if (HSRegulatorsAssetParams.isEmpty()) {
            findHSComponents(context, HSRegulatorsAssetParams, HS_REGULATORS_ASSET_PATH);
        }
        if (HSConnectorsAssetParams.isEmpty()) {
            findHSComponents(context, HSConnectorsAssetParams, HS_CONNECTORS_ASSET_PATH);
        }
        if (HSServicesAssetParams.isEmpty()) {
            findHSComponents(context, HSServicesAssetParams, HS_SERVICES_ASSET_PATH);
        }
    }

    private static void findHSComponents(@NonNull Context context,
                                         @NonNull List<HSComponentAssetParams> assetParamsMap,
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
                                        @NonNull List<HSComponentAssetParams> assetParamsMap,
                                        @NonNull String filePath) {
        HSComponentAssetParams hsComponentAssetParams = createHSComponentParams(assetManager, filePath);
        if (hsComponentAssetParams != null) {
            assetParamsMap.add(hsComponentAssetParams);
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
            String sdk = networkAssetConfig.optString(KEY_SDK);
            String version = networkAssetConfig.optString(KEY_VERSION);
            String classpath = networkAssetConfig.optString(KEY_CLASSPATH);
            if (TextUtils.isEmpty(name)
                    || TextUtils.isEmpty(sdk)
                    || TextUtils.isEmpty(version)
                    || TextUtils.isEmpty(classpath)) {
                return null;
            }

            return new HSComponentAssetParams(name, sdk, version, classpath);
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