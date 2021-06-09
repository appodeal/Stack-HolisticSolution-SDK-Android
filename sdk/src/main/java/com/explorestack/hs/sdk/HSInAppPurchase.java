package com.explorestack.hs.sdk;

import java.util.Map;

public class HSInAppPurchase {

    public enum PurchaseType {SUBSCRIPTION, PURCHASE}

    private PurchaseType type;
    private String publicKey;
    private String signature;
    private String purchaseData;
    private long purchaseTimestamp;
    private String price;
    private String currency;
    private String sku;
    private String orderId;
    private String purchaseToken;
    private Map<String, String> additionalParameters;

    private HSInAppPurchase() {
    }

    public PurchaseType getType() {
        return type;
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

    public long getPurchaseTimestamp() {
        return purchaseTimestamp;
    }

    public String getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public String getSku() {
        return sku;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getPurchaseToken() {
        return purchaseToken;
    }

    public Map<String, String> getAdditionalParameters() {
        return additionalParameters;
    }

    public static Builder newBuilder() {
        return new HSInAppPurchase().new Builder(PurchaseType.PURCHASE);
    }

    public static Builder newSubscriptionBuilder() {
        return new HSInAppPurchase().new Builder(PurchaseType.SUBSCRIPTION);
    }

    @Override
    public String toString() {
        return "price='" + price + '\'' + ", currency='" + currency + '\'';
    }

    public class Builder {

        private Builder(PurchaseType type) {
            HSInAppPurchase.this.type = type;
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

        public Builder withPurchaseTimestamp(long purchaseTimestamp) {
            HSInAppPurchase.this.purchaseTimestamp = purchaseTimestamp;
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

        public Builder withSku(String sku) {
            HSInAppPurchase.this.sku = sku;
            return this;
        }


        public Builder withOrderId(String orderId) {
            HSInAppPurchase.this.orderId = orderId;
            return this;
        }


        public Builder withPurchaseToken(String purchaseToken) {
            HSInAppPurchase.this.purchaseToken = purchaseToken;
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
