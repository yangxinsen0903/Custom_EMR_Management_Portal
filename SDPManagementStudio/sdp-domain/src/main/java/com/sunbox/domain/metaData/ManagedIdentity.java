package com.sunbox.domain.metaData;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 托管标识
 */
@Data
public class ManagedIdentity implements Serializable {
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
     * 托管标识业务名称
     */
    private String miName;

    /**
     * Tenant ID
     */
    private String tenantId;
    /**
     * Client Id
     */
    private String clientId;
    /**
     * 资源ID
     */
    private String resourceId;
    /**
     * 资源
     */
    private String name;

    private String principalId;

    /**
     * 订阅id
     */
    private String subscriptionId;
    /**
     * 订阅name
     */
    private String subscriptionName;

}