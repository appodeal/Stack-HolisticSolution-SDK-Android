package com.explorestack.hs.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class HSComponent {

    @NonNull
    private final String name;
    @Nullable
    private final String version;

    public HSComponent(@NonNull String name, @Nullable String version) {
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

    protected HSError buildError(@NonNull String message) {
        return HSError.forComponent(this, message);
    }
}
