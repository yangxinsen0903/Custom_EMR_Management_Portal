package com.sunbox.sdpcompose.service.ambari.blueprint;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunbox.sdpcompose.service.ambari.enums.ConfigFileType;

import java.util.*;

/**
 * 申请开通的磁盘信息
 * @author: wangda
 * @date: 2022/12/5
 */
public class DiskInfo {
    /** 磁盘的SkuName */
    String skuName;

    /** 磁盘大小 */
    Integer sizeGB;

    /** 磁盘数量 */
    Integer count = 1;

    public String getSkuName() {
        return skuName;
    }

    public void setSkuName(String skuName) {
        this.skuName = skuName;
    }

    public Integer getSizeGB() {
        return sizeGB;
    }

    public void setSizeGB(Integer sizeGB) {
        this.sizeGB = sizeGB;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
