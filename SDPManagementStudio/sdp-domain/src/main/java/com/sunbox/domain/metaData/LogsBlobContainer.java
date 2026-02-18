package com.sunbox.domain.metaData;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 日志桶
 */
@Data
public class LogsBlobContainer implements Serializable {
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
     * 日志桶业务名称
     */
    private String logName;

    /**
     * 存储账户名称
     */
    private String storageAccountName;
    /**
     * 存储账户id
     */
    private String resourceId;
    /**
     * 日志桶
     */
    private String name;
    /**
     * 日志桶的URL地址
     */
    private String blobContainerUrl;

    /**
     * 订阅id
     */
    private String subscriptionId;
    /**
     * 订阅name
     */
    private String subscriptionName;

}