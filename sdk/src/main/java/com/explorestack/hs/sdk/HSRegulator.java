package com.explorestack.hs.sdk;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class HSRegulator<T> extends HSComponent {

    public HSRegulator(@NonNull String name,
                       @NonNull String version,
                       @NonNull String adapterVersion) {
        super(name, version, adapterVersion);
    }

    public abstract void start(@NonNull Context context,
                               @NonNull HSComponentParams params,
                               @NonNull HSComponentCallback callback);

    @Nullable
    public abstract T getConsent();
}
