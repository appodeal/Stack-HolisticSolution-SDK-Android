package com.explorestack.hs.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HSAppConfig {

    @Nullable
    private List<HSService> services;
    @Nullable
    private List<HSConnector> connectors;
    private long componentInitializeTimeout;
    private boolean isDebugEnabled;

    public HSAppConfig withServices(@Nullable HSService... services) {
        if (services != null) {
            this.services = Arrays.asList(services);
        }
        return this;
    }

    @NonNull
    List<HSService> getServices() {
        return services == null ? Collections.<HSService>emptyList() : services;
    }

    public HSAppConfig withConnectors(@Nullable HSConnector... connectors) {
        if (connectors != null) {
            this.connectors = Arrays.asList(connectors);
        }
        return this;
    }

    @NonNull
    List<HSConnector> getConnectors() {
        return connectors == null ? Collections.<HSConnector>emptyList() : connectors;
    }

    public HSAppConfig setComponentInitializeTimeout(long componentInitializeTimeout) {
        this.componentInitializeTimeout = componentInitializeTimeout;
        return this;
    }

    long getComponentInitializeTimeout() {
        return componentInitializeTimeout;
    }

    public HSAppConfig setDebugEnabled(boolean debugEnabled) {
        isDebugEnabled = debugEnabled;
        return this;
    }

    boolean isDebugEnabled() {
        return isDebugEnabled;
    }
}
