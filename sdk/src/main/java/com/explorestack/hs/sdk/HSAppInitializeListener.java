package com.explorestack.hs.sdk;

import androidx.annotation.NonNull;

public interface HSAppInitializeListener {

    void onAppInitialized();

    void onAppInitializationFailed(@NonNull HSError error);
}
