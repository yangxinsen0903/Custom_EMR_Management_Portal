package com.sunbox.domain;

public class AmbariConfigItemRequest extends PageRequest {

    private String serviceCode;
    /**
     * 高可用
     */
    private String itemType;
    private String configTypeCode;
    private String stackCode;
    private String key;


    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getConfigTypeCode() {
        return configTypeCode;
    }

    public void setConfigTypeCode(String configTypeCode) {
        this.configTypeCode = configTypeCode;
    }

    public String getStackCode() {
        return stackCode;
    }

    public void setStackCode(String stackCode) {
        this.stackCode = stackCode;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }


}
