package com.explorestack.hs.sdk;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class HSConnector<T> extends HSComponent {

    public HSConnector(@NonNull String name,
                       @NonNull String version,
                       @NonNull String adapterVersion) {
        super(name, version, adapterVersion);
    }

    public abstract void initialize(@Nullable Activity activity,
                                    @NonNull HSComponentParams params,
                                    @NonNull HSComponentCallback callback,
                                    @Nullable HSRegulator<T> regulator);

    @Nullable
    public HSConnectorCallback createConnectorCallback(@NonNull Context context) {
        return null;
    }
}
