package com.sunbox.sdpadmin.model.shein.request;

import java.util.Map;

public class Empty {
    /**
     * 具体配置，k-v形式的Map，如 dfs.replication -> 2
     */
    private Map<String, Object> cfg;
    /**
     * 标识，各个配置文件的名称，如 hdfs-site
     */
    private String classification;
    /**
     * 属性配置，k-v形式的Map
     */
    private Map<String, Object> properties;

    public Map<String, Object> getcfg() { return cfg; }
    public void setcfg(Map<String, Object> value) { this.cfg = value; }

    public String getClassification() { return classification; }
    public void setClassification(String value) { this.classification = value; }

    public Map<String, Object> getProperties() { return properties; }
    public void setProperties(Map<String, Object> value) { this.properties = value; }
}
