package com.sunbox.domain;

import java.util.Date;

public class InfoClusterVmReqJobFailed {
    private Long id;

    private String clusterId;

    private String planId;

    private String jobId;

    private Integer status;

    private Date createdTime;

    private Date modifiedTime;

    //未处理
    public static Integer STATUS_INIT=0;

    //处理完成
    public static Integer STATUS_COMPLETED=1;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId == null ? null : clusterId.trim();
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId == null ? null : planId.trim();
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId == null ? null : jobId.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    @Override
    public String toString() {
        return "InfoClusterVmReqJobFailed{" +
                "id=" + id +
                ", clusterId='" + clusterId + '\'' +
                ", planId='" + planId + '\'' +
                ", jobId='" + jobId + '\'' +
                ", status=" + status +
                ", createdTime=" + createdTime +
                ", modifiedTime=" + modifiedTime +
                '}';
    }
}