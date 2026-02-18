package com.sunbox.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * cluster_destroy_task
 * @author 
 */

public class ClusterDestroyTask  {
    /**
     * ID
     */
    private Long id;
    /**
     * 集群ID
     */
    private String clusterId;

    /**
     * 集群名称
     */
    private String clusterName;

    /**
     * 是否加入直接销毁白名单,是空或1,否0
     */
    private Integer isWhiteAddr;

    /**
     * 开始销毁时间
     */
    private Date startDestroyTime;

    /**
     * 结束销毁时间
     */
    private Date endDestroyTime;

    /**
     * 销毁任务ID
     */
    private String destroyTaskId;

    /**
     * 销毁状态DestroyStatusConstant:待销毁1，销毁中2，已销毁3，销毁失败0 ,任务已取消4,
     */
    private String destroyStatus;


    /**
     * 是否强制删除,是1,否0
     */
    private Integer fDel;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 创建人
     */
    private String createdby;

    /**
     * 修改时间
     */
    private Date modifiedTime;

    /**
     * 修改人
     */
    private String modifiedby;

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

    public Integer getIsWhiteAddr() {
        return isWhiteAddr;
    }

    public void setIsWhiteAddr(Integer isWhiteAddr) {
        this.isWhiteAddr = isWhiteAddr;
    }

    public Date getStartDestroyTime() {
        return startDestroyTime;
    }

    public void setStartDestroyTime(Date startDestroyTime) {
        this.startDestroyTime = startDestroyTime;
    }

    public Date getEndDestroyTime() {
        return endDestroyTime;
    }

    public void setEndDestroyTime(Date endDestroyTime) {
        this.endDestroyTime = endDestroyTime;
    }

    public String getDestroyTaskId() {
        return destroyTaskId;
    }

    public void setDestroyTaskId(String destroyTaskId) {
        this.destroyTaskId = destroyTaskId;
    }

    public String getDestroyStatus() {
        return destroyStatus;
    }

    public void setDestroyStatus(String destroyStatus) {
        this.destroyStatus = destroyStatus;
    }

    public Integer getfDel() {
        return fDel;
    }

    public void setfDel(Integer fDel) {
        this.fDel = fDel;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getCreatedby() {
        return createdby;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public String getModifiedby() {
        return modifiedby;
    }

    public void setModifiedby(String modifiedby) {
        this.modifiedby = modifiedby;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", clusterId=").append(clusterId);
        sb.append(", clusterName=").append(clusterName);
        sb.append(", isWhiteAddr=").append(isWhiteAddr);
        sb.append(", startDestroyTime=").append(startDestroyTime);
        sb.append(", endDestroyTime=").append(endDestroyTime);
        sb.append(", destroyTaskId=").append(destroyTaskId);
        sb.append(", destroyStatus=").append(destroyStatus);
        sb.append(", createdTime=").append(createdTime);
        sb.append(", createdby=").append(createdby);
        sb.append(", modifiedTime=").append(modifiedTime);
        sb.append(", modifiedby=").append(modifiedby);
        sb.append(", fDel=").append(fDel);
        sb.append(", id=").append(id);
        sb.append("]");
        return sb.toString();
    }
}