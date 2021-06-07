package com.explorestack.hs.sdk;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class HSConnector<T> extends HSComponent {

    public HSConnector(@NonNull String name, @Nullable String version) {
        super(name, version);
    }

    public abstract void initialize(@Nullable Activity activity,
                                    @NonNull HSComponentParams params,
                                    @NonNull HSComponentCallback callback,
                                    @Nullable HSRegulator<T> regulator);
}
