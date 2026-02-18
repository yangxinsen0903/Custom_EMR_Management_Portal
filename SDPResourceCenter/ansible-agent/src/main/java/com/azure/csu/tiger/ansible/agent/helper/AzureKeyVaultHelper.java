package com.azure.csu.tiger.ansible.agent.helper;


import com.azure.core.credential.TokenCredential;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AzureKeyVaultHelper {

    private static final Logger logger = LoggerFactory.getLogger(AzureKeyVaultHelper.class);

    @Autowired
    private SecretClient secretClient;

    @Value("${ansible.scriptpath}")
    private String scriptPath;

    @Value("${ansible.privatekeyname}")
    private String privatekeyname;

    @Autowired
    private TokenCredential tokenCredential;

    public String getKeyValueFromKV(String secretName) {
        KeyVaultSecret retrievedSecret = secretClient.getSecret(secretName);
        String secretValue = retrievedSecret.getValue();
        String fullFilePath = scriptPath+privatekeyname;
        FileHelper.createFile(scriptPath+privatekeyname,secretValue,true);
        FileHelper.ChownFile(fullFilePath);

        logger.info("Chown private key pem file: " + fullFilePath);
        return fullFilePath;
     }

    public String getKeyValueFromKV(String kvName, String secretName) {
        if (!StringUtils.hasText(kvName)) {
            return null;
        }
        String keyVaultUri = String.format("https://%s.vault.azure.net/", kvName);
        SecretClient secretClient = new SecretClientBuilder().vaultUrl(keyVaultUri).credential(tokenCredential).buildClient();
        KeyVaultSecret retrievedSecret = secretClient.getSecret(secretName);
        String secretValue = retrievedSecret.getValue();
        String fullFilePath = scriptPath+kvName;
        FileHelper.createFile(scriptPath+kvName,secretValue,true);
        FileHelper.ChownFile(fullFilePath);

        logger.info("Chown private key pem file: " + fullFilePath);
        return fullFilePath;
     }


}

