package com.sunbox.domain;


import java.util.Date;

public class ConfScalingTask {

    /** 在等待队列中 */
    public static final Integer IN_TAKS_WAIT_QUEUE = 1;

    /** 不在等待队列中 */
    public static final Integer NOT_IN_TAKS_WAIT_QUEUE = 0;

    public static final Integer FORCE_SCALEIN_YES = 1;

    public static final Integer FORCE_SCALEIN_NO = 0;

    private String taskId;

    private String clusterId;

    private String groupName;

    /** 1扩容，2缩容 */
    private Integer scalingType;

    private String vmRole;

    private String esRuleId;

    private String esRuleName;

    //如果scaling_type=4(磁盘调整)时,此字段代表磁盘iops
    private Integer beforeScalingCount;
    //如果scaling_type=4(磁盘调整)时,此字段代表磁盘吞吐量
    private Integer afterScalingCount;

    private Integer scalingCount;

    // 期望数量街面上提交时的输入数量
    private Integer expectCount;

    /** 是否优雅缩容;1 是 0 否 */
    private Integer isGracefulScalein;
    /** 优雅缩容等待时间（S）;60 ～1800 */
    private Integer scaleinWaitingtime;
    /**操作方式, 用于标识扩缩容的类型, 见本类中Operation_type开头的常量*/
    private Integer operatiionType;

    private Date begTime;

    private Date endTime;

    /** 扩缩容状态,见本类中SCALINGTASK_开头的常量 */
    private Integer state;

    // 是否执行集群启动前脚本（1：执行，0：不执行）
    private Integer enableBeforestartScript;

    // 是否执行集群启动后脚本（1：执行，0：不执行）
    private Integer enableAfterstartScript;

    private Date createTime;

    /**
     * vm默认用户名
     */
    private String defaultUsername;
    private Integer maxCount;
    private Integer minCount;
    private String remark;
    /** 删除实例组,0-不是,1-是 */
    private Integer deleteGroup;
    /** 是否在任务队列中 */
    private Integer inQueue;
    private String scaleoutTaskId;
    private String createdBy;

    /**
     * 是否暴力缩容DataNode，1:暴力缩容 0：不暴力缩容 <p/>
     * 见本类中 FORCE_SCALEIN 开头的常量
     */
    private Integer forceScaleinDataNode;


    // region 操作类型
    /**
     * 手动触发
     */
    public static final Integer Operation_type_UserManual = 1;
    /**
     * 弹性伸缩
     */
    public static final Integer Operation_type_Scaling = 2;
    /**
     * 定时伸缩
     */
    public static final Integer Operation_type_Scheduled = 3;
    /**
     * 第三方API
     */
    public static final Integer Operation_type_ThirdApi = 4;
    /**
     * 创建实例组
     */
    public static final Integer Operation_type_create_group = 5;
    /**
     * 删除实例组
     */
    public static final Integer Operation_type_delete_group = 6;
    /**
     * 竞价
     */
    public static final Integer Operation_type_spot = 7;
    /**
     * 删除扩容任务实例
     */
    public static final Integer Operation_type_delete_Task_Vm = 8;

    /** 补全被驱逐的VM */
    public static final Integer Operation_type_Complete_Evict_Vm = 9;

    /** 清理VM, 为后续的定时任务清理作准备 */
    public static final Integer Operation_type_Clean_Vm = 10;

    /** 补全VM, 为后续的定时任务补全作准备 */
    public static final Integer Operation_type_Complete_Vm = 10;
    // endregion

    // region 伸缩类型
    public static final Integer ScaleType_OUT = 1;
    public static final Integer ScaleType_IN = 2;
    public static final Integer ScaleType_Part_OUT = 3;
    // 磁盘吞吐量和带宽调整
    public static final Integer scaleType_diskThroughput = 4;
    // endregion

    // region 伸缩任务状态
    /**
     * 任务创建
     */
    public static final Integer SCALINGTASK_Create = 0;

    /**
     * 任务执行中
     */
    public static final Integer SCALINGTASK_Running = 1;

    /**
     * 任务完成
     */
    public static final Integer SCALINGTASK_Complete = 2;

    /**
     * 任务失败
     */
    public static final Integer SCALINGTASK_Failed = -9;

    public static final Integer SCALINGTASK_DELETE_GROUP = 1;
    // endregion 伸缩任务状态

    // region 是否优雅缩容常量
    public static final Integer SCALINGTASK_GRACEFULSCALEIN = 1;
    public static final Integer SCALINGTASK_NOT_GRACEFULSCALEIN = 0;
    // endregion 是否优雅缩容常量

    // region 是否执行脚本
    public static final Integer EXECUTE_SCRIPT_ENABLE = 1;
    public static final Integer EXECUTE_SCRIPT_DISENABLE = 0;
    // endregion 是否执行脚本

    public static final String SCALING_FLAG = "scaling_flag_";

    public Integer getForceScaleinDataNode() {
        return forceScaleinDataNode;
    }

    public void setForceScaleinDataNode(Integer forceScaleinDataNode) {
        this.forceScaleinDataNode = forceScaleinDataNode;
    }

    public String getDefaultUsername() {
        return defaultUsername;
    }

    public void setDefaultUsername(String defaultUsername) {
        this.defaultUsername = defaultUsername;
    }

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

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName == null ? null : groupName.trim();
    }

    public Integer getScalingType() {
        return scalingType;
    }

    public void setScalingType(Integer scalingType) {
        this.scalingType = scalingType;
    }

    public String getVmRole() {
        return vmRole;
    }

    public void setVmRole(String vmRole) {
        this.vmRole = vmRole == null ? null : vmRole.trim();
    }

    public String getEsRuleId() {
        return esRuleId;
    }

    public void setEsRuleId(String esRuleId) {
        this.esRuleId = esRuleId == null ? null : esRuleId.trim();
    }

    public String getEsRuleName() {
        return esRuleName;
    }

    public void setEsRuleName(String esRuleName) {
        this.esRuleName = esRuleName == null ? null : esRuleName.trim();
    }

    public Integer getBeforeScalingCount() {
        return beforeScalingCount;
    }

    public void setBeforeScalingCount(Integer beforeScalingCount) {
        this.beforeScalingCount = beforeScalingCount;
    }

    public Integer getAfterScalingCount() {
        return afterScalingCount;
    }

    public void setAfterScalingCount(Integer afterScalingCount) {
        this.afterScalingCount = afterScalingCount;
    }

    public Integer getScalingCount() {
        return scalingCount;
    }

    public void setScalingCount(Integer scalingCount) {
        this.scalingCount = scalingCount;
    }

    public Integer getIsGracefulScalein() {
        return isGracefulScalein;
    }

    public void setIsGracefulScalein(Integer isGracefulScalein) {
        this.isGracefulScalein = isGracefulScalein;
    }

    public Integer getScaleinWaitingtime() {
        return scaleinWaitingtime;
    }

    public void setScaleinWaitingtime(Integer scaleinWaitingtime) {
        this.scaleinWaitingtime = scaleinWaitingtime;
    }

    public Integer getOperatiionType() {
        return operatiionType;
    }

    public void setOperatiionType(Integer operatiionType) {
        this.operatiionType = operatiionType;
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

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
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

    public void setMaxCount(Integer maxCount) {
        this.maxCount = maxCount;
    }

    public Integer getMaxCount() {
        return maxCount;
    }

    public void setMinCount(Integer minCount) {
        this.minCount = minCount;
    }

    public Integer getMinCount() {
        return minCount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void appendRemark(String remark) {
        if (this.remark != null) {
            this.remark += ";";
            this.remark += remark;
            if (this.remark.length() > 3000) {
                this.remark = this.remark.substring(0, 3000);
            }
        } else {
            this.remark = remark;
        }
    }

    public String getRemark() {
        return remark;
    }

    public Integer getDeleteGroup() {
        return deleteGroup;
    }

    public void setDeleteGroup(Integer deleteGroup) {
        this.deleteGroup = deleteGroup;
    }

    public void setInQueue(Integer inQueue) {
        this.inQueue = inQueue;
    }

    public Integer getInQueue() {
        return inQueue;
    }

    public void setScaleoutTaskId(String scaleoutTaskId) {
        this.scaleoutTaskId = scaleoutTaskId;
    }

    public String getScaleoutTaskId() {
        return scaleoutTaskId;
    }

    public Integer getExpectCount() {
        return expectCount;
    }

    public void setExpectCount(Integer expectCount) {
        this.expectCount = expectCount;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public String toString() {
        return "ConfScalingTask{" +
                "taskId='" + taskId + '\'' +
                ", clusterId='" + clusterId + '\'' +
                ", groupName='" + groupName + '\'' +
                ", scalingType=" + scalingType +
                ", vmRole='" + vmRole + '\'' +
                ", esRuleId='" + esRuleId + '\'' +
                ", esRuleName='" + esRuleName + '\'' +
                ", beforeScalingCount=" + beforeScalingCount +
                ", afterScalingCount=" + afterScalingCount +
                ", scalingCount=" + scalingCount +
                ", expectCount=" + expectCount +
                ", isGracefulScalein=" + isGracefulScalein +
                ", scaleinWaitingtime=" + scaleinWaitingtime +
                ", operatiionType=" + operatiionType +
                ", begTime=" + begTime +
                ", endTime=" + endTime +
                ", state=" + state +
                ", enableBeforestartScript=" + enableBeforestartScript +
                ", enableAfterstartScript=" + enableAfterstartScript +
                ", createTime=" + createTime +
                ", createdBy=" + createdBy +
                ", defaultUsername='" + defaultUsername + '\'' +
                ", maxCount=" + maxCount +
                ", minCount=" + minCount +
                ", remark='" + remark + '\'' +
                ", deleteGroup=" + deleteGroup +
                ", inQueue=" + inQueue +
                ", scaleoutTaskId='" + scaleoutTaskId + '\'' +
                '}';
    }
}