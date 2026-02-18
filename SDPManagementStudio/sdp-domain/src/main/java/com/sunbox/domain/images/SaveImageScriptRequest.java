package com.sunbox.domain.images;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class SaveImageScriptRequest {

    @NotEmpty(message = "脚本名称不能为空")
    private String scriptName;

    @NotEmpty(message = "运行时机不能为空")
    private String runTiming;

    @NotEmpty(message = "playbook地址不能为空")
    private String playbookUri;

    @NotEmpty(message = "脚本地址不能为空")
    private String scriptFileUri;

    private String extraVars;

}
