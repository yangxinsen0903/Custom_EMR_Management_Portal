/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.domain;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import java.util.Objects;

/**
 * 集群实例组的全托管参数
 * @author wangda
 * @date 2024/12/30
 */
public class FullCustodyParam {
    /** 窗口大小 */
    public static final String SCALE_HOSTING_WINDOW_SIZE = "scale.hosting.window.size";
    /** 统计周期个数 */
    public static final String SCALE_HOSTING_WINDOW_COUNT = "scale.hosting.window.count";
    /** 扩缩容冷却时间（分钟） */
    public static final String SCALE_HOSTING_FREEZE_TIME = "scale.hosting.freeze.time";
    /** 扩容指标（Container/App） */
    public static final String SCALE_HOSTING_SCALEOUT_METRIC = "scale.hosting.scaleout.metric";
    /** 缩容内存百分比阈值（%） */
    public static final String SCALE_HOSTING_SCALEIN_MEMORY_THRESHOLD = "scale.hosting.scalein.memory.threshold";

    /** 统计窗口期大小（分钟） */
    private Integer windowSize;
    /** 统计周期个数 */
    private Integer windowCount;
    /** 扩缩容冷却时间（分钟） */
    private Integer freezeTime;
    /** 扩容指标（Container/App） */
    private String scaleoutMetric;
    /** 缩容内存百分比阈值（%） */
    private Integer scaleinMemoryThreshold;

    /**
     * 转为JSON格式字符串
     * @return
     */
    public String toJsonString() {
        return JSONUtil.toJsonStr(this);
    }

    /**
     * 将JSON字符串参数转为对象
     * @param jsonStr
     * @return
     */
    public static FullCustodyParam parse(String jsonStr) {
        if (StrUtil.isBlank(jsonStr)) {
            return new FullCustodyParam();
        } else {
            return JSONUtil.toBean(jsonStr, FullCustodyParam.class);
        }
    }

    public boolean isSetScaleinMemoryThreshold() {
        return Objects.nonNull(scaleinMemoryThreshold) && scaleinMemoryThreshold > 0;
    }

    public Integer getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(Integer windowSize) {
        this.windowSize = windowSize;
    }

    public Integer getWindowCount() {
        return windowCount;
    }

    public void setWindowCount(Integer windowCount) {
        this.windowCount = windowCount;
    }

    public Integer getFreezeTime() {
        return freezeTime;
    }

    public void setFreezeTime(Integer freezeTime) {
        this.freezeTime = freezeTime;
    }

    public String getScaleoutMetric() {
        return scaleoutMetric;
    }

    public void setScaleoutMetric(String scaleoutMetric) {
        this.scaleoutMetric = scaleoutMetric;
    }

    public Integer getScaleinMemoryThreshold() {
        return scaleinMemoryThreshold;
    }

    public void setScaleinMemoryThreshold(Integer scaleinMemoryThreshold) {
        this.scaleinMemoryThreshold = scaleinMemoryThreshold;
    }
}
