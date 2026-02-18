package com.azure.csu.tiger.rm.api.config;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.resourcegraph.ResourceGraphManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureConfig {

    @Value("${azure.clientId}")
    private String clientId;
    @Value("${azure.clientSecret}")
    private String clientSecret;
    @Value("${azure.clientCert}")
    private String clientCert;
    @Value("${azure.tenantId}")
    private String tenantId;

    @Bean
    public AzureProfile azureFile() {
        return new AzureProfile(AzureEnvironment.AZURE);
    }

    @Bean(name = "tokenCredential")
    @ConditionalOnProperty(name = "azure.clientAuth", havingValue = "secret", matchIfMissing = false)
    public TokenCredential tokenCredentialSecret(AzureProfile profile) {
        return new ClientSecretCredentialBuilder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .tenantId(tenantId)
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();
    }

    @Bean(name = "tokenCredential")
    @ConditionalOnProperty(name = "azure.clientAuth", havingValue = "cert", matchIfMissing = false)
    public TokenCredential tokenCredentialCertificate(AzureProfile profile) {
        return new ClientCertificateCredentialBuilder()
                .clientId(clientId)
                .pemCertificate(clientCert)
                .tenantId(tenantId)
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();
    }

//    @Bean
//    public AzureResourceManager azureResourceManager(TokenCredential tokenCredential, AzureProfile profile) {
//        return AzureResourceManager
//                .configure()
//                .authenticate(tokenCredential, profile)
//                .withSubscription(subscriptionId);
//    }

    @Bean
    public ResourceGraphManager resourceGraphManager(TokenCredential tokenCredential, AzureProfile profile) {
        return ResourceGraphManager
                .authenticate(tokenCredential, profile);
    }

}
