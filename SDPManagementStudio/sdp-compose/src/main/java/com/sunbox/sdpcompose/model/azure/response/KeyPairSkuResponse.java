package com.sunbox.sdpcompose.model.azure.response;

/**
 * @Description: key pair response
 * @Title: KeyPairSkuResponse
 * @Package: com.sunbox.sdpcompose.model.azure.response
 * @Author: wangshihao
 * @Copyright: 版权
 * @CreateTime: 2022/12/6 19:11
 */
public class KeyPairSkuResponse extends BaseSkuResponse{
    private static final long serialVersionUID = 1L;

    private String keyVaultResourceId;

    private String privateKeySecretName;

    private String publicKeySecretName;


    public String getKeyVaultResourceId() {
        return keyVaultResourceId;
    }

    public void setKeyVaultResourceId(String keyVaultResourceId) {
        this.keyVaultResourceId = keyVaultResourceId;
    }

    public String getPrivateKeySecretName() {
        return privateKeySecretName;
    }

    public void setPrivateKeySecretName(String privateKeySecretName) {
        this.privateKeySecretName = privateKeySecretName;
    }

    public String getPublicKeySecretName() {
        return publicKeySecretName;
    }

    public void setPublicKeySecretName(String publicKeySecretName) {
        this.publicKeySecretName = publicKeySecretName;
    }
}
