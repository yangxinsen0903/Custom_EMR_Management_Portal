/*
 * Copyright (c) 2008, 2023, Sunboxsoft. All rights reserved.
 */
package com.sunbox.sdpadmin.model.admin.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author wangda
 * @date 2024/10/25
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SheinCmdbInfoItem {
    private String uid;

    private String name;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
