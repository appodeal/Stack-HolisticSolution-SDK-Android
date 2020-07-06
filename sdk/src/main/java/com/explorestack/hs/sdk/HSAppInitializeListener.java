package com.explorestack.hs.sdk;

import androidx.annotation.Nullable;

import java.util.List;

public interface HSAppInitializeListener {

    void onAppInitialized(@Nullable List<HSError> errors);
}
