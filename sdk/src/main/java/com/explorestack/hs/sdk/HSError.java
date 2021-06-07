package com.explorestack.hs.sdk;

import androidx.annotation.NonNull;

public class HSError {

    public static HSError Internal = new HSError(0, "Internal error");

    public static HSError NoServices = new HSError(1, "No services provided");
    public static HSError NoConnectors = new HSError(2, "No connectors provided");
    public static HSError NoRegulator = new HSError(3, "No regulator provided");
    public static HSError NoIAPValidateHandlers = new HSError(4, "No IAP validators found");
    public static HSError NoIAPValidateTimeout = new HSError(5, "IAP validation timeout");

    public static HSError forRequest(@NonNull String message) {
        return new HSError(100, "Request error (" + message + ")");
    }

    public static HSError forComponent(@NonNull HSComponent component, @NonNull String message) {
        return new HSError(1000, String.format("[%s]: %s", component.getName(), message));
    }

    private final int code;
    @NonNull
    private final String message;

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
