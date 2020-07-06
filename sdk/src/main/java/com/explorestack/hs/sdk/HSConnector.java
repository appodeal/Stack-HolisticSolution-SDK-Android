package com.explorestack.hs.sdk;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class HSConnector extends HSComponent implements HSConnectorCallback {

    public HSConnector(@NonNull String name, @Nullable String version) {
        super(name, version);
    }

    public abstract void initialize(@NonNull Context context,
                                    @NonNull HSAppParams params,
                                    @NonNull HSComponentCallback callback);
}
