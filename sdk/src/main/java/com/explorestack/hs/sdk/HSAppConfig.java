package com.explorestack.hs.sdk;

import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

public class HSAppConfig {

    @Nullable
    private List<HSService> services;
    @Nullable
    private List<HSConnector> connectors;
    private long serviceInitializeTimeout;
    private boolean isDebugEnabled;

    public HSAppConfig withServices(@Nullable HSService... services) {
        if (services != null) {
            this.services = Arrays.asList(services);
        }
        return this;
    }

    @Nullable
    List<HSService> getServices() {
        return services;
    }

    public HSAppConfig withConnectors(@Nullable HSConnector... connectors) {
        if (connectors != null) {
            this.connectors = Arrays.asList(connectors);
        }
        return this;
    }

    @Nullable
    List<HSConnector> getConnectors() {
        return connectors;
    }

    public HSAppConfig setServiceInitializeTimeout(long serviceInitializeTimeout) {
        this.serviceInitializeTimeout = serviceInitializeTimeout;
        return this;
    }

    long getServiceInitializeTimeout() {
        return serviceInitializeTimeout;
    }

    public HSAppConfig setDebugEnabled(boolean debugEnabled) {
        isDebugEnabled = debugEnabled;
        return this;
    }

    boolean isDebugEnabled() {
        return isDebugEnabled;
    }
}
