/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.dao.query;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Vm事件的分页查询参数
 * @author wangda
 * @date 2024/7/14
 */
public class VmEventQueryParam {
    /** region, 相等匹配 */
    private String region;
    /** 集群ID, 相等匹配 */
    private String clusterId;
    /** 集群名, like匹配 */
    private String clusterName;
    /** 集群名, like匹配 */
    private String vmName;
    /** 状态 , 相等匹配*/
    private List<String> states = new ArrayList<>();

    /** 购买类型, 相等匹配 */
    private String purchaseType;
    /** 事件类型, 相等匹配 */
    private String eventType;

    /** 事件触发的开始时间. 大于等于此时间 */
    private Date beginTime;
    /** 事件触发的结束时间, 小于等于此时间 */
    private Date endTime;

    /** 查询开始位置 */
    private Integer offset;
    /** 查询返回的记录数量 */
    private Integer pageSize;

    /** 排序类型: asc 或 desc */
    private String sortType = "desc";
    /**
     * 设置分页信息
     * @param page 页号
     * @param pageSize 页大小
     * @return
     */
    public VmEventQueryParam pager(int page, int pageSize) {
        Assert.isTrue(page > 0, "页号不能小于1");
        Assert.isTrue(pageSize > 0, "页大小不能小于1");
        this.offset = (page-1) * pageSize;
        this.pageSize = pageSize;
        return this;
    }
    public Integer getOffset() {
        return offset;
    }

    public VmEventQueryParam setOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public VmEventQueryParam setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public String getRegion() {
        return region;
    }

    public VmEventQueryParam setRegion(String region) {
        this.region = region;
        return this;
    }

    public String getClusterId() {
        return clusterId;
    }

    public VmEventQueryParam setClusterId(String clusterId) {
        this.clusterId = clusterId;
        return this;
    }

    public String getClusterName() {
        return clusterName;
    }

    public VmEventQueryParam setClusterName(String clusterName) {
        this.clusterName = clusterName;
        return this;
    }

    public String getVmName() {
        return vmName;
    }

    public VmEventQueryParam setVmName(String vmName) {
        this.vmName = vmName;
        return this;
    }

    public VmEventQueryParam setStates(String... states) {
        if (Objects.isNull(states)) {
            return this;
        }
        for (String state : states) {
            if (StrUtil.isNotEmpty(state)){
                this.states.add(state);
            }
        }
        return this;
    }

    public List<String> getStates() {
        return this.states;
    }

    public String getPurchaseType() {
        return purchaseType;
    }

    public VmEventQueryParam setPurchaseType(String purchaseType) {
        this.purchaseType = purchaseType;
        return this;
    }

    public String getEventType() {
        return eventType;
    }

    public VmEventQueryParam setEventType(String eventType) {
        this.eventType = eventType;
        return this;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public VmEventQueryParam setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
        return this;
    }

    public Date getEndTime() {
        return endTime;
    }

    public VmEventQueryParam setEndTime(Date endTime) {
        this.endTime = endTime;
        return this;
    }

    public String getSortType() {
        return sortType;
    }

    public void setSortType(String sortType) {
        this.sortType = sortType;
    }
}
