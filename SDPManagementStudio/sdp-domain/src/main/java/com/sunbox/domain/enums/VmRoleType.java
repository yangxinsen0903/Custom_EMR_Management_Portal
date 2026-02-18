package com.sunbox.domain.enums;

import cn.hutool.core.util.StrUtil;

public enum VmRoleType {
    AMBARI("ambari"),
    MASTER("master"),
    TASK("task"),
    CORE("core"),
    ;

    private String vmRole;

    public String getVmRole() {
        return vmRole;
    }

    VmRoleType(String vmRole) {
        this.vmRole = vmRole;
    }

    public static VmRoleType valueOfName(String vmRole) {
        for (VmRoleType value : values()) {
            if (StrUtil.equalsIgnoreCase(vmRole, value.vmRole)) {
                return value;
            }
        }
        throw new RuntimeException("实例类型不存在,vmRole=" + vmRole);
    }

    public boolean equalValue(String vmRole) {
        return StrUtil.equalsIgnoreCase(this.vmRole, vmRole);
    }
}
