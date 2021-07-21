package com.explorestack.hs.sdk;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

final class HSSimpleContextProvider implements HSContextProvider {

    @NonNull
    private final Context context;

    HSSimpleContextProvider(@NonNull Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public Context getApplicationContext() {
        return getContext().getApplicationContext();
    }

    @NonNull
    @Override
    public Context getContext() {
        return context;
    }

    @Nullable
    @Override
    public Activity getActivity() {
        Context context = getContext();
        if (context instanceof Activity) {
            return (Activity) context;
        }
        return HSAppInstance.get().getTopActivity();
    }

}
