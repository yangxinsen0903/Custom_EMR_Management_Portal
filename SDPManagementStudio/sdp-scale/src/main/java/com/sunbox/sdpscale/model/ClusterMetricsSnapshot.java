package com.sunbox.sdpscale.model;

import cn.hutool.core.math.MathUtil;
import cn.hutool.core.util.NumberUtil;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.sunbox.util.JsonMapper;
import com.sunbox.web.BaseCommonInterFace;

import java.math.RoundingMode;
import java.util.HashMap;

import static com.sunbox.sdpscale.task.FullCustodyTask.*;

public class ClusterMetricsSnapshot implements BaseCommonInterFace {
    public Snapshot appsPending;
    public Snapshot appsRunning;
    public Snapshot pendingContainers;
    public Snapshot allocatedContainers;
    public Snapshot pendingMb;
    public Snapshot allcatedMb;
    public Snapshot availableMb;

    public ClusterMetricsSnapshot(MetricRegistry registry) {
        this.appsPending = registry.histogram(METRIC_APPS_PENDING).getSnapshot();
        this.appsRunning = registry.histogram(METRIC_APPS_RUNNING).getSnapshot();
        this.pendingContainers = registry.histogram(METRIC_PENDING_CONTAINERS).getSnapshot();
        this.allocatedContainers = registry.histogram(METRIC_ALLOCATED_CONTAINERS).getSnapshot();
        this.pendingMb = registry.histogram(METRIC_PENDING_MB).getSnapshot();
        this.allcatedMb = registry.histogram(METRIC_ALLOCATED_MB).getSnapshot();
        this.availableMb = registry.histogram(METRIC_AVAILABLE_MB).getSnapshot();

    }

    /**
     * 计算可用内存比率
     * @return 保留4位小数
     */
    public double getAvaliableMemoryRatio() {
        double ratio = getAvailableMb().getMean() / (getAllcatedMb().getMean() + getAvailableMb().getMean());
        return NumberUtil.round(ratio, 4, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 计算App的Pending比率
     * @return 保留4位小数
     */
    public double getAppPendingRatio() {
        double ratio = getAppsPending().getMean() / getAppsRunning().getMean();
        return NumberUtil.round(ratio, 4, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 计算Container的Pending比率
     * @return 保留4位小数
     */
    public double getContainerPendingRatio() {
        double ratio =  getPendingContainers().getMean() / getAllocatedContainers().getMean();
        return NumberUtil.round(ratio, 4, RoundingMode.HALF_UP).doubleValue();
    }

    public String toMeanJsonValues(){
        HashMap<String, String> values = new HashMap<>();
        values.put("appsPendingMean", String.format("%.2f", appsPending.getMean()));
        values.put("appsRunningMean", String.format("%.2f", appsRunning.getMean()));
        values.put("pendingContainersMean", String.format("%.2f", pendingContainers.getMean()));
        values.put("allocatedContainersMean", String.format("%.2f", allocatedContainers.getMean()));
        values.put("pendingMbMean", String.format("%.2f", pendingMb.getMean()));
        values.put("allcatedMbMean", String.format("%.2f", allcatedMb.getMean()));
        values.put("availableMbMean", String.format("%.2f", availableMb.getMean()));
        return JsonMapper.nonDefaultMapper().toJson(values);
    }

    public Snapshot getAppsPending() {
        return appsPending;
    }

    public Snapshot getAppsRunning() {
        return appsRunning;
    }

    public Snapshot getPendingContainers() {
        return pendingContainers;
    }

    public Snapshot getAllocatedContainers() {
        return allocatedContainers;
    }

    public Snapshot getPendingMb() {
        return pendingMb;
    }

    public Snapshot getAllcatedMb() {
        return allcatedMb;
    }

    public Snapshot getAvailableMb() {
        return availableMb;
    }
}
