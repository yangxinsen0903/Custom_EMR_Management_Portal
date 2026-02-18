package com.sunbox.domain.enums;

import lombok.Getter;

public enum GroupVmType {
    VM("VM","单VM"),
    VM_POOL("VM_POOL","多机型资源池"),
    VM_FLEET("VM_FLEET","实例队列");

    private final String code;
    private final String info;

    GroupVmType(String code, String info)
    {
        this.code = code;
        this.info = info;
    }

    public String getCode() {
        return code;
    }

    public String getInfo() {
        return info;
    }
}
