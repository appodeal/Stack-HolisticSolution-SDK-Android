package com.explorestack.hs.sdk;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class HSService extends HSComponent {

    public HSService(@NonNull String name, @Nullable String version) {
        super(name, version);
    }

    public abstract void start(@NonNull Context context,
                               @NonNull HSComponentParams params,
                               @NonNull HSComponentCallback callback,
                               @NonNull HSConnectorCallback connectorCallback);
}
