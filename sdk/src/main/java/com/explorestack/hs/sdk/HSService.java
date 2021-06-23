package com.explorestack.hs.sdk;

import android.content.Context;

import androidx.annotation.NonNull;

public abstract class HSService extends HSComponent {

    public HSService(@NonNull String name,
                     @NonNull String version,
                     @NonNull String adapterVersion) {
        super(name, version, adapterVersion);
    }

    public abstract void start(@NonNull Context context,
                               @NonNull HSComponentParams params,
                               @NonNull HSComponentCallback callback,
                               @NonNull HSConnectorCallback connectorCallback);
}
