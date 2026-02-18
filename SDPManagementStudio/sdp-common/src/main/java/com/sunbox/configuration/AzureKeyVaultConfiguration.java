package com.sunbox.configuration;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;

/**
 * @author : [niyang]
 * @className : AzureKeyVaultConfiguration
 * @description : [AzureKeyVault配置]
 * @createTime : [2022/12/8 10:12 PM]
 */
 @Configuration
public class AzureKeyVaultConfiguration {

     private Logger logger = LoggerFactory.getLogger(AzureKeyVaultConfiguration.class);

    // @Value("${keyvault.uri}")
    // private String keyVaultUri;

    @Value("${keyvault.clientid}")
    private String clientId;

    @Value("${keyvault.clientsecret}")
    private String clientSecret;

    @Value("${keyvault.tenantid}")
    private String tenantId;

    @Value("${keyvault.certificate}")
    private String certificate;

    @Value("${keyvault.certificate.enabled:false}")
    private Boolean certEnabled;


    @Bean
    public TokenCredential getClientSecretCredential(){
        if (certEnabled) {
            if (StrUtil.isBlank(certificate)) {
                throw new RuntimeException("KeyVault的ServicePrincipal开启了证书认证，但是未找到证书内容，请检查是否正确配置了证书内容：config_detail -> keyvault.certificate");
            }

            // 将证书内容保存到文件中
            String certSavePath = "/home/cert/sdp-cert.pem";
            FileUtil.writeString(certificate, certSavePath, "UTF-8");

            TokenCredential tokenCredential = new ClientCertificateCredentialBuilder()
                    .clientId(clientId)
                    .tenantId(tenantId)
                    .authorityHost(AzureAuthorityHosts.AZURE_PUBLIC_CLOUD)
                    .pemCertificate(certSavePath)
                    .build();
            logger.info("Service Principal开启证书认证模式");
            return tokenCredential;
        } else {
            ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .tenantId(tenantId)
                    .authorityHost(AzureAuthorityHosts.AZURE_PUBLIC_CLOUD)
                    .build();
            logger.info("Service Principal开启密钥认证模式");
            return clientSecretCredential;
        }
    }

}
