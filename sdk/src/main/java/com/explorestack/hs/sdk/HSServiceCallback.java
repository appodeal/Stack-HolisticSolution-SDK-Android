package com.explorestack.hs.sdk;

import androidx.annotation.NonNull;

public interface HSServiceCallback {

    void onFinished();

    void onFail(@NonNull HSError error);
}
