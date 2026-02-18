package com.sunbox.sdpcompose.service.ambari.blueprint;

import java.util.ArrayList;
import java.util.List;

/**
 * 主机实例信息
 * @author: wangda
 * @date: 2022/12/5
 */
public class HostInstance {

    /** 主机角色:ambari, master, core, task */
    String hostRole;

    /** 主机名 */
    String hostName;

    /** CPU核数 */
    Integer vCpu;

    /** 内存大小 */
    Integer memoryGB;

    /** 系统盘大小 */
    Integer osDiskSize;

    /** 数据盘 */
    List<DiskInfo> disks = new ArrayList<>();

    /**
     * 填加一个数据盘
     * @param diskInfo
     */
    public void addDiskInfo(DiskInfo diskInfo) {
        this.disks.add(diskInfo);
    }

    public String getHostRole() {
        return hostRole;
    }

    public void setHostRole(String hostRole) {
        this.hostRole = hostRole;
    }

    public Integer getvCpu() {
        return vCpu;
    }

    public void setvCpu(Integer vCpu) {
        this.vCpu = vCpu;
    }

    public Integer getMemoryGB() {
        return memoryGB;
    }

    public void setMemoryGB(Integer memoryGB) {
        this.memoryGB = memoryGB;
    }

    public Integer getOsDiskSize() {
        return osDiskSize;
    }

    public void setOsDiskSize(Integer osDiskSize) {
        this.osDiskSize = osDiskSize;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public List<DiskInfo> getDisks() {
        return disks;
    }

    public void setDisks(List<DiskInfo> disks) {
        this.disks = disks;
    }

    @Override
    public String toString() {
        return "HostInstance{" +
                "hostRole='" + hostRole + '\'' +
                ", hostName='" + hostName + '\'' +
                ", vCpu=" + vCpu +
                ", memoryGB=" + memoryGB +
                ", osDiskSize=" + osDiskSize +
                ", disks=" + disks +
                '}';
    }
}
