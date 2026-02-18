/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.service;

import com.sunbox.domain.BizConfig;
import com.sunbox.domain.OrderApprovalRequest;
import com.sunbox.domain.ResultMsg;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author wangda
 * @date 2024/7/12
 */
public interface BizConfigService {
    /**
     * 保存一个配置
     *
     * @param bizConfig
     */
    void insert(BizConfig bizConfig);

    /**
     * 获取分组后的配置
     *
     * @return
     */
    List<BizConfigGroup> getGroupedConfigs();

    /**
     * 获取全部的配置
     *
     * @return
     */
    List<BizConfig> getAllConfigs();

    /**
     * 更新一个配置
     *
     * @param bizConfig
     */
    void updateBizConfig(BizConfig bizConfig);

    /**
     * 删除一个配置
     *
     * @param bizConfig
     */
    void delete(Long bizConfig);

    /**
     * 获取一个配置的值
     *
     * @param category
     * @param key
     * @param clz
     * @param <T>
     * @return
     */
    <T> T getConfigValue(String category, String key, Class<T> clz);

    /**
     * 获取集群销毁限流全局配置
     */
    ResultMsg getDestoryClusterLimitConfig();

    /**
     * 保存集群销毁限流全局配置
     */
    ResultMsg updateDestoryClusterLimitConfig(Map<String, String> param);

    /**
     * 根据cfgKey获取配置
     */
    List<BizConfig> getConfigValueByKey(List<String> keyList);

    Map<String, String> getConfigValueMapByKey(List<String> keyList);

     ResultMsg queryOrderApproval( OrderApprovalRequest request) ;

}