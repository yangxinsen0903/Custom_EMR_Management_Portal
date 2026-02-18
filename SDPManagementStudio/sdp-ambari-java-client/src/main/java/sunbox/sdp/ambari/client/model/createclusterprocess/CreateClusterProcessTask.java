package sunbox.sdp.ambari.client.model.createclusterprocess;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

/**
 * 任务
 */
public class CreateClusterProcessTask {

    @SerializedName("cluster_name")
    private String clusterName;

    @SerializedName("id")
    private Long id;

    @SerializedName("request_id")
    private Long requestId;

    @SerializedName("stage_id")
    private Long stageId;

    @SerializedName("attempt_cnt")
    private Integer attemptCnt;

    @SerializedName("command")
    private String command;

    @SerializedName("command_detail")
    private String commandDetail;

    @SerializedName("start_time")
    private Long startTime;

    @SerializedName("end_time")
    private Long endTime;

    @SerializedName("error_log")
    private String errorLog;

    @SerializedName("exit_code")
    private Integer exitCode;

    @SerializedName("host_name")
    private String hostName;

    @SerializedName("ops_display_name")
    private String opsDisplayName;

    @SerializedName("output_log")
    private String outputLog;

    @SerializedName("role")
    private String role;

    @SerializedName("status")
    private String status;

    @SerializedName("stderr")
    private String stderr;

    @SerializedName("stdout")
    private String stdout;

    public Integer getAttemptCnt() {
        return attemptCnt;
    }

    public void setAttemptCnt(Integer attemptCnt) {
        this.attemptCnt = attemptCnt;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getCommandDetail() {
        return commandDetail;
    }

    public void setCommandDetail(String commandDetail) {
        this.commandDetail = commandDetail;
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

    public String getErrorLog() {
        return errorLog;
    }

    public void setErrorLog(String errorLog) {
        this.errorLog = errorLog;
    }

    public Integer getExitCode() {
        return exitCode;
    }

    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getOpsDisplayName() {
        return opsDisplayName;
    }

    public void setOpsDisplayName(String opsDisplayName) {
        this.opsDisplayName = opsDisplayName;
    }

    public String getOutputLog() {
        return outputLog;
    }

    public void setOutputLog(String outputLog) {
        this.outputLog = outputLog;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStderr() {
        return stderr;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    public String getStdout() {
        return stdout;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    @ApiModelProperty(value = "")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
