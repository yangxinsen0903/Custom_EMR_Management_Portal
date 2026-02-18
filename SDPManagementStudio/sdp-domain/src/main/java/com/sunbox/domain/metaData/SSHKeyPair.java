package com.sunbox.domain.metaData;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 密钥对
 */
@Data
public class SSHKeyPair implements Serializable {
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
     * ssh 密钥对业务名称
     */
    private String name;
    /**
     * KeyVault的资源id
     */
    private String keyVaultResourceId;
    /**
     * KeyVault的资源名称
     */
    private String keyVaultResourceName;
    /**
     * 密钥在KeyVault的的名称
     */
    private String nameInKeyVault;
    /**
     * 密钥的资源ID
     */
    private String secretResourceId;

    /**
     * 订阅id
     */
    private String subscriptionId;
    /**
     * 订阅name
     */
    private String subscriptionName;

    /**
     * 1公钥 2私钥
     */
    private Integer keyType;

    public static Integer publicKeyType=1;
    public static Integer privateKeyType=2;

}