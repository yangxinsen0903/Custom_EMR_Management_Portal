/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.sdpadmin.model.shein.request;

/**
 * 全托管弹性扩缩容请求对象
 * @author wangda
 * @date 2025/1/2
 */
public class FullCustodyRequest {
    /** 集群id */
    private String clusterId;
    /** 实例组id */
    private String groupName;
    /** 全托管扩缩容状态, ENABLED: 开启全托管。DISABLED：关闭全托管 */
    private String fullCustodyState;

    /** 是否执行启动前脚本。1: 执行; 0:不执行 */
    private Integer enableBeforestartScript;

    /** 是否执行启动后脚本。1: 执行; 0: 不执行 */
    private Integer enableAfterstartScript;

    /** 是否优雅缩容 1：是，0：不是。默认为0 */
    private Integer isGracefulScalein;

    /** 优雅缩容等待时间单位：分钟 */
    private Integer scaleinWaitingTime;
    /** 全托管参数。此参数可选，不设置时使用全局配置。设置后，使用此自定义的配置值 */
    private FullCustodyParamDto fullCustodyParam;

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getFullCustodyState() {
        return fullCustodyState;
    }

    public void setFullCustodyState(String fullCustodyState) {
        this.fullCustodyState = fullCustodyState;
    }

    public Integer getEnableBeforestartScript() {
        return enableBeforestartScript;
    }

    public void setEnableBeforestartScript(Integer enableBeforestartScript) {
        this.enableBeforestartScript = enableBeforestartScript;
    }

    public Integer getEnableAfterstartScript() {
        return enableAfterstartScript;
    }

    public void setEnableAfterstartScript(Integer enableAfterstartScript) {
        this.enableAfterstartScript = enableAfterstartScript;
    }

    public Integer getIsGracefulScalein() {
        return isGracefulScalein;
    }

    public void setIsGracefulScalein(Integer isGracefulScalein) {
        this.isGracefulScalein = isGracefulScalein;
    }

    public Integer getScaleinWaitingTime() {
        return scaleinWaitingTime;
    }

    public void setScaleinWaitingTime(Integer scaleinWaitingTime) {
        this.scaleinWaitingTime = scaleinWaitingTime;
    }

    public FullCustodyParamDto getFullCustodyParam() {
        return fullCustodyParam;
    }

    public void setFullCustodyParam(FullCustodyParamDto fulCustodyParam) {
        this.fullCustodyParam = fulCustodyParam;
    }
}
