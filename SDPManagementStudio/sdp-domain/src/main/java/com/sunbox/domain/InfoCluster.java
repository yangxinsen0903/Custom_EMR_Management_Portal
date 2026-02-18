package com.sunbox.domain;

import java.util.Date;

/**
    * 集群创建信息; 1
    */
public class InfoCluster {
    /**
    * 集群ID
    */
    private String clusterId;

    /**
    * ambari用户名
    */
    private String ambariUsername;

    /**
    * ambari密码;对称加密存储
    */
    private String ambariPassword;

    /**
    * ambari地址
    */
    private String ambariHost;

    /**
     *
     * */
    private String masterIps;

    /**
    * master实例数量
    */
    private Integer masterVmsCount;

    /**
    * core实例数量
    */
    private Integer coreVmsCount;

    /**
    * task实例数量
    */
    private Integer taskVmsCount;

    /**
    * 组件应用数量
    */
    private Integer appsCount;

    /**
    * 集群创建开始时间
    */
    private Date clusterCreateBegtime;

    /**
    * 集群创建完成时间
    */
    private Date clusterCreateEndtime;

    private Integer ambariCount;

    public String getMasterIps() {
        return masterIps;
    }

    public void setMasterIps(String masterIps) {
        this.masterIps = masterIps;
    }

    public Integer getAmbariCount() {
        return ambariCount;
    }

    public void setAmbariCount(Integer ambariCount) {
        this.ambariCount = ambariCount;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getAmbariUsername() {
        return ambariUsername;
    }

    public void setAmbariUsername(String ambariUsername) {
        this.ambariUsername = ambariUsername;
    }

    public String getAmbariPassword() {
        return ambariPassword;
    }

    public void setAmbariPassword(String ambariPassword) {
        this.ambariPassword = ambariPassword;
    }

    public String getAmbariHost() {
        return ambariHost;
    }

    public void setAmbariHost(String ambariHost) {
        this.ambariHost = ambariHost;
    }

    public Integer getMasterVmsCount() {
        return masterVmsCount;
    }

    public void setMasterVmsCount(Integer masterVmsCount) {
        this.masterVmsCount = masterVmsCount;
    }

    public Integer getCoreVmsCount() {
        return coreVmsCount;
    }

    public void setCoreVmsCount(Integer coreVmsCount) {
        this.coreVmsCount = coreVmsCount;
    }

    public Integer getTaskVmsCount() {
        return taskVmsCount;
    }

    public void setTaskVmsCount(Integer taskVmsCount) {
        this.taskVmsCount = taskVmsCount;
    }

    public Integer getAppsCount() {
        return appsCount;
    }

    public void setAppsCount(Integer appsCount) {
        this.appsCount = appsCount;
    }

    public Date getClusterCreateBegtime() {
        return clusterCreateBegtime;
    }

    public void setClusterCreateBegtime(Date clusterCreateBegtime) {
        this.clusterCreateBegtime = clusterCreateBegtime;
    }

    public Date getClusterCreateEndtime() {
        return clusterCreateEndtime;
    }

    public void setClusterCreateEndtime(Date clusterCreateEndtime) {
        this.clusterCreateEndtime = clusterCreateEndtime;
    }

    @Override
    public String toString() {
        return "InfoCluster{" +
                "clusterId='" + clusterId + '\'' +
                ", ambariUsername='" + ambariUsername + '\'' +
                ", ambariPassword='" + ambariPassword + '\'' +
                ", ambariHost='" + ambariHost + '\'' +
                ", masterIps='" + masterIps + '\'' +
                ", masterVmsCount=" + masterVmsCount +
                ", coreVmsCount=" + coreVmsCount +
                ", taskVmsCount=" + taskVmsCount +
                ", appsCount=" + appsCount +
                ", clusterCreateBegtime=" + clusterCreateBegtime +
                ", clusterCreateEndtime=" + clusterCreateEndtime +
                ", ambariCount=" + ambariCount +
                '}';
    }
}