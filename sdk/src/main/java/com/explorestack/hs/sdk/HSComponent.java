package com.explorestack.hs.sdk;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class HSComponent {

    @NonNull
    private final String name;
    @NonNull
    private final String version;
    @NonNull
    private final String adapterVersion;

    private boolean isEventsEnabled = true;

    public HSComponent(@NonNull String name,
                       @NonNull String version,
                       @NonNull String adapterVersion) {
        this.name = name;
        this.version = version;
        this.adapterVersion = adapterVersion;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getServerName() {
        return name.toLowerCase();
    }

    @NonNull
    public String getVersion() {
        return version;
    }

    @NonNull
    public String getAdapterVersion() {
        return adapterVersion;
    }

    public void setEventsEnabled(boolean enabled) {
        isEventsEnabled = enabled;
    }

    public boolean isEventsEnabled() {
        return isEventsEnabled;
    }

    @Nullable
    public HSIAPValidateHandler createIAPValidateHandler(@NonNull Context context) {
        return null;
    }

    @Nullable
    public HSEventsHandler createEventsHandler(@NonNull Context context) {
        return null;
    }

    @Nullable
    public Application.ActivityLifecycleCallbacks getLifecycleCallback(@NonNull Context context) {
        return null;
    }

    protected HSError buildError(@NonNull String message) {
        return HSError.forComponent(this, message);
    }
}
