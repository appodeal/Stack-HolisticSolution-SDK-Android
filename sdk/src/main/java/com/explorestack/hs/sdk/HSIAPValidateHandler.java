package com.explorestack.hs.sdk;

import androidx.annotation.NonNull;

public interface HSIAPValidateHandler {

    void onValidateInAppPurchase(@NonNull HSInAppPurchase purchase,
                                 @NonNull HSIAPValidateCallback callback);

}
