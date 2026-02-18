package com.sunbox.sdpcompose.service.ambari;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author: wangda
 * @date: 2023/1/5
 */
class QueryProgressResultTest {


    @Test
    void isSuccess() {
        QueryProgressResult result = buildResult();
        assertTrue(result.isSuccess());

        // 有为空的字段
        result = buildResult();
        result.setFailedTaskCount(null);
        result.setProgressPercent(null);
        result.setQueuedTaskCount(null);
        result.setAbortedTaskCount(null);
        assertTrue(result.isSuccess());

    }

    @Test
    void isFail() {
        QueryProgressResult result = buildResult();
        result.setFailedTaskCount(1);

        assertTrue(result.isFail());
        assertFalse(result.isSuccess());

    }

    @Test
    void isPending_totalIsZeroAndCompletedIsZeroAndPendingBiggerZero() {
        QueryProgressResult result = buildResult();
        result.setPendingHostRequestCount(5);
        assertTrue(result.isPending());
    }

    @Test
    void isPending_taskCountIsZeroAndPendingIsZero() {
        QueryProgressResult result = buildResult();

        result.setTaskCount(0);
        assertFalse(result.isPending());
    }

    @Test
    void isPending_taskCountGTZeroAndtaskCoutnNotEQCompleted() {
        QueryProgressResult result = buildResult();

        result.setTaskCount(10);
        result.setCompletedTaskCount(5);
        assertFalse(result.isPending());
    }

    @Test
    void isPending_taskCountGTZeroAndtaskCoutnEQCompleted() {
        QueryProgressResult result = buildResult();

        result.setTaskCount(10);
        result.setCompletedTaskCount(10);
        result.setPendingHostRequestCount(5);
        assertTrue(result.isPending());
    }

    @Test
    void isRunning() {
        QueryProgressResult result = buildResult();
        result.setCompletedTaskCount(78);
        assertTrue(result.isRunning());
    }

    QueryProgressResult buildResult() {
        QueryProgressResult result = new QueryProgressResult();
        result.setTaskCount(80);
        result.setCompletedTaskCount(80);
        result.setProgressPercent(100.00);
        result.setFailedTaskCount(0);
        result.setAbortedTaskCount(0);
        result.setQueuedTaskCount(0);
        result.setTimeoutTaskCount(0);
        return result;
    }

}