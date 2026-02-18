package com.sunbox.domain.cluster;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * work_order_approval_request, 表对应的实体类
 * @author 
 */
public class WorkOrderApprovalRequest  {
    /**
     * ID
     */
    private Long id;

    /**
     * 集群ID
     */
    private String clusterId;

    /**
     * Shein工单系统生成的工单ID
     */
    private String ticketId;

    /**
     * 请求类型。CREATE：创建集群；DESTORY：销毁集群
     */
    private String requestType;

    /**
     * 审核结果。INIT：初始；AGREE：通过，REFUSE：驳回，BACK，REVOKE
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
        this.clusterId = clusterId;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
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

    @Override
    public String toString() {
        return "WorkOrderApprovalRequest{" +
                "id=" + id +
                ", clusterId='" + clusterId + '\'' +
                ", ticketId='" + ticketId + '\'' +
                ", requestType='" + requestType + '\'' +
                ", approvalState='" + approvalState + '\'' +
                ", approvalResult='" + approvalResult + '\'' +
                ", createdby='" + createdby + '\'' +
                ", createdTime=" + createdTime +
                ", modifiedby='" + modifiedby + '\'' +
                ", modifiedTime=" + modifiedTime +
                '}';
    }
}