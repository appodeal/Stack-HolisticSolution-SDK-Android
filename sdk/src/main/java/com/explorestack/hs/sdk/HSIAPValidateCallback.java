package com.explorestack.hs.sdk;

import androidx.annotation.NonNull;

public interface HSIAPValidateCallback {

    void onSuccess();

    void onFail(@NonNull HSError error);
}
