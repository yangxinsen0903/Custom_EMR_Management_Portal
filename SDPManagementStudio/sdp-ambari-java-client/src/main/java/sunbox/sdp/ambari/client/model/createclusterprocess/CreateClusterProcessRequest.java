package sunbox.sdp.ambari.client.model.createclusterprocess;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;

public class CreateClusterProcessRequest {

    @SerializedName("aborted_task_count")
    private Integer abortedTaskCount;

    @SerializedName("completed_task_count")
    private Integer completedTaskCount;

    @SerializedName("cluster_host_info")
    private String clusterHostInfo;

    @SerializedName("cluster_name")
    private String clusterName;

    @SerializedName("create_time")
    private Long createTime;

    @SerializedName("end_time")
    private Long endTime;

    @SerializedName("exclusive")
    private Boolean exclusive;

    @SerializedName("failed_task_count")
    private Integer failedTaskCount;

    @SerializedName("id")
    private Long id;

    @SerializedName("inputs")
    private String inputs;

    @SerializedName("operation_level")
    private String operationLevel;

    @SerializedName("pending_host_request_count")
    private Integer pendingHostRequestCount;

    @SerializedName("progress_percent")
    private Double progressPercent;

    @SerializedName("queued_task_count")
    private Integer queuedTaskCount;

    @SerializedName("request_context")
    private String requestContext = "";

    @SerializedName("request_schedule")
    private String requestSchedule = "";

    @SerializedName("request_status")
    private String requestStatus = "";

//    @SerializedName("resource_filters")
//    private String resourceFilters = "";

    @SerializedName("start_time")
    private Long startTime;

    @SerializedName("task_count")
    private Integer taskCount;

    @SerializedName("timed_out_task_count")
    private Integer timedOutTaskCount;

    @SerializedName("type")
    private String type = "";

    @SerializedName("user_name")
    private String userName = "";

    @ApiModelProperty(value = "")
    public Integer getCompletedTaskCount() {
        return completedTaskCount;
    }

    public void setCompletedTaskCount(Integer completedTaskCount) {
        this.completedTaskCount = completedTaskCount;
    }

    @ApiModelProperty(value = "")
    public Integer getAbortedTaskCount() {
        return abortedTaskCount;
    }

    public void setAbortedTaskCount(Integer abortedTaskCount) {
        this.abortedTaskCount = abortedTaskCount;
    }
    @ApiModelProperty(value = "")
    public String getClusterHostInfo() {
        return clusterHostInfo;
    }

    public void setClusterHostInfo(String clusterHostInfo) {
        this.clusterHostInfo = clusterHostInfo;
    }
    @ApiModelProperty(value = "")
    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }
    @ApiModelProperty(value = "")
    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }
    @ApiModelProperty(value = "")
    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }
    @ApiModelProperty(value = "")
    public Boolean getExclusive() {
        return exclusive;
    }

    public void setExclusive(Boolean exclusive) {
        this.exclusive = exclusive;
    }
    @ApiModelProperty(value = "")
    public Integer getFailedTaskCount() {
        return failedTaskCount;
    }

    public void setFailedTaskCount(Integer failedTaskCount) {
        this.failedTaskCount = failedTaskCount;
    }
    @ApiModelProperty(value = "")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    @ApiModelProperty(value = "")
    public String getInputs() {
        return inputs;
    }

    public void setInputs(String inputs) {
        this.inputs = inputs;
    }
    @ApiModelProperty(value = "")
    public String getOperationLevel() {
        return operationLevel;
    }

    public void setOperationLevel(String operationLevel) {
        this.operationLevel = operationLevel;
    }
    @ApiModelProperty(value = "")
    public Integer getPendingHostRequestCount() {
        return pendingHostRequestCount;
    }

    public void setPendingHostRequestCount(Integer pendingHostRequestCount) {
        this.pendingHostRequestCount = pendingHostRequestCount;
    }
    @ApiModelProperty(value = "")
    public Double getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(Double progressPercent) {
        this.progressPercent = progressPercent;
    }
    @ApiModelProperty(value = "")
    public Integer getQueuedTaskCount() {
        return queuedTaskCount;
    }

    public void setQueuedTaskCount(Integer queuedTaskCount) {
        this.queuedTaskCount = queuedTaskCount;
    }
    @ApiModelProperty(value = "")
    public String getRequestContext() {
        return requestContext;
    }

    public void setRequestContext(String requestContext) {
        this.requestContext = requestContext;
    }
    @ApiModelProperty(value = "")
    public String getRequestSchedule() {
        return requestSchedule;
    }

    public void setRequestSchedule(String requestSchedule) {
        this.requestSchedule = requestSchedule;
    }
    @ApiModelProperty(value = "")
    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }
//    @ApiModelProperty(value = "")
//    public String getResourceFilters() {
//        return resourceFilters;
//    }

//    public void setResourceFilters(String resourceFilters) {
//        this.resourceFilters = resourceFilters;
//    }
    @ApiModelProperty(value = "")
    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }
    @ApiModelProperty(value = "")
    public Integer getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(Integer taskCount) {
        this.taskCount = taskCount;
    }
    @ApiModelProperty(value = "")
    public Integer getTimedOutTaskCount() {
        return timedOutTaskCount;
    }

    public void setTimedOutTaskCount(Integer timedOutTaskCount) {
        this.timedOutTaskCount = timedOutTaskCount;
    }
    @ApiModelProperty(value = "")
    public String getType() {
        return type;
    }
    @ApiModelProperty(value = "")
    public void setType(String type) {
        this.type = type;
    }

    public String getUserName() {
        return userName;
    }
    @ApiModelProperty(value = "")
    public void setUserName(String userName) {
        this.userName = userName;
    }
}
