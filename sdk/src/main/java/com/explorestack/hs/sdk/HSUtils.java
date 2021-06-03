package com.explorestack.hs.sdk;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Currency;
import java.util.Map;

public class HSUtils {

    public static Bundle mapToBundle(@Nullable Map<String, Object> params) {
        Bundle result = new Bundle();
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                putIntoBundle(result, entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    private static void putIntoBundle(@NonNull Bundle bundle,
                                      @Nullable String key,
                                      @Nullable Object value) {
        if (key == null || value == null) {
            return;
        }
        if (value instanceof Integer) {
            bundle.putInt(key, (int) value);
        } else if (value instanceof int[]) {
            bundle.putIntArray(key, (int[]) value);
        } else if (value instanceof Long) {
            bundle.putLong(key, (long) value);
        } else if (value instanceof long[]) {
            bundle.putLongArray(key, (long[]) value);
        } else if (value instanceof Double) {
            bundle.putDouble(key, (double) value);
        } else if (value instanceof double[]) {
            bundle.putDoubleArray(key, (double[]) value);
        } else if (value instanceof String) {
            bundle.putString(key, (String) value);
        } else if (value instanceof String[]) {
            bundle.putStringArray(key, (String[]) value);
        } else if (value instanceof Boolean) {
            bundle.putBoolean(key, (boolean) value);
        } else if (value instanceof boolean[]) {
            bundle.putBooleanArray(key, (boolean[]) value);
        } else {
            bundle.putString(key, value.toString());
        }
    }

    @Nullable
    public static Double parsePrice(@NonNull String price, @Nullable String currency) {
        try {
            if (TextUtils.isEmpty(currency)) {
                return Double.parseDouble(price);
            } else {
                DecimalFormat format = new DecimalFormat();
                Currency formatCurrency = Currency.getInstance(currency);
                format.setCurrency(formatCurrency);
                int idxDot = price.indexOf('.');
                int idxCom = price.indexOf(',');
                boolean containsDot = idxDot > -1;
                boolean containsComma = idxCom > -1;
                DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols();
                if (containsDot && !containsComma) {
                    setUpFormatSymbols(formatSymbols, '.', ',');
                } else if (!containsDot && containsComma) {
                    setUpFormatSymbols(formatSymbols, ',', '.');
                } else if (containsDot && containsComma) {
                    if (idxDot > idxCom) {
                        setUpFormatSymbols(formatSymbols, '.', ',');
                    } else {
                        setUpFormatSymbols(formatSymbols, ',', '.');
                    }
                }
                format.setDecimalFormatSymbols(formatSymbols);
                Number number = format.parse(price.replace(formatCurrency.getSymbol(), ""));
                if (number != null) {
                    return number.doubleValue();
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        try {
            return Double.parseDouble(price);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    private static void setUpFormatSymbols(DecimalFormatSymbols formatSymbols,
                                           char decimalSeparator,
                                           char groupingSeparator) {
        formatSymbols.setDecimalSeparator(decimalSeparator);
        formatSymbols.setGroupingSeparator(groupingSeparator);
    }
}
