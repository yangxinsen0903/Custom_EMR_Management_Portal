package com.sunbox.domain;

import java.util.Date;

/**
    * 标签字典库
    */
public class ConfTagKeys {
    /**
    * 标签名称
    */
    private String tagKey;

    /**
    * 创建人
    */
    private String createdby;

    /**
    * 创建时间
    */
    private Date createdTime;

    public String getTagKey() {
        return tagKey;
    }

    public void setTagKey(String tagKey) {
        this.tagKey = tagKey;
    }

    public String getCreatedby() {
        return createdby;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }
}