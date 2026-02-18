package com.sunbox.sdpcompose.properties;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author : [niyang]
 * @className : AzureServiceBusProperties
 * @description : [描述说明该类的功能]
 * @createTime : [2022/11/29 8:16 PM]
 */
@Qualifier("AzureServiceBusProperties")
@ConfigurationProperties(prefix = "azure.servicebus")
public class AzureServiceBusProperties {
    private String connectionString;

    private String topicName;

    private String subName;

    private String configs;

    private boolean showMessage;

    private boolean isLocal;

    public boolean isLocal() {
        return isLocal;
    }

    public void setLocal(boolean local) {
        isLocal = local;
    }

    public boolean isShowMessage() {
        return showMessage;
    }

    public void setShowMessage(boolean showMessage) {
        this.showMessage = showMessage;
    }

    public String getConfigs() {
        return configs;
    }

    public void setConfigs(String configs) {
        this.configs = configs;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getSubName() {
        return subName;
    }

    public void setSubName(String subName) {
        this.subName = subName;
    }
}
