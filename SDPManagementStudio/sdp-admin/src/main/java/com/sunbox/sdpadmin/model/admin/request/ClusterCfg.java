package com.sunbox.sdpadmin.model.admin.request;

import com.alibaba.fastjson.JSONObject;

public class ClusterCfg {
    private JSONObject cfg;
    private String classification;

    public JSONObject getcfg() { return cfg; }
    public void setcfg(JSONObject value) { this.cfg = value; }

    public String getClassification() { return classification; }
    public void setClassification(String value) { this.classification = value; }
}