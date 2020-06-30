package com.explorestack.hs.sdk;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class HSService {

    @NonNull
    private final String name;
    @Nullable
    private final String version;

    public HSService(@NonNull String name, @Nullable String version) {
        this.name = name;
        this.version = version;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @Nullable
    public String getVersion() {
        return version;
    }

    public abstract void start(@NonNull Context context,
                               @NonNull HSAppParams params,
                               @NonNull HSServiceCallback callback,
                               @NonNull HSConnectorCallback connectorCallback);

    protected HSError buildError(@NonNull String message) {
        return HSError.forService(this, message);
    }
}
