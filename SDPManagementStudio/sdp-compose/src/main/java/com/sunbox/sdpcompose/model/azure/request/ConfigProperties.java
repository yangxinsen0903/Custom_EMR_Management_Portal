package com.sunbox.sdpcompose.model.azure.request;

import java.util.Map;

public class ConfigProperties {

    private String confItemName;

    private Map<String, Object> confs;

    public String getConfItemName() {
        return confItemName;
    }

    public void setConfItemName(String confItemName) {
        this.confItemName = confItemName;
    }

    public Map<String, Object> getConfs() {
        return confs;
    }

    public void setConfs(Map<String, Object> confs) {
        this.confs = confs;
    }
}
