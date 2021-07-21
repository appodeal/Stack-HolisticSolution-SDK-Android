package com.explorestack.hs.sdk;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import static com.explorestack.hs.sdk.HSComponentAssetManager.findHSConnectorsAssetParams;
import static com.explorestack.hs.sdk.HSComponentAssetManager.findHSRegulatorsAssetParams;
import static com.explorestack.hs.sdk.HSComponentAssetManager.findHSServicesAssetParams;

class HSComponentRegistry {

    private static final String TAG = "HSComponentRegistry";

    private static List<HSService> hsServices = new ArrayList<>();
    private static List<HSRegulator> hsRegulators = new ArrayList<>();
    private static List<HSConnector> hsConnectors = new ArrayList<>();

    @NonNull
    static List<HSService> getServices() {
        return hsServices;
    }

    @NonNull
    static List<HSRegulator> getRegulators() {
        return hsRegulators;
    }

    @NonNull
    static List<HSConnector> getConnectors() {
        return hsConnectors;
    }

    @NonNull
    static List<HSService> registerServices(@NonNull HSAppInstance app, @NonNull Context context) {
        if (hsServices.isEmpty()) {
            hsServices = create(findHSServicesAssetParams(context));
        }
        return hsServices;
    }

    @NonNull
    static List<HSRegulator> registerRegulators(@NonNull HSAppInstance app, @NonNull Context context) {
        if (hsRegulators.isEmpty()) {
            hsRegulators = create(findHSRegulatorsAssetParams(context));
            for (HSRegulator regulator : hsRegulators) {
                app.getRegulatorDelegate().addRegulator(regulator);
            }
        }
        return hsRegulators;
    }

    @NonNull
    static List<HSConnector> registerConnectors(@NonNull HSAppInstance app, @NonNull Context context) {
        if (hsConnectors.isEmpty()) {
            hsConnectors = create(findHSConnectorsAssetParams(context));
            for (HSConnector connector : hsConnectors) {
                app.getConnectorDelegate().addCallback(connector,
                                                       connector.createConnectorCallback(context));
            }
        }
        return hsConnectors;
    }

    @NonNull
    private static <T extends HSComponent> List<T> create(
            @NonNull List<HSComponentAssetParams> componentsAssetParams
    ) {
        List<T> hsComponents = new ArrayList<>();
        for (HSComponentAssetParams assetParams : componentsAssetParams) {
            try {
                T component = (T) Class.forName(assetParams.getClasspath())
                                       .getConstructor()
                                       .newInstance();
                hsComponents.add(component);
            } catch (Throwable ignored) {
                HSLogger.logError(TAG, String.format("HSComponent (%s) create fail!", assetParams.getName()));
            }
        }
        return hsComponents;
    }
}
