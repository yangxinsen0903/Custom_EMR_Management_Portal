package com.sunbox.util;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.thread.ThreadUtil;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author : [niyang]
 * @className : KeyVaultUtil
 * @description : [描述说明该类的功能]
 * @createTime : [2022/12/8 10:26 PM]
 */
public class KeyVaultUtil {
    Logger logger = LoggerFactory.getLogger(KeyVaultUtil.class);

    @Autowired
    private TokenCredential tokenCredential;

    private SecretClient getSecretClient(String endpoint){
        Assert.notEmpty(endpoint,"获取keyVault: endpoint不能为空");
        SecretClient secretClient = new SecretClientBuilder()
                .vaultUrl(endpoint)
                .credential(tokenCredential)
                .buildClient();
        return secretClient;
    }

    public boolean setSecret(String name,String val,String endpoint){
        Integer i =0;
        while (true) {
            try {
                logger.info("开始设置KeyVault：" + name);
                this.getSecretClient(endpoint).setSecret(new KeyVaultSecret(name, val));
                logger.info("完成设置KeyVault：" + name);
                return true;
            } catch (Exception e) {
                logger.error("设置KeyVault失败: " + e.getMessage());
            }
            i++;
            logger.warn("设置KeyVault失败,重试："+i);
            if (i > 5){
                return false;
            }
            Integer delay = (int)Math.pow(2,i);
            ThreadUtil.sleep(1000L* delay);
        }
    }

    /**
     * 根据keyName获取KeyVault中保存的Value with retry
     *
     * @param name
     * @return
     */
    public String getSecretVal(String name,String endpoint){
        Integer i=0;
        while (true) {
            try {
                logger.info("开始获取KeyVault：" + name);
                KeyVaultSecret retrievedSecret = this.getSecretClient(endpoint).getSecret(name);
                logger.info("完成获取KeyVault：" + name);
                return retrievedSecret.getValue();
            } catch (Exception e) {
                logger.error("获取KeyVault失败, name:{}", name, e);
                if (e.getMessage().contains("SecretNotFound")) {
                    return null;
                }
            }

            i++;
            logger.warn("获取KeyVault失败,重试："+i);
            if (i > 5){
                return null;
            }
            Integer delay = (int)Math.pow(2,i);
            ThreadUtil.sleep(1000L* delay);
        }
    }


    public boolean delSecret(String secretName,String endpoint){

        try {
            // 先软删除。
            logger.info("开始软删除KeyVault：" + secretName);
            SyncPoller<DeletedSecret, Void> deletionPoller = this.getSecretClient(endpoint).beginDeleteSecret(secretName);
            deletionPoller.waitForCompletion();
            logger.info("成轼软删除KeyVault：" + secretName);
        }catch (Exception e){
            logger.error("软删除KeyVault失败：" + e.getMessage());
        }

        try {
            // 再彻底清除
            logger.info("开始清除KeyVault：" + secretName);
            this.getSecretClient(endpoint).purgeDeletedSecret(secretName);
            logger.info("成功清除KeyVault：" + secretName);
            return true;
        }catch (Exception e){
            logger.error("清除KeyVault失败: " + e.getMessage());
            return false;
        }
    }
}
