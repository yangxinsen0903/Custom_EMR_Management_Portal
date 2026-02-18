package com.sunbox.domain.enums;

import java.util.Objects;

public enum PurchaseType {
    Standard(1),
    Spot(2),
    ;
    private Integer purchaseType;

    public Integer getPurchaseType() {
        return purchaseType;
    }

    PurchaseType(Integer purchaseType) {
        this.purchaseType = purchaseType;
    }

    public Boolean equalValue(Integer purchaseType) {
        return Objects.equals(this.purchaseType, purchaseType);
    }
}
