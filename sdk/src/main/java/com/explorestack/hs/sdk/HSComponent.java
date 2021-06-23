package com.explorestack.hs.sdk;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class HSComponent {

    @NonNull
    private final String name;
    @Nullable
    private final String version;

    private boolean isEventsEnabled = true;

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
    public HSConnectorCallback createConnectorCallback(@NonNull Context context) {
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
