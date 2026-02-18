package com.sunbox.domain.metaData;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 磁盘SKU
 */
@Data
public class DiskSku implements Serializable {
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
     * 磁盘SKU名称
     */
    private String name;
    /**
     * 最大大小（单位G）
     */
    private String maxSize;
    /**
     * 最大吞吐量（MB/s）
     */
    private String maxThroughput;
    /**
     * 最大IOPS
     */
    private String maxIOPS;

    /**
     * 订阅id
     */
    private String subscriptionId;
    /**
     * 订阅name
     */
    private String subscriptionName;
}