package com.sunbox.sdpcompose.model.azure.request;

import lombok.Data;

import java.util.List;

@Data
public class AzureExecuteJobPlaybookRequest {
    /**
     * 版本，v1
     */
    private String apiVersion;
    /**
     * IP地址，用半角逗号分隔
     */
    private String nodeList;
    /**
     * playbook，Blob上playbook地址
     */
    private String playbookUri;
    /**
     * 脚本tar包路径，存储在Blob上面
     */
    private List<String> scriptFileUris;
    /**
     * 扩展参数
     * */
    private String extraVars;
    /**
     * 执行单个节点playbook的超 时时间，秒
     */
    private Integer timeout;
    /**
     * 消息ID
     */
    private String transactionId;

    /**
     * 1 ：sdp 2：custom
     * */
    private Integer playbookType;

    private String region;

    public static Integer playbookType_sdp = 1;
    public static Integer playbookType_custom = 2;

    private String sshKeyVaultName;
    private String sshPrivateSecretName;
    private String subscriptionId;
}