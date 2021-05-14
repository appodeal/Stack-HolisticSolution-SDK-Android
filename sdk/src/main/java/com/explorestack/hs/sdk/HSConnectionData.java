package com.explorestack.hs.sdk;

public class HSConnectionData {

    public final String type;
    public final String subType;
    public final boolean isFast;

    HSConnectionData(String type, String subtype, boolean fast) {
        this.type = type;
        this.subType = subtype;
        this.isFast = fast;
    }
}