package sunbox.sdp.ambari.client.model.createclusterprocess;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

/**
 * Stage详情
 */
public class CreateClusterProcessStageDetail {

    @SerializedName("cluster_name")
    private String clusterName;

    @SerializedName("command_params")
    private String commandParams;

    @SerializedName("context")
    private String context;

    @SerializedName("display_status")
    private String displayStatus;

    @SerializedName("end_time")
    private Long endTime;

    @SerializedName("host_params")
    private String hostParams;

    @SerializedName("progress_percent")
    private Integer progressPercent;

    @SerializedName("request_id")
    private Integer requestId;

    @SerializedName("skippable")
    private Boolean skippable;

    @SerializedName("stage_id")
    private Integer stageId;

    @SerializedName("start_time")
    private Long startTime;

    @SerializedName("status")
    private String status;

    @ApiModelProperty(value = "")
    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }
    @ApiModelProperty(value = "")
    public String getCommandParams() {
        return commandParams;
    }

    public void setCommandParams(String commandParams) {
        this.commandParams = commandParams;
    }
    @ApiModelProperty(value = "")
    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
    @ApiModelProperty(value = "")
    public String getDisplayStatus() {
        return displayStatus;
    }

    public void setDisplayStatus(String displayStatus) {
        this.displayStatus = displayStatus;
    }
    @ApiModelProperty(value = "")
    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }
    @ApiModelProperty(value = "")
    public String getHostParams() {
        return hostParams;
    }

    public void setHostParams(String hostParams) {
        this.hostParams = hostParams;
    }
    @ApiModelProperty(value = "")
    public Integer getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(Integer progressPercent) {
        this.progressPercent = progressPercent;
    }
    @ApiModelProperty(value = "")
    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }
    @ApiModelProperty(value = "")
    public Boolean getSkippable() {
        return skippable;
    }

    public void setSkippable(Boolean skippable) {
        this.skippable = skippable;
    }
    @ApiModelProperty(value = "")
    public Integer getStageId() {
        return stageId;
    }

    public void setStageId(Integer stageId) {
        this.stageId = stageId;
    }
    @ApiModelProperty(value = "")
    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }
    @ApiModelProperty(value = "")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
