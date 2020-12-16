package com.explorestack.hs.sdk;

import java.util.Map;

public class HSInAppPurchase {

    private String publicKey;
    private String signature;
    private String purchaseData;
    private String price;
    private String currency;
    private Map<String, String> additionalParameters;

    private HSInAppPurchase() {
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getSignature() {
        return signature;
    }

    public String getPurchaseData() {
        return purchaseData;
    }

    public String getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public Map<String, String> getAdditionalParameters() {
        return additionalParameters;
    }

    public static HSInAppPurchase.Builder newBuilder() {
        return new HSInAppPurchase().new Builder();
    }

    @Override
    public String toString() {
        return "price='" + price + '\'' + ", currency='" + currency + '\'';
    }

    public class Builder {

        private Builder() {
        }

        public Builder withPublicKey(String publicKey) {
            HSInAppPurchase.this.publicKey = publicKey;
            return this;
        }


        public Builder withSignature(String signature) {
            HSInAppPurchase.this.signature = signature;
            return this;
        }


        public Builder withPurchaseData(String purchaseData) {
            HSInAppPurchase.this.purchaseData = purchaseData;
            return this;
        }


        public Builder withPrice(String price) {
            HSInAppPurchase.this.price = price;
            return this;
        }


        public Builder withCurrency(String currency) {
            HSInAppPurchase.this.currency = currency;
            return this;
        }

        public Builder withAdditionalParams(Map<String, String> additionalParameters) {
            HSInAppPurchase.this.additionalParameters = additionalParameters;
            return this;
        }

        public HSInAppPurchase build() {
            return HSInAppPurchase.this;
        }
    }
}
