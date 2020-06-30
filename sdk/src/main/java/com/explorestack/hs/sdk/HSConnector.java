package com.explorestack.hs.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class HSConnector implements HSConnectorCallback {

    @NonNull
    private final String name;
    @Nullable
    private final String version;

    public HSConnector(@NonNull String name, @Nullable String version) {
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

}
