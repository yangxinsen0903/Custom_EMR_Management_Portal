/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.service;

import cn.hutool.core.util.StrUtil;
import com.sunbox.domain.BizConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 按category分组后的一组配置
 * @author wangda
 * @date 2024/7/12
 */
public class BizConfigGroup {

    private String category;

    private List<BizConfig> configs = new ArrayList<>();

    public BizConfig getConfigByKey(String key) {
        Optional<BizConfig> cfg = configs.stream().filter(v -> StrUtil.equalsIgnoreCase(v.getCfgKey(), key)).findFirst();
        return cfg.orElse(null);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<BizConfig> getConfigs() {
        return configs;
    }

    public void setConfigs(List<BizConfig> configs) {
        this.configs = configs;
    }
}
