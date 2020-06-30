package com.explorestack.hs.sdk;

import androidx.annotation.NonNull;

public class HSError {

    public static HSError NoServices = new HSError(1, "No services provided");
    public static HSError NoConnectors = new HSError(2, "No connectors provided");

    public static HSError forService(@NonNull HSService service, @NonNull String message) {
        return new HSError(1000, String.format("[%s]: %s", service.getName(), message));
    }

    public static HSError forConnector(@NonNull HSConnector service, @NonNull String message) {
        return new HSError(1000, String.format("[%s]: %s", service.getName(), message));
    }

    private int code;
    @NonNull
    private String message;

    private HSError(int code, @NonNull String message) {
        this.code = code;
        this.message = message;
    }

    @NonNull
    @Override
    public String toString() {
        return "HSError{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}
