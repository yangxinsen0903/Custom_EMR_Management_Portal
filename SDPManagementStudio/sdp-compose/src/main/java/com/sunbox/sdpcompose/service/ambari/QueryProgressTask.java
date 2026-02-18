package com.sunbox.sdpcompose.service.ambari;

import java.util.Objects;

/**
 * 任务执行状态
 * @author: wangda
 * @date: 2022/12/10
 */
public class QueryProgressTask {

    private String clusterName;

    private Long id;

    private Long requestId;

    private Long stageId;

    private Integer attemptCnt;

    private String command;

    private String commandDetail;

    private Long startTime;

    private Long endTime;

    private String errorLog;

    private Integer exitCode;

    private String hostName;

    private String opsDisplayName;

    private String outputLog;

    private String role;

    private String status;

    private String stderr;

    private String stdout;

    public boolean isComplete() {
        return Objects.equals("COMPLETED", status);
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Long getStageId() {
        return stageId;
    }

    public void setStageId(Long stageId) {
        this.stageId = stageId;
    }

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


}
