package com.explorestack.hs.sdk;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class HSRegulator<T> extends HSComponent {

    public HSRegulator(@NonNull String name, @Nullable String version) {
        super(name, version);
    }

    public abstract void start(@NonNull Context context,
                               @NonNull HSAppParams params,
                               @NonNull HSComponentCallback callback);

    @Nullable
    public abstract T getConsent();
}
