package com.azure.csu.tiger.ansible.agent.config;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


@Configuration
public class AzureConfig {

    @Value("${azure.credential.tenantid}")
    private String azureTenantid;

    @Value("${azure.credential.clientid}")
    private String azureClentid;

    @Value("${azure.credential.clientsecret}")
    private String azureClientsecret;

    @Value("${azure.credential.clientCert}")
    private String azureClientCert;


    @Bean
    public AzureProfile azureFile() {
        return new AzureProfile(AzureEnvironment.AZURE);
    }

//    @Bean
//    @Primary
//    public TokenCredential tokenCredential(AzureProfile profile) {
//        return new ClientSecretCredentialBuilder()
//                .clientId(azureClentid)
//                .clientSecret(azureClientsecret)
//                .tenantId(azureTenantid)
//                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
//                .build();
//    }

    @Bean(name = "tokenCredential")
    @Primary
    @ConditionalOnProperty(name = "azure.credential.clientAuth", havingValue = "secret", matchIfMissing = false)
    public TokenCredential tokenCredentialSecret(AzureProfile profile) {
        return new ClientSecretCredentialBuilder()
                .clientId(azureClentid)
                .clientSecret(azureClientsecret)
                .tenantId(azureTenantid)
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();
    }

    @Bean(name = "tokenCredential")
    @Primary
    @ConditionalOnProperty(name = "azure.credential.clientAuth", havingValue = "cert", matchIfMissing = false)
    public TokenCredential tokenCredentialCertificate(AzureProfile profile) {
        return new ClientCertificateCredentialBuilder()
                .clientId(azureClentid)
                .pemCertificate(azureClientCert)
                .tenantId(azureTenantid)
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();
    }

    @Value("${azure.keyvault.uri}")
    private String keyVaultUri;

    @Bean
    public SecretClient secretClient(TokenCredential tokenCredential) {
        return new SecretClientBuilder().vaultUrl(keyVaultUri).credential(tokenCredential).buildClient();
    }

}