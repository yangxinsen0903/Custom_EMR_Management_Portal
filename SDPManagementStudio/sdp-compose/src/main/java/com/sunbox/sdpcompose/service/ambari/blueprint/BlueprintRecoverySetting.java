package com.sunbox.sdpcompose.service.ambari.blueprint;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Blueprint服务配置项
 * @author: wangda
 * @date: 2022/12/5
 */
public class BlueprintRecoverySetting {

    @JsonProperty("recovery_enabled")
    String recoveryEnabled;

    @JsonProperty("credential_store_enabled")
    String credentialStoreEnabled;

    @JsonProperty("name")
    String name;

    public static BlueprintRecoverySetting of(boolean recoveryEnabled, String name) {
        BlueprintRecoverySetting setting = new  BlueprintRecoverySetting();
        setting.setRecoveryEnabled(String.valueOf(recoveryEnabled));
        setting.setName(name);

        return setting;
    }

    public static BlueprintRecoverySetting of(boolean recoveryEnabled, boolean credentialStoreEnabled, String name) {
        BlueprintRecoverySetting setting = new  BlueprintRecoverySetting();
        setting.setRecoveryEnabled(String.valueOf(recoveryEnabled));
        setting.setCredentialStoreEnabled(String.valueOf(credentialStoreEnabled));
        setting.setName(name);

        return setting;
    }

    public String isRecoveryEnabled() {
        return recoveryEnabled;
    }

    public void setRecoveryEnabled(String recoveryEnabled) {
        this.recoveryEnabled = recoveryEnabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCredentialStoreEnabled() {
        return credentialStoreEnabled;
    }

    public void setCredentialStoreEnabled(String credentialStoreEnabled) {
        this.credentialStoreEnabled = credentialStoreEnabled;
    }
}
