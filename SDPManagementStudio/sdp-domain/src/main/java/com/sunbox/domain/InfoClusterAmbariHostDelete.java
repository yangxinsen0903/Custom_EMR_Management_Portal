package com.sunbox.domain;

import java.util.Date;

public class InfoClusterAmbariHostDelete {
    private Long id;

    private String clusterId;

    private String ambariClusterName;

    private String hostName;

    private String ambariServerIp;

    private String planId;

    private Integer status;

    private Integer retryCount;

    private Date createdTime;

    private Date modifiedTime;

    public static Integer STATUS_Init=0;

    public static Integer STATUS_Complete=3;

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

    public String getAmbariClusterName() {
        return ambariClusterName;
    }

    public void setAmbariClusterName(String ambariClusterName) {
        this.ambariClusterName = ambariClusterName == null ? null : ambariClusterName.trim();
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName == null ? null : hostName.trim();
    }

    public String getAmbariServerIp() {
        return ambariServerIp;
    }

    public void setAmbariServerIp(String ambariServerIp) {
        this.ambariServerIp = ambariServerIp;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId == null ? null : planId.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
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
}