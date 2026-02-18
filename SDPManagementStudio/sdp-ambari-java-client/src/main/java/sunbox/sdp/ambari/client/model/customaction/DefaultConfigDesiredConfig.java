package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 默认配置的DesiredConfig
 * @author: wangda
 * @date: 2023/2/15
 */
public class DefaultConfigDesiredConfig {

    @SerializedName("type")
    private String type;

    @SerializedName("properties")
    private Map<String, String> properties = new HashMap<>();

    @SerializedName("properties_attributes")
    private Map<String, Object> propertiesAttributes;

    @SerializedName("service_config_version_note")
    private String serviceConfigVersionNote;

    public DefaultConfigDesiredConfig(String type) {
        this.type = type;
    }

    public void putProperty(String key, String value) {
        properties.put(key, value);
    }

    public void putProperties(Map<String, String> properties) {
        this.properties.putAll(properties);
    }

    /**
     * 设置PropertiesAttributes
     * @param flag 配置标识，如：final password等
     * @param attrs K-V格式的配置项
     */
    public void putPropertiesAttributes(String flag, Map<String, Object> attrs) {
        if (Objects.isNull(propertiesAttributes)) {
            propertiesAttributes = new HashMap<>();
        }
        Map<String, Object> existAttrs = (Map<String, Object>)propertiesAttributes.get(flag);
        if (Objects.isNull(existAttrs)) {
            propertiesAttributes.put(flag, attrs);
        } else {
            existAttrs.putAll(attrs);
        }
    }

    public Map<String, Object> getPropertiesAttributes() {
        return propertiesAttributes;
    }

    public void setPropertiesAttributes(Map<String, Object> propertiesAttributes) {
        this.propertiesAttributes = propertiesAttributes;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String getServiceConfigVersionNote() {
        return serviceConfigVersionNote;
    }

    public void setServiceConfigVersionNote(String serviceConfigVersionNote) {
        this.serviceConfigVersionNote = serviceConfigVersionNote;
    }
}
