package com.sunbox.sdpadmin.model.shein.request;

import java.util.List;

public class InstanceGroupNewConfigElement {
    private InstanceGroupAddConfig instanceGroupAddConfig;
    private List<InstanceGroupCfg> instanceGroupCfgs;

    public InstanceGroupAddConfig getInstanceGroupAddConfig() { return instanceGroupAddConfig; }
    public void setInstanceGroupAddConfig(InstanceGroupAddConfig value) { this.instanceGroupAddConfig = value; }

    public List<InstanceGroupCfg> getInstanceGroupCfgs() { return instanceGroupCfgs; }
    public void setInstanceGroupCfgs(List<InstanceGroupCfg> value) { this.instanceGroupCfgs = value; }
}