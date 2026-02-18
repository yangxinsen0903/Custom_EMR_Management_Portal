package com.sunbox.sdpcompose.service.ambari.blueprint;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sunbox.sdpcompose.util.JacksonUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Blueprint的配置项
 * @author: wangda
 * @date: 2022/12/5
 */
public class BlueprintConfiguration {
    /** Blueprint的配置项, 一般一个配置文件是一个配置项, 这个配置文件的详细配置内容在 */
    @JsonIgnore
    String configItemName;

    /** properties_attributes节点 */
    @JsonProperty("properties_attributes")
    Map<String, Object> propertiesAttributes = new HashMap<>();

    /** properties节点, 大数据组件配置文件的属性值 */
    @JsonProperty("properties")
    Map<String, Object> properties = new HashMap<>();

    /**
     * 设置PropertiesAttributes
     * @param flag 配置标识，如：final password等
     * @param attrs K-V格式的配置项
     */
    public void putPropertiesAttributes(String flag, Map<String, Object> attrs) {
        Map<String, Object> existAttrs = (Map<String, Object>)propertiesAttributes.get(flag);
        if (Objects.isNull(existAttrs)) {
            propertiesAttributes.put(flag, attrs);
        } else {
            existAttrs.putAll(attrs);
        }
    }

    public Map<String, BlueprintConfiguration> toMap() {
        Map<String, BlueprintConfiguration> map = new HashMap<>();
        map.put(this.getConfigItemName(), this);
        return map;
    }

    public BlueprintConfiguration() {

    }

    /**
     * 构造
     * @param configItemName 配置项名称,即:配置文件的标识
     */
    public BlueprintConfiguration(String configItemName) {
        this.configItemName = configItemName;
    }

    /**
     * 构造
     * @param configItemName 配置项名称,即:配置文件的标识
     * @param properties 配置文件的配置项
     */
    public BlueprintConfiguration(String configItemName, Map<String, Object> properties) {
        this.configItemName = configItemName;
        this.properties.putAll(properties);
    }

    public String getConfigItemName() {
        return configItemName;
    }

    public void setConfigItemName(String configItemName) {
        this.configItemName = configItemName;
    }

    public Map<String, Object> getPropertiesAttributes() {
        return propertiesAttributes;
    }

    public void setPropertiesAttributes(Map<String, Object> propertiesAttributes) {
        this.propertiesAttributes = propertiesAttributes;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * 查询一个属性值
     * @param key 属性的Key
     * @return 属性值
     */
    public Object getProperty(String key) {
        return this.properties.get(key);
    }

    /**
     * 增加一个属性值,如果存在, 就覆盖
     * @param key 属性的Key
     * @param value 属性的值
     */
    public void putProperties(String key, Object value) {
        this.properties.put(key, value);
    }

    /**
     * 批量增加配置
     * @param properties
     */
    public void putProperties(Map<String, Object> properties) {
        this.properties.putAll(properties);
    }

    @Override
    public String toString() {
        return JacksonUtils.toJson(this);
    }
}
