package com.sunbox.sdpcompose.service.ambari.configgeneerator;

import com.sunbox.sdpcompose.service.ambari.enums.HostGroupRole;

public class CustomConfigGeneratorFactory {
    private CustomConfigGeneratorFactory() {

    }

    public static CustomConfigGenerator tryCreate(HostGroupRole groupRole) {
        if (groupRole.equals(HostGroupRole.CORE)) {
            return new CoreNodeManagerResourceConfigGenerator();
        } else if (groupRole.equals(HostGroupRole.TASK)) {
            return new TaskNodeManagerResourceConfigGenerator();
        } else {
            return null;
        }
    }
}
