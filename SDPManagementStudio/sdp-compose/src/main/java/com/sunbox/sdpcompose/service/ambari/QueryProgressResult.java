package com.sunbox.sdpcompose.service.ambari;

import cn.hutool.core.collection.CollectionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 查询进展结果
 *
 * @author: wangda
 * @date: 2022/12/10
 */
public class QueryProgressResult {
    /**
     * 请求Id
     */
    Long requestId;

    /**
     * 进展百分比
     */
    Double progressPercent;

    /**
     * 全部任务数量
     */
    Integer taskCount;

    /**
     * 执行失败的任务数量
     */
    Integer failedTaskCount;

    /**
     * 执行中止的任务数量
     */
    Integer abortedTaskCount;

    /**
     * Queued的任务数量
     */
    Integer queuedTaskCount;

    /**
     * 完成的任务数量
     */
    Integer completedTaskCount;

    /**
     * 执行超时的任务数量
     */
    Integer timeoutTaskCount;

    /**
     * Pending的任务数量
     */
    Integer pendingHostRequestCount;

    /**
     *
     */
    List<QueryProgressTask> taskList = new ArrayList<>();

    /**
     * 任务是否处理成功<p/>
     * 处理成功条件：<br/>
     * <ol>
     *     <li>taskCount > 0 &&</li>
     *     <li>completedTaskCount == taskCount &&</li>
     *     <li>failedTaskCount == 0 &&</li>
     *     <li>abortedTaskCount == 0 &&</li>
     *     <li>timeoutTaskCount == 0 &&</li>
     *     <li>pendingHostRequestCount == 0</li>
     * </ol>
     *
     * @return true:处理成功；false:处理失败
     */
    public boolean isSuccess() {
        checkNullNum();
        Integer percent = progressPercent.intValue();
        return Objects.nonNull(taskCount) && taskCount > 0
                && Objects.equals(completedTaskCount, taskCount)
                && Objects.equals(percent, 100)
                && Objects.equals(failedTaskCount, 0)
                && Objects.equals(abortedTaskCount, 0)
                && Objects.equals(failedTaskCount, 0)
                && Objects.equals(timeoutTaskCount, 0)
                && Objects.equals(queuedTaskCount, 0)
                && Objects.equals(pendingHostRequestCount, 0);
    }

    /**
     * 任务执行是否失败<p/>
     * 失败判定条件：<br/>
     * <ol>
     *     <li>taskCount > 0  &&</li>
     *     <li>completedTaskCount == taskCount</li>
     *     <li>&& (</li>
     *     <li>failedTaskCount > 0 ||</li>
     *     <li>abortedTaskCount > 0 ||</li>
     *     <li>timeoutTaskCount > 0 ||</li>
     * </ol>
     *
     * @return
     */
    public boolean isFail() {
        checkNullNum();
        return Objects.nonNull(taskCount) && taskCount > 0
                && Objects.equals(completedTaskCount, taskCount)
                && (
                (Objects.nonNull(failedTaskCount) && failedTaskCount > 0) ||
                        (Objects.nonNull(abortedTaskCount) && abortedTaskCount > 0) ||
                        (Objects.nonNull(failedTaskCount) && failedTaskCount > 0) ||
                        (Objects.nonNull(timeoutTaskCount) && timeoutTaskCount > 0)
        );
    }

    /**
     * 任务执行是否等待<p/>
     * 判定条件：<br/>
     * <ol>
     *     <li>total == complete</li>
     *     <li>pendingHostRequestCount > 0</li>
     * </ol>
     *
     * @return
     */
    public boolean isPending() {
        checkNullNum();
        return Objects.equals(taskCount, completedTaskCount)
                && pendingHostRequestCount > 0;
    }

    /**
     * 任务是否在运行中。<p/>
     * 总任务数大于0： taskCount > 0 <br/>
     * 总任务数 不等于 完成任务数： taskCount != completedTaskCount
     * @return
     */
    public boolean isRunning() {
        checkNullNum();
        return taskCount > 0 && !Objects.equals(taskCount, completedTaskCount);
    }

    private void checkNullNum() {
        progressPercent = Objects.isNull(progressPercent)? 0: progressPercent;
        taskCount = Objects.isNull(taskCount)? 0: taskCount;
        failedTaskCount = Objects.isNull(failedTaskCount)? 0: failedTaskCount;
        abortedTaskCount = Objects.isNull(abortedTaskCount)? 0: abortedTaskCount;
        queuedTaskCount = Objects.isNull(queuedTaskCount)? 0: queuedTaskCount;
        completedTaskCount = Objects.isNull(completedTaskCount)? 0: completedTaskCount;
        timeoutTaskCount = Objects.isNull(timeoutTaskCount)? 0: timeoutTaskCount;
        pendingHostRequestCount = Objects.isNull(pendingHostRequestCount)? 0: pendingHostRequestCount;
    }

    public List<String> getFailHosts() {
        List<String> hosts = new ArrayList<>();
        if (CollectionUtil.isEmpty(taskList)) {
            return hosts;
        }

        for (QueryProgressTask task : taskList) {
            if (!task.isComplete()) {
                hosts.add(task.getHostName());
            }
        }
        return hosts;
    }

    /**
     * 增加一个任务状态
     *
     * @param task
     */
    public void addTask(QueryProgressTask task) {
        this.taskList.add(task);
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Double getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(Double progressPercent) {
        this.progressPercent = progressPercent;
    }

    public Integer getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(Integer taskCount) {
        this.taskCount = taskCount;
    }

    public Integer getFailedTaskCount() {
        return failedTaskCount;
    }

    public void setFailedTaskCount(Integer failedTaskCount) {
        this.failedTaskCount = failedTaskCount;
    }

    public Integer getAbortedTaskCount() {
        return abortedTaskCount;
    }

    public void setAbortedTaskCount(Integer abortedTaskCount) {
        this.abortedTaskCount = abortedTaskCount;
    }

    public Integer getQueuedTaskCount() {
        return queuedTaskCount;
    }

    public void setQueuedTaskCount(Integer queuedTaskCount) {
        this.queuedTaskCount = queuedTaskCount;
    }

    public Integer getCompletedTaskCount() {
        return completedTaskCount;
    }

    public void setCompletedTaskCount(Integer completedTaskCount) {
        this.completedTaskCount = completedTaskCount;
    }

    public Integer getTimeoutTaskCount() {
        return timeoutTaskCount;
    }

    public void setTimeoutTaskCount(Integer timeoutTaskCount) {
        this.timeoutTaskCount = timeoutTaskCount;
    }

    public Integer getPendingHostRequestCount() {
        return pendingHostRequestCount;
    }

    public void setPendingHostRequestCount(Integer pendingHostRequestCount) {
        this.pendingHostRequestCount = pendingHostRequestCount;
    }

    public List<QueryProgressTask> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<QueryProgressTask> taskList) {
        this.taskList = taskList;
    }


    @Override
    public String toString() {
        return "QueryProgressResult{" +
                "requestId=" + requestId +
                ", progressPercent=" + progressPercent +
                ", taskCount=" + taskCount +
                ", failedTaskCount=" + failedTaskCount +
                ", abortedTaskCount=" + abortedTaskCount +
                ", queuedTaskCount=" + queuedTaskCount +
                ", completedTaskCount=" + completedTaskCount +
                ", timeoutTaskCount=" + timeoutTaskCount +
                ", pendingHostRequestCount=" + pendingHostRequestCount +
                ", taskList=" + taskList +
                '}';
    }
}
