package sunbox.sdp.ambari.client.model.createclusterprocess;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

/**
 *
 */
public class CreateClusterProcessStage {

    @SerializedName("cluster_name")
    private String clusterName;

    @SerializedName("request_id")
    private Long requestId;

    @SerializedName("stage_id")
    private Long stageId;

    @SerializedName("command_params")
    private String commandParams;

    @SerializedName("context")
    private String context;

    @SerializedName("display_status")
    private String displayStatus;

    @SerializedName("start_time")
    private Long startTime;

    @SerializedName("end_time")
    private Long endTime;

    @SerializedName("host_params")
    private String hostParams;

    @SerializedName("progress_percent")
    private Double progressPercent;

    @SerializedName("skippable")
    private Boolean skippable;

    @SerializedName("status")
    private String status;


    public String getCommandParams() {
        return commandParams;
    }

    public void setCommandParams(String commandParams) {
        this.commandParams = commandParams;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getDisplayStatus() {
        return displayStatus;
    }

    public void setDisplayStatus(String displayStatus) {
        this.displayStatus = displayStatus;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public String getHostParams() {
        return hostParams;
    }

    public void setHostParams(String hostParams) {
        this.hostParams = hostParams;
    }

    public Double getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(Double progressPercent) {
        this.progressPercent = progressPercent;
    }

    public Boolean getSkippable() {
        return skippable;
    }

    public void setSkippable(Boolean skippable) {
        this.skippable = skippable;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @ApiModelProperty(value = "")
    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    @ApiModelProperty(value = "")
    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    @ApiModelProperty(value = "")
    public Long getStageId() {
        return stageId;
    }

    public void setStageId(Long stageId) {
        this.stageId = stageId;
    }
}
