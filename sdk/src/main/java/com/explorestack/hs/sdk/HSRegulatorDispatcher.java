package com.explorestack.hs.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

class HSRegulatorDispatcher {

    @NonNull
    private final List<HSRegulator<?>> regulators = new ArrayList<>();
    @NonNull
    private final HSAppInstance app;

    public HSRegulatorDispatcher(@NonNull HSAppInstance app) {
        this.app = app;
    }

    public void addRegulator(@NonNull HSRegulator<?> component) {
        regulators.add(component);
    }

    public void removeRegulator(@NonNull HSRegulator<?> component) {
        regulators.remove(component);
    }

    @Nullable
    public HSRegulator<?> getBestRegulator() {
        for (HSRegulator<?> regulator : regulators) {
            if (regulator != null) {
                if (regulator.getConsent() != null) {
                    return regulator;
                }
            }
        }
        return null;
    }
}
