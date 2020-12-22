package com.explorestack.hs.sdk.connector.appodeal;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.appodeal.ads.Appodeal;
import com.explorestack.hs.sdk.HSAppParams;
import com.explorestack.hs.sdk.HSComponentCallback;
import com.explorestack.hs.sdk.HSConnector;
import com.explorestack.hs.sdk.HSInAppPurchase;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Currency;
import java.util.Map;

public class HSAppodealConnector extends HSConnector {

    public HSAppodealConnector() {
        super("Appodeal", Appodeal.getVersion());
    }

    @Override
    public void initialize(@NonNull Context context,
                           @NonNull HSAppParams params,
                           @NonNull HSComponentCallback callback) {
        callback.onFinished();
    }

    @Override
    public void setAttributionId(@Nullable String key, @Nullable String value) {
        if (key != null && value != null) {
            Appodeal.setExtraData(key, value);
        }
    }

    @Override
    public void setConversionData(@Nullable Map<String, Object> data) {
        if (data != null) {
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                Appodeal.setSegmentFilter(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
    }

    @Override
    public void setExtra(@Nullable String key, @Nullable String value) {
        if (key != null && value != null) {
            Appodeal.setExtraData(key, value);
        }
    }

    @Override
    public void setExtra(@Nullable Map<String, Object> extra) {
        if (extra != null) {
            for (Map.Entry<String, Object> entry : extra.entrySet()) {
                setExtra(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
    }

    @Override
    public void trackInApp(@Nullable Context context, @Nullable HSInAppPurchase purchase) {
        if (context != null && purchase != null) {
            String purchasePrice;
            if ((purchasePrice = purchase.getPrice()) != null) {
                String currency = purchase.getCurrency();
                Double price = parsePrice(purchasePrice, currency);
                if (price != null) {
                    Appodeal.trackInAppPurchase(context, price, currency);
                }
            }
        }
    }

    @VisibleForTesting
    @Nullable
    Double parsePrice(@NonNull String price, @Nullable String currency) {
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

    private void setUpFormatSymbols(DecimalFormatSymbols formatSymbols,
                                    char decimalSeparator,
                                    char groupingSeparator) {
        formatSymbols.setDecimalSeparator(decimalSeparator);
        formatSymbols.setGroupingSeparator(groupingSeparator);
    }
}
