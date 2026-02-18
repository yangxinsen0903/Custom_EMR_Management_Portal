package com.sunbox.domain.metaData;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 子网
 */
@Data
public class Subnet implements Serializable {
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
     * 子网资源id
     */
    private String subnetId;
    /**
     * 子网资源名称
     */
    private String subnetName;
    /**
     * 子网业务名称
     */
    private String subnetDisplayName;

    /**
     * 虚拟网络资源id
     */
    private String virtualNetworkResourceId;
    /**
     * 虚拟网络资源Name
     */
    private String virtualNetworkName;

    /**
     * 订阅id
     */
    private String subscriptionId;
    /**
     * 订阅name
     */
    private String subscriptionName;

}