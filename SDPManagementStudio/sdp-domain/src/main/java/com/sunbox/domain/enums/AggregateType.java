package com.sunbox.domain.enums;

public enum AggregateType {
    MAX(1, "max"),
    MIN(2, "min"),
    AVG(3, "avg");

    public Integer typeId;
    public String typeText;

    AggregateType(Integer typeId, String typeText) {
        this.typeId = typeId;
        this.typeText = typeText;
    }
}
