package com.sunbox.domain.azure;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
     * sku
     */
    private  String vmSize;

    /**
     * 获取tag值
     * @param tagName Tag名
     * @return
     */
    public String getTagByName(String tagName) {
        return getTagByName(tagName, null);
    }

    /**
     * 获取tag值
     * @param tagName Tag名
     * @param def 没有Tag时的默认值
     * @return
     */
    public String getTagByName(String tagName, String def) {
        if (Objects.isNull(tags)) {
            return def;
        }

        return tags.get(tagName);
    }

    /**
     * 获取tag值
     * @param tagName Tag名
     * @param def 没有Tag时的默认值
     * @return
     */
    public <T> T getTagByName(String tagName, T def, Class<T> cls) {
        if (Objects.isNull(tags)) {
            return def;
        }

        return Convert.convert(cls, tags.get(tagName));
    }

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

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
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

    public String getVmSize() {
        return vmSize;
    }

    public void setVmSize(String vmSize) {
        this.vmSize = vmSize;
    }

    @Override
    public String toString() {
        return "VirtualMachineResponse{" +
                "hostName='" + hostName + '\'' +
                ", privateIp='" + privateIp + '\'' +
                ", name='" + name + '\'' +
                ", zone='" + zone + '\'' +
                ", uniqueId='" + uniqueId + '\'' +
                ", priority='" + priority + '\'' +
                ", vmState='" + vmState + '\'' +
                ", tags=" + tags +
                ", vmSize='" + vmSize + '\'' +
                '}';
    }
}
