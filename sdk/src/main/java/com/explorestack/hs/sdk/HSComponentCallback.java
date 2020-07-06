package com.explorestack.hs.sdk;

import androidx.annotation.NonNull;

public interface HSComponentCallback {

    void onFinished();

    void onFail(@NonNull HSError error);
}
