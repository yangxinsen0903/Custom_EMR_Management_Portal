package com.sunbox.sdpcompose.service.ambari;

/**
 * 创建集群结果
 * @author: wangda
 * @date: 2022/12/10
 */
public class InProgressResult {
    /** 集群创建是否提交成功,如果提交成功,可以查看集群创建的进度 */
    private boolean isSuccess = true;

    /** 查询集群创建进度的请求ID */
    private Long requestId;

    /** 集群名称 */
    private String clusterName;

    /** 没成功时，返回的错误消息 */
    private String message;

    public boolean isSuccess() {
        return isSuccess;
    }

    public InProgressResult setSuccess(boolean success) {
        this.isSuccess = success;
        return this;
    }

    public Long getRequestId() {
        return requestId;
    }

    public InProgressResult setRequestId(Long requestId) {
        this.requestId = requestId;
        return this;
    }

    public String getClusterName() {
        return clusterName;
    }

    public InProgressResult setClusterName(String clusterName) {
        this.clusterName = clusterName;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public InProgressResult setMessage(String message) {
        this.message = message;
        return this;
    }
}
