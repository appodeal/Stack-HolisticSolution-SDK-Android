package com.explorestack.hs.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public interface HSInAppPurchaseValidateListener {

    void onInAppPurchaseValidateSuccess(@NonNull HSInAppPurchase purchase,
                                        @Nullable List<HSError> errors);

    void onInAppPurchaseValidateFail(@NonNull List<HSError> errors);
}
