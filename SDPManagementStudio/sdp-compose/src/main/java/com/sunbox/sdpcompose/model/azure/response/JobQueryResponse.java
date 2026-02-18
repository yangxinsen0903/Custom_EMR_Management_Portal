package com.sunbox.sdpcompose.model.azure.response;

import com.sunbox.sdpcompose.model.azure.response.createvm.ErrorMessage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Description: job query resp
 * @Title: JobQueryResponse
 * @Package: com.sunbox.sdpcompose.model.azure.response
 * @Author: wangshihao
 * @Copyright: 版权
 * @CreateTime: 2022/12/7 17:36
 */
public class JobQueryResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    private String id;

    private String type;

    private String status;

    private List<VirtualMachineGroupResponse> data;

    private ErrorMessage message;

    /**
     * 获取创建成功的VM
     * @param role
     * @return
     */
    public List<VirtualMachineResponse> getSuccessVms(String role) {
        List<VirtualMachineResponse> vms = new ArrayList<>();
        if (Objects.isNull(data)) {
            return vms;
        }
        for (VirtualMachineGroupResponse groupResp : data) {
            if (!groupResp.matchRole(role)) {
                continue;
            }
            // 过滤role
            for (VirtualMachineResponse vm : groupResp.getVirtualMachines()) {
                // 检查VM是否成功
                if (vm.isSucceeded()) {
                    vms.add(vm);
                }
            }
        }
        return vms;
    }

    public ErrorMessage getMessage() {
        return message;
    }

    public void setMessage(ErrorMessage message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<VirtualMachineGroupResponse> getData() {
        return data;
    }

    public void setData(List<VirtualMachineGroupResponse> data) {
        this.data = data;
    }
}
