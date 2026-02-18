package com.sunbox.domain.azure;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.sunbox.domain.azure.createvm.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 申请VM资源返回的响应对象
 */
public class JobQueryResponse implements Serializable {

    Logger logger = LoggerFactory.getLogger(JobQueryResponse.class);

    private static final long serialVersionUID = 1L;

    private String name;

    private String id;

    private String type;

    private String status;

    private List<VirtualMachineGroupResponse> data;

    private ErrorMessage message;

    public static final String STATE_SUCCEEDED = "Succeeded";
    public static final String STATE_FAILED = "Failed";

    public static final String VMROLE_AMBARI = "ambari";
    public static final String VMROLE_MASTER = "master";
    public static final String VMROLE_CORE = "core";
    public static final String VMROLE_TASK = "task";

    /**
     * 获取全部的VM
     * @return
     */
    public List<VirtualMachineResponse> getAllVms() {
        List<VirtualMachineResponse> vms = new ArrayList<>();
        if (Objects.isNull(data)) {
            return vms;
        }
        for (VirtualMachineGroupResponse groupResp : data) {
            for (VirtualMachineResponse vm : groupResp.getVirtualMachines()) {
                vms.add(vm);
            }
        }
        return vms;
    }

    /**
     * 获取全部的成功的VM
     * @return
     */
    public List<VirtualMachineResponse> getAllSuccessVms() {
        List<VirtualMachineResponse> results = new ArrayList<>();
        List<VirtualMachineResponse> vms = getAllVms();
        for (VirtualMachineResponse vm : vms) {
            if (vm.isSucceeded()) {
                results.add(vm);
            }
        }
        return results;
    }

    /**
     * 获取全部失败的VM
     * @return
     */
    public List<VirtualMachineResponse> getAllFailVms() {
        List<VirtualMachineResponse> results = new ArrayList<>();
        List<VirtualMachineResponse> vms = getAllVms();
        for (VirtualMachineResponse vm : vms) {
            if (!vm.isSucceeded()) {
                results.add(vm);
            }
        }
        return results;
    }

    public List<VirtualMachineResponse> getSuccessVmByRole(String role) {
        List<VirtualMachineResponse> results = new ArrayList<>();
        for (VirtualMachineGroupResponse group : this.data) {
            if (group.matchRole(role)) {
                for (VirtualMachineResponse vm : group.getVirtualMachines()) {
                    if (vm.isSucceeded()) {
                        results.add(vm);
                    }
                }
            }
        }
        return results;
    }

    public List<VirtualMachineResponse> getFailVmByRole(String role) {
        List<VirtualMachineResponse> results = new ArrayList<>();
        for (VirtualMachineGroupResponse group : this.data) {
            if (group.matchRole(role)) {
                for (VirtualMachineResponse vm : group.getVirtualMachines()) {
                    if (!vm.isSucceeded()) {
                        results.add(vm);
                    }
                }
            }
        }
        return results;
    }

    public Integer getVMCount(String state, String role) {
        Integer vmCount = 0;
        for (VirtualMachineGroupResponse group : data) {
            if (StrUtil.isNotBlank(role)) {
                if (!StrUtil.contains(group.getGroupName(), role)) {
                    // 如果不是当前指定的role, 直接跳过
                    continue;
                }
            }
            for (VirtualMachineResponse vm : group.getVirtualMachines()) {
                if (StrUtil.isBlank(state) || StrUtil.equalsIgnoreCase(vm.getVmState(), state)) {
                    vmCount ++;
                }
            }
        }
        return vmCount;
    }

    public Integer getTotalVMCountByRole(String role) {
        Integer totalCount = 0;
        for (VirtualMachineGroupResponse group : data) {
            if (StrUtil.isBlank(role) || StrUtil.contains(group.getGroupName(), role)) {
                totalCount += group.getCount();
            }
        }
        return totalCount;
    }

    /**
     * 任务是否顺利完成<br/>
     * 1. 状态为: Completed<br/>
     * 2. VM数量不为0<br/>
     * 3. 所有的VM状态都为:Completed<br/>
     * 4. VM申请数据与申请数据相匹配<br/>
     * @return
     */
    public boolean isSuccessed() {
        //TODO: VM申请数据与申请数据相匹配
        return StrUtil.equalsIgnoreCase(status, "Completed")
                && getAllVms().size() > 0
                && isAllVmCompleted();
    }

    /**
     * 是否所有的VM都成功
     * @return
     */
    private boolean isAllVmCompleted() {
        for (VirtualMachineResponse allVm : getAllVms()) {
            if (!StrUtil.equalsIgnoreCase(allVm.getVmState(), "Succeeded")) {
                return false;
            }
        }
        return true;
    }

    /**
     * 任务是否顺利完成<br/>
     * 1. 状态为: Completed<br/>
     * 2. VM数量不为0<br/>
     * 3. 所有的VM状态都为:Completed<br/>
     * 4. VM申请数据与申请数据相匹配<br/>
     * @param reqGroupVmCount 向Azure请求的每个实例组VM的数量. 数量必须要匹配.  key:groupName, value: vm数量
     * @return
     */
    public boolean isSuccessed(Map<String, Integer> reqGroupVmCount) {
        // 如果没传实例组中VM的数量, 只做基本的判断
        if (CollectionUtil.isEmpty(reqGroupVmCount)) {
            return isSuccessed();
        }

        boolean checkResult = StrUtil.equalsIgnoreCase(status, "Completed")
                && getAllVms().size() > 0
                && isAllVmCompleted();
        // 如果检查没成功, 直接返回没成功
        if (!checkResult) {
            return false;
        }

        // 如果传了实例组中Vm的数量, 则与实际返回的数量做比对.
        for (Map.Entry<String, Integer> reqGroup : reqGroupVmCount.entrySet()) {
            int actualVmCount = getTotalVMCountByGroupName(reqGroup.getKey());
            if (!Objects.equals(actualVmCount,  reqGroup.getValue())) {
                logger.warn("申请资源时,RM返回的申请结果数量不匹配,认为未申请成功. groupName={}, 申请数量={}, 实际返回数量={}",
                        reqGroup.getKey(), reqGroup.getValue(), actualVmCount);
                return false;
            }
        }
        return true;
    }

    public Integer getTotalVMCountByGroupName(String groupName) {
        Integer totalCount = 0;
        for (VirtualMachineGroupResponse group : data) {
            if (StrUtil.equalsIgnoreCase(StrUtil.trim(groupName), StrUtil.trim(group.getGroupName()))) {
                totalCount += group.getCount();
            }
        }
        return totalCount;
    }

    /**
     * 任务是否执行失败
     * @return
     */
    public boolean isFailed() {
        return StrUtil.equalsIgnoreCase(status, "Failed");
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
