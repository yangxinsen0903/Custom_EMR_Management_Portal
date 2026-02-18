package sunbox.sdp.ambari.client.model.createclusterprocess;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;

/**
 *
 */
public class CreateClusterProcessTaskDetail {
    @SerializedName("attempt_cnt")
    private Integer attemptCnt;

    @SerializedName("cluster_name")
    private String clusterName;

    @SerializedName("command")
    private String command;

    @SerializedName("command_detail")
    private String commandDetail;

    @SerializedName("end_time")
    private Long endTime;

    @SerializedName("error_log")
    private String errorLog;

    @SerializedName("exit_code")
    private Integer exitCode;

    @SerializedName("host_name")
    private String hostName;

    @SerializedName("id")
    private Long id;

    @SerializedName("ops_display_name")
    private String opsDisplayName;

    @SerializedName("output_log")
    private String outputLog;

    @SerializedName("request_id")
    private Integer requestId;

    @SerializedName("role")
    private String role;

    @SerializedName("stage_id")
    private Integer stageId;

    @SerializedName("start_time")
    private Long startTime;

    @SerializedName("status")
    private String status;

    @SerializedName("stderr")
    private String stderr;

    @SerializedName("stdout")
    private String stdout;

    @SerializedName("structured_out")
    private Map<String, Object> structuredOut;

    @ApiModelProperty(value = "")
    public Integer getAttemptCnt() {
        return attemptCnt;
    }

    public void setAttemptCnt(Integer attemptCnt) {
        this.attemptCnt = attemptCnt;
    }

    @ApiModelProperty(value = "")
    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    @ApiModelProperty(value = "")
    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @ApiModelProperty(value = "")
    public String getCommandDetail() {
        return commandDetail;
    }

    public void setCommandDetail(String commandDetail) {
        this.commandDetail = commandDetail;
    }

    @ApiModelProperty(value = "")
    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    @ApiModelProperty(value = "")
    public String getErrorLog() {
        return errorLog;
    }

    public void setErrorLog(String errorLog) {
        this.errorLog = errorLog;
    }

    @ApiModelProperty(value = "")
    public Integer getExitCode() {
        return exitCode;
    }

    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
    }

    @ApiModelProperty(value = "")
    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    @ApiModelProperty(value = "")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ApiModelProperty(value = "")
    public String getOpsDisplayName() {
        return opsDisplayName;
    }

    public void setOpsDisplayName(String opsDisplayName) {
        this.opsDisplayName = opsDisplayName;
    }

    @ApiModelProperty(value = "")
    public String getOutputLog() {
        return outputLog;
    }

    public void setOutputLog(String outputLog) {
        this.outputLog = outputLog;
    }

    @ApiModelProperty(value = "")
    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    @ApiModelProperty(value = "")
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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

    @ApiModelProperty(value = "")
    public String getStderr() {
        return stderr;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    @ApiModelProperty(value = "")
    public String getStdout() {
        return stdout;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    @ApiModelProperty(value = "")
    public Map<String, Object> getStructuredOut() {
        return structuredOut;
    }

    public void setStructuredOut(Map<String, Object> structuredOut) {
        this.structuredOut = structuredOut;
    }
}
