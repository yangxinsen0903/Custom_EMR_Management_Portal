/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.sdpadmin.model.admin.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 更新业务配置的请求对象
 * @author wangda
 * @date 2024/7/12
 */
public class UpdateBizConfigRequest {
    // ID主键， 不自增，需要手动设置
    @NotNull(message = "id不能为空")
    private Long id;
    // 配置分类
    @NotBlank(message = "配置分类不能为空")
    private String category;
    // 配置名称，中文
    @NotBlank(message = "配置名称不能为空")
    private String name;
    // 配置的Key
    @NotBlank(message = "配置key不能为空")
    private String cfgKey;
    // 配置的值
    @NotNull(message = "配置值不能为空")
    private String cfgValue;
    // 状态：VALID、INVALID、DELETED
    @NotNull(message = "状态不能为空")
    private String state;

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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
