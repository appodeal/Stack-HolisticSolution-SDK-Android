package com.explorestack.hs.sdk;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface HSContextProvider {

    @NonNull
    Context getApplicationContext();

    @NonNull
    Context getContext();

    @Nullable
    Activity getActivity();

}
