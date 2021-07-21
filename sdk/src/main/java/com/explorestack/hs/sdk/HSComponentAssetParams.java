package com.explorestack.hs.sdk;

import androidx.annotation.NonNull;

class HSComponentAssetParams {

    private final String name;
    private final String sdk;
    private final String classpath;

    public HSComponentAssetParams(@NonNull String name,
                                  @NonNull String sdk,
                                  @NonNull String classpath) {
        this.name = name;
        this.sdk = sdk;
        this.classpath = classpath;
    }

    public String getName() {
        return name;
    }

    public String getSdk() {
        return sdk;
    }

    public String getClasspath() {
        return classpath;
    }
}