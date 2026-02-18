package com.sunbox.sdpcompose.model.azure.response;

import cn.hutool.core.util.StrUtil;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: 虚拟机集群
 * @Title: VirtualMachineGroupResponse
 * @Package: com.sunbox.sdpcompose.model.azure.response
 * @Author: wangshihao
 * @Copyright: 版权
 * @CreateTime: 2022/12/7 17:47
 */
public class VirtualMachineGroupResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String groupName;

    private Integer count;

    private List<VirtualMachineResponse> virtualMachines;

    /**
     * 是否匹配一个vmRole
     * @param role
     * @return
     */
    public boolean matchRole(String role) {
        return StrUtil.containsIgnoreCase(groupName, role);
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<VirtualMachineResponse> getVirtualMachines() {
        return virtualMachines;
    }

    public void setVirtualMachines(List<VirtualMachineResponse> virtualMachines) {
        this.virtualMachines = virtualMachines;
    }
}
