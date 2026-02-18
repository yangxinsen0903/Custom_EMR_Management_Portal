package com.sunbox.domain;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;

public class InfoClusterInfoCollectLog {
    public static final String RUNING = "执行中";
    public static final String DONE = "完成";
    public static final String FAIL = "失败";
    /**
     * Id  唯一标识
     */
    private String id;
    /**
     * clusterId 集群id
     */
    private String clusterId;
    /**
     * clusterName 集群名称
     */
    private String clusterName;
    /**
     * taskId 任务id
     */
    private String taskId;

    /**
     * state 状态 执行中、完成、失败
     */
    private String state;
    /**
     * createTime 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /**
     * finishTime 完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date finishTime;
    /**
     * 文件上传地址
     */
    private String filePath;

    /**
     * hosts 主机id
     */
    private String hostIps;

    private Integer pageIndex;

    private Integer pageSize;

    private String ansibleTransactionId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }



    public String getHostIps() {
        return hostIps;
    }

    public void setHostIps(String hostIps) {
        this.hostIps = hostIps;
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

    public String getAnsibleTransactionId() {
        return ansibleTransactionId;
    }

    public void setAnsibleTransactionId(String ansibleTransactionId) {
        this.ansibleTransactionId = ansibleTransactionId;
    }
}
