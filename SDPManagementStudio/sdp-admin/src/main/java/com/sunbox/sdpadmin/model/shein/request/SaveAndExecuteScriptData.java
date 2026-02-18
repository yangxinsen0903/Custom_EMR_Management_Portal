package com.sunbox.sdpadmin.model.shein.request;

import lombok.Data;

import java.util.List;

@Data
public class SaveAndExecuteScriptData {

    private String clusterId;

    // 实例组角色
    private List<String> vmRoles;

    // 实例组id
    private List<String> insGpIds;

    private String jobName;

    // 脚本地址
    private String scriptPath;

    // 脚本参数
    private String scriptParam;
}
