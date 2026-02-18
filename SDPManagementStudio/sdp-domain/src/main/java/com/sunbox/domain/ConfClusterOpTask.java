package com.sunbox.domain;

import java.util.Date;

public class ConfClusterOpTask {
    private String taskId;

    private String clusterId;

    private String serviceName;

    private String groupId;

    private String opreationType;

    private Integer state;

    private String paramInfo;

    private Date beginTime;

    private Date endTime;

    private Date createTime;

    //region 操作类型枚举
    public static final String OP_TYPE_STOP="stop";

    public static final String OP_TYPE_START="start";

    public static final String OP_TYPE_RESTART="restart";

    //endregion

    //region 任务状态枚举

    // 已创建
    public static final Integer OP_TASK_STATE_Create=0;
    // 执行中
    public static final Integer OP_TASK_STATE_Running=1;
    // 执行完成
    public static final Integer OP_TASK_STATE_Complate=2;
    // 执行失败
    public static final Integer OP_TASK_STATE_Failed=-9;

    //endregion

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId == null ? null : taskId.trim();
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId == null ? null : clusterId.trim();
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName == null ? null : serviceName.trim();
    }

    public String getOpreationType() {
        return opreationType;
    }

    public void setOpreationType(String opreationType) {
        this.opreationType = opreationType == null ? null : opreationType.trim();
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getParamInfo() {
        return paramInfo;
    }

    public void setParamInfo(String paramInfo) {
        this.paramInfo = paramInfo == null ? null : paramInfo.trim();
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "ConfClusterOpTask{" +
                "taskId='" + taskId + '\'' +
                ", clusterId='" + clusterId + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", opreationType='" + opreationType + '\'' +
                ", state=" + state +
                ", paramInfo='" + paramInfo + '\'' +
                ", beginTime=" + beginTime +
                ", endTime=" + endTime +
                ", createTime=" + createTime +
                '}';
    }
}