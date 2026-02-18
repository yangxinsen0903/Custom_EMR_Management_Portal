package com.sunbox.domain;

import java.util.Date;

public class InfoClusterOperationPlan {
    private String planId;

    private String planName;

    private String clusterId;

    private String templateId;

    private String operationType;

    private Date begTime;

    private Date endTime;

    private String createdby;

    private Date createdTime;

    private Integer pageIndex;

    private Integer pageSize;

    private String clusterName;
    /** 扩缩容任务ID */
    private String scalingTaskId;
    /** 由于清理VM任务是由其它任务(扩容任务)过程中,发生VM失败时,派生出来的子任务,所以此ID是父任务的planId */
    private String opTaskId;

    private Integer state;

    private Double percent;

    private Integer startPercent;

    private Integer endPercent;

    /**
     * 执行计划创建
     */
    public static final Integer Plan_State_Create=0;

    /**
     * 计划执行中
     */
    public static final Integer Plan_State_Running=1;

    /**
     * 执行计划完成
     */
    public static final Integer Plan_State_Completed=2;

    /**
     * 执行计划超时
     */
    public static final Integer Plan_state_TimeOut = -1;

    /**
     * 执行计划失败
     */
    public static final Integer Plan_State_Failed=-2;


    /**
     * 创建集群
     */
    public static final String Plan_OP_Create = "create";
    /**
     * 销毁集群
     */
    public static final String Plan_OP_Delete = "delete";
    /**
     * 集群缩容
     */
    public static final String Plan_OP_ScaleIn = "scalein";
    /**
     * 集群扩容
     */
    public static final String Plan_OP_ScaleOut = "scaleout";

    /**
     * 集群扩容-扩容被驱逐后,Azure Fleet自动补充的VM
     */
    public static final String Plan_OP_ScaleOutEvictVm = "scaleoutEvictVm";

    /**
     * 磁盘扩容
     */
    public static final String Plan_OP_Part_ScaleOut = "scaleoutpart";
    /**
     * 执行用户脚本
     */
    public static final String Plan_OP_RunUserScript = "runuserscript";

    /**
     * 重启集群中的某个服务
     */
    public static final String Plan_OP_ClusterService_Restart = "restartservice";

    /**
     * 停止服务
     */
    public static final String Plan_OP_ClusterService_Stop = "stopservice";

    /**
     * 启动服务
     */
    public static final String Plan_OP_ClusterService_Start = "startservice";

    /**
     * 收集日志
     */
    public static final String Plan_OP_CollectLogs = "collectLogs";

    /**
     * 清理VM
     */
    public static final String Plan_OP_ClearVMs = "clearvms";

    /**
     * 删除申请资源失败的VM，（不单独生成plan）
     */
    public static final String Plan_OP_DeleteFailedVMs = "deleteFailedVMs";

    /**
     * pv2磁盘调整
     */
    public static final String Plan_OP_pv2DiskThroughput = "pv2DiskAdjust";


    public Double getPercent() {
        return percent;
    }

    public void setPercent(Double percent) {
        this.percent = percent;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public String getOpTaskId() {
        return opTaskId;
    }

    public void setOpTaskId(String opTaskId) {
        this.opTaskId = opTaskId;
    }

    public String getScalingTaskId() {
        return scalingTaskId;
    }

    public void setScalingTaskId(String scalingTaskId) {
        this.scalingTaskId = scalingTaskId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId == null ? null : planId.trim();
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId == null ? null : clusterId.trim();
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId == null ? null : templateId.trim();
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType == null ? null : operationType.trim();
    }

    public Date getBegTime() {
        return begTime;
    }

    public void setBegTime(Date begTime) {
        this.begTime = begTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getCreatedby() {
        return createdby;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby == null ? null : createdby.trim();
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Integer getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getStartPercent() {
        return startPercent;
    }

    public void setStartPercent(Integer startPercent) {
        this.startPercent = startPercent;
    }

    public Integer getEndPercent() {
        return endPercent;
    }

    public void setEndPercent(Integer endPercent) {
        this.endPercent = endPercent;
    }

    @Override
    public String toString() {
        return "InfoClusterOperationPlan{" +
                "planId='" + planId + '\'' +
                ", clusterId='" + clusterId + '\'' +
                ", templateId='" + templateId + '\'' +
                ", operationType='" + operationType + '\'' +
                ", begTime=" + begTime +
                ", endTime=" + endTime +
                ", createdby='" + createdby + '\'' +
                ", createdTime=" + createdTime +
                ", pageIndex=" + pageIndex +
                ", pageSize=" + pageSize +
                ", clusterName='" + clusterName + '\'' +
                ", scalingTaskId='" + scalingTaskId + '\'' +
                ", opTaskId='" + opTaskId + '\'' +
                ", state=" + state +
                ", percent=" + percent +
                '}';
    }
}