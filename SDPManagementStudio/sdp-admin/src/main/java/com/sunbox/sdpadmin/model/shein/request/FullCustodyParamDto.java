/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.sdpadmin.model.shein.request;

/**
 * 全托管参
 * @author wangda
 * @date 2025/1/2
 */
public class FullCustodyParamDto {
    /** 扩容指标（Container/App） */
    private String scaleoutMetric;
    /** 缩容内存百分比阈值（%） */
    private Integer scaleinMemoryThreshold;

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
