package com.azure.csu.tiger.rm.api.enums;

import java.util.Objects;

public enum ProvisionType {
    VM(0, "vm"),
    VMSS(1, "vmss"),
    AZURE_FLEET(2, "Azure_Fleet");

    private Integer type;
    private String name;

    ProvisionType(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public static ProvisionType fromType(Integer type) {
        for (ProvisionType value : ProvisionType.values()) {
            if (Objects.equals(type, value.getType())) {
                return value;
            }
        }
        return null;
    }
}
