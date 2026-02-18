package com.sunbox.domain;


import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * 接口入参
 */
public class OrderApprovalRequest  {

    /**
     * 集群ID
     */
    private String clusterId;

    /**
     * 集群名称
     */
    private String clusterName;

    /**
     * 请求类型。CREATE：创建集群；DESTORY：销毁集群
     */
    private String requestType;
    /**
     * 审核结果。INIT：初始；AGREE：通过，REFUSE：驳回，
     */
    private String approvalState;

    /**
     * 审核返回结果
     */
    private String approvalResult;


    /**
     * 创建人
     */
    private String createdby;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdTime;

    /**
     * 修改人
     */
    private String modifiedby;

    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date modifiedTime;

    private Integer pageIndex;
    private Integer pageSize;

    public Integer getPageIndex() {
        return this.pageIndex == null ? 1 : this.pageIndex;
    }

    public Integer getPageSize() {
        return this.pageSize == null ? 20 : this.pageSize;
    }

    public Integer getPageStart() {
        return (getPageIndex() - 1) * getPageSize();
    }
    //  -----------
    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    // 计算分页,用于sql
    public void page(){
        this.pageIndex=getPageStart();
        this.pageSize=getPageSize();
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

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getApprovalState() {
        return approvalState;
    }

    public void setApprovalState(String approvalState) {
        this.approvalState = approvalState;
    }

    public String getApprovalResult() {
        return approvalResult;
    }

    public void setApprovalResult(String approvalResult) {
        this.approvalResult = approvalResult;
    }

    public String getCreatedby() {
        return createdby;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getModifiedby() {
        return modifiedby;
    }

    public void setModifiedby(String modifiedby) {
        this.modifiedby = modifiedby;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }
}
