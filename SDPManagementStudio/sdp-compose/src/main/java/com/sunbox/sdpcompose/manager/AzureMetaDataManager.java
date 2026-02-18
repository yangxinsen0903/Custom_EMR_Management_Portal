/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.sdpcompose.manager;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sunbox.dao.mapper.MetaDataItemMapper;
import com.sunbox.domain.MetaDataItem;
import com.sunbox.domain.enums.MetaDataType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Azure元数据的管理类
 * @author wangda
 * @date 2024/6/8
 */
@Component
public class AzureMetaDataManager {
    @Autowired
    MetaDataItemMapper metaDataItemMapper;

    /**
     * 根据资源类型和名称查询元数据
     * @param region 数据中心
     * @param resourceName 资源名称, SshKey在Keyvault中的名称
     * @return
     */
    public Map<String, String> findSshKeyByName(String region, String resourceName) {
        MetaDataItem item = new MetaDataItem();
        item.setType(MetaDataType.SSH_KEY.getCode());
        item.setRegion(region);
        List<String> items = metaDataItemMapper.selectMetaData(item);
        Optional<JSONObject> obj = items.stream().map(s -> {
            return JSONUtil.parseObj(s);
        }).filter(json -> {
            return json.getStr("nameInKeyVault").equals(resourceName);
        }).findFirst();

        if (obj.isPresent()) {
            return obj.get().toBean(Map.class);
        } else {
            throw new RuntimeException("未在元数据中找到SSH Key对应的信息: sshKey=" + resourceName + ", region=" + region);
        }
    }
}
