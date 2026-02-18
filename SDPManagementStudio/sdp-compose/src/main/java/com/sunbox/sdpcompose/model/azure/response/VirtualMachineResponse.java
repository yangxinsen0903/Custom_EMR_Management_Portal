package com.sunbox.sdpcompose.model.azure.response;

import cn.hutool.core.util.StrUtil;

import java.io.Serializable;
import java.util.Map;

/**
 * @Description: VirtualMachine
 * @Title: VirtualMachineResponse
 * @Package: com.sunbox.sdpcompose.model.azure.response
 * @Author: wangshihao
 * @Copyright: 版权
 * @CreateTime: 2022/12/7 17:49
 */
public class VirtualMachineResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String hostName;

    private String privateIp;

    private String name;

    private String zone;

    /**
     * vm 唯一ID 对应info_cluster_vm 表中的vmid
     */
    private String uniqueId;

    /** 类型: Spot 或 Regular */
    private String priority;

    /** 状态:Failed 或 Succeeded */
    private String vmState;

    /**
     * Tags
     */
    private Map<String, String> tags;

    /**
     * 是否成功
     * @return
     */
    public boolean isSucceeded() {
        return StrUtil.equals(vmState, "Succeeded");
    }

    public boolean isSpot() {
        return StrUtil.equals(priority, "Spot");
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getVmState() {
        return vmState;
    }

    public void setVmState(String vmState) {
        this.vmState = vmState;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getPrivateIp() {
        return privateIp;
    }

    public void setPrivateIp(String privateIp) {
        this.privateIp = privateIp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
