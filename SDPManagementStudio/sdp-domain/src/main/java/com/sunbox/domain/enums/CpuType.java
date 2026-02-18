package com.sunbox.domain.enums;

/**
 * cpu架构类型
 */
public enum CpuType
{
    AMD64("AMD64", "AMD架构"),
    INTEL("INTEL", "Intel架构"),
    ARM("ARM", "ARM架构"),;

    private final String code;
    private final String info;

    CpuType(String code, String info)
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
