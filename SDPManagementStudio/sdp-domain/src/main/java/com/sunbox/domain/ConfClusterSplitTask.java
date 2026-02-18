package com.sunbox.domain;

import java.util.Date;

/**
 * 集群拆分的子步骤
 * 主要用于集群创建时对创建集群进行拆分
 */
public class ConfClusterSplitTask {
    private String id;
    /**
     * length(32)
     * @see Subject
     */
    private String subject;

    private String clusterId;

    private String vmRole;

    private String groupName;

    // 扩容到的数量
    private Integer scalingOutCount;

    // 期望数量
    private Integer expectCount;

    private String taskId;

    /**
     * @see TaskType
     */
    private String taskType;

    private Date createTime;

    private Date modifiedTime;

    /**
     * @see State
     */
    private Integer state;

    /**
     * length(255)
     */
    private String remark;

    private Integer sortIndex;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getVmRole() {
        return vmRole;
    }

    public void setVmRole(String vmRole) {
        this.vmRole = vmRole;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Integer getScalingOutCount() {
        return scalingOutCount;
    }

    public void setScalingOutCount(Integer scalingOutCount) {
        this.scalingOutCount = scalingOutCount;
    }

    public Integer getExpectCount() {
        return expectCount;
    }

    public void setExpectCount(Integer expectCount) {
        this.expectCount = expectCount;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(Integer sortIndex) {
        this.sortIndex = sortIndex;
    }

    @Override
    public String toString() {
        return "ConfClusterSplitTask{" +
                "id='" + id + '\'' +
                ", subject='" + subject + '\'' +
                ", clusterId='" + clusterId + '\'' +
                ", vmRole='" + vmRole + '\'' +
                ", groupName='" + groupName + '\'' +
                ", scalingOutCount=" + scalingOutCount +
                ", expectCount=" + expectCount +
                ", taskId='" + taskId + '\'' +
                ", taskType='" + taskType + '\'' +
                ", createTime=" + createTime +
                ", modifiedTime=" + modifiedTime +
                ", state=" + state +
                ", remark='" + remark + '\'' +
                ", sortIndex=" + sortIndex +
                '}';
    }

    public enum TaskType {
        SCALE_OUT_TASK("SOT", "扩容任务"),
        ;

        private final String value;
        private final String text;

        TaskType(String value, String text) {
            this.value = value;
            this.text = text;
        }

        public String getValue() {
            return value;
        }

        public String getText() {
            return text;
        }
    }

    public enum Subject {
        SCALE_OUT_CREATE_CLUSTER("SOCC", "扩容-创建集群后"),
        ;

        private final String value;
        private final String text;

        Subject(String value, String text) {
            this.value = value;
            this.text = text;
        }

        public String getValue() {
            return value;
        }

        public String getText() {
            return text;
        }
    }

    public enum State{
        WAITING(1, "等待执行"),
        RUNNING(2, "运行中"),
        SUCCESS(3, "执行成功"),
        FAILURE(4, "执行失败"),
        ;

        private final int value;
        private final String text;

        State(int value, String text) {
            this.value = value;
            this.text = text;
        }

        public int getValue() {
            return value;
        }

        public String getText() {
            return text;
        }
    }
}
