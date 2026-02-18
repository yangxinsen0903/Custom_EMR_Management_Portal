package com.sunbox.domain.metaData;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 数据中心
 */
@Data
public class Region implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String region;

    private String regionName;

    private String type;

    private String version;

    private String remark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * key
     */
    private String key;
    /**
     * 显示名称
     */
    private String displayName;
    /**
     * 物理位置
     */
    private String physicalLocation;
    /**
     * 订阅id
     */
    private String subscriptionId;
    /**
     * 订阅name
     */
    private String subscriptionName;

}