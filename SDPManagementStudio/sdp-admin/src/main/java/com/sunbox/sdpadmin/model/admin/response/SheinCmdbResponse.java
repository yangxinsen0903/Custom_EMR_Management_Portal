/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.sdpadmin.model.admin.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author wangda
 * @date 2024/10/25
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SheinCmdbResponse {
    /**
     * 响应码，200：已提交创建集群任务
     */
    private String code;
    /**
     * 结果数据
     */
    private SheinCmdbInfo info;

    public List<SheinCmdbInfoItem> getAllResults() {
        if (Objects.isNull(info)) {
            return new ArrayList<>();
        }
        if (Objects.isNull(info.getResults())) {
            return new ArrayList<>();
        }
        return info.getResults();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public SheinCmdbInfo getInfo() {
        return info;
    }

    public void setInfo(SheinCmdbInfo info) {
        this.info = info;
    }

}
