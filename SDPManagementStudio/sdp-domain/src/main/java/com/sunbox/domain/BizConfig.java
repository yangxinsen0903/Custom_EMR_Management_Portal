/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.domain;

import cn.hutool.core.convert.Convert;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;

/**
 * 业务配置, 需要实时生效,管理员手动配置.
 *
 * @author wangda
 * @date 2024/7/12
 */
public class BizConfig {
    /**
     * ID主键， 不自增，需要手动设置
     */
    private Long id;
    /**
     * 配置分类
     */
    private String category;
    /**
     * 配置名称，中文
     */
    private String name;
    /**
     * 配置的Key
     */
    private String cfgKey;
    /**
     * 配置的值
     */
    private String cfgValue;
    /**
     * 备注说明
     */
    private String remark;
    /**
     * 排序序号
     */
    private Integer sortNo;
    /**
     * 状态：VALID、INVALID、DELETED
     */
    private String state;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;

    @JsonIgnore
    public String getValueAsStr() {
        return cfgValue;
    }

    @JsonIgnore
    public Integer getValueAsInt() {
        return Convert.toInt(cfgValue);
    }

    @JsonIgnore
    public Long getValueAsLong() {
        return Convert.toLong(cfgValue);
    }

    @JsonIgnore
    public Boolean getValueAsBoolean() {
        return Convert.toBool(cfgValue);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCfgKey() {
        return cfgKey;
    }

    public void setCfgKey(String cfgKey) {
        this.cfgKey = cfgKey;
    }

    public String getCfgValue() {
        return cfgValue;
    }

    public void setCfgValue(String cfgValue) {
        this.cfgValue = cfgValue;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @JsonIgnore
    public Date getUpdateTime() {
        return updateTime;
    }

    @JsonIgnore
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
