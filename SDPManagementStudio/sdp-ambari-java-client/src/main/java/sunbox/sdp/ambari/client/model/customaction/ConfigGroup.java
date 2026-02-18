package sunbox.sdp.ambari.client.model.customaction;

import cn.hutool.core.collection.CollectionUtil;
import com.google.gson.annotations.SerializedName;

import java.util.*;

/**
 * 配置组
 * @author: wangda
 * @date: 2023/2/11
 */
public class ConfigGroup {
    @SerializedName("id")
    private Long id;

    @SerializedName("service_name")
    private String serviceName;

    /**
     * groupName + Tag是一个配置组的唯一标识
     */
    @SerializedName("group_name")
    private String groupName;

    @SerializedName("tag")
    private String tag;

    @SerializedName("cluster_name")
    private String clusterName;

    @SerializedName("description")
    private String description = "";

    @SerializedName("desired_configs")
    private List<Map<String, Object>> desiredConfigs;

    @SerializedName("hosts")
    private List<HostRole> hosts;

    @SerializedName("service_config_version_note")
    private String serviceConfigVersionNote;

    @SerializedName("version_tags")
    private List<Map> versionTags;

    /**
     * 增加一个配置
     * @param desiredConfig 一个配置文件的完整配置
     */
    public void addOneDesiredConfig(Map<String, Object> desiredConfig) {
        if (Objects.isNull(desiredConfigs)) {
            desiredConfigs = new ArrayList<>();
        }
        desiredConfigs.add(desiredConfig);
    }

    /**
     * 增加一个配置
     * @param classification 配置文件名称
     * @param configProperties 一个配置文件的配置值，不包括配置文件名
     */
    public void addOneDesiredConfig(String classification, Map<String, Object> configProperties) {
        Map<String, String> propertiesMap = new HashMap<>();
        configProperties.entrySet().forEach(entry -> {
            propertiesMap.put(entry.getKey(), String.valueOf(entry.getValue()));
        });

        Map<String, Object> aConfig = new HashMap<>();
        aConfig.put("type", classification);
        aConfig.put("properties", propertiesMap);
        addOneDesiredConfig(aConfig);
    }

    /**
     * 补全一个配置<br/>
     * 1. 如果配置文件不存在，新增；<br/>
     * 2. 如果配置文件已经存在，将不存的配置补全（不进行覆盖）<br/>
     * 注：本方法检查该配置文件是否是当前配置组的，需要在调用本方法前验证
     * @param classification 配置文件名称
     * @param configProperties 一个配置文件的配置值，不包括配置文件名
     */
    public void completeOneDesiredConfig(String classification, Map<String, Object> configProperties) {
        if (Objects.isNull(desiredConfigs)) {
            desiredConfigs = new ArrayList<>();
        }

        Map<String, Object> aDesiredCfg = null;
        for (Map<String, Object> cfg : desiredConfigs) {
            String configItemName = (String)cfg.get("type");
            if (Objects.equals(configItemName, classification)) {
                aDesiredCfg = (Map<String, Object>)cfg.get("properties");
            }
        }
        if (Objects.isNull(aDesiredCfg)) {
            addOneDesiredConfig(classification, configProperties);
            return;
        }

        for (Map.Entry<String, Object> entry : configProperties.entrySet()) {
            if (Objects.isNull(aDesiredCfg.get(entry.getKey()))) {
                aDesiredCfg.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
    }

    /**
     * 处理spark3的配置，对于配置为spark的配置，将其替换为spark3
     */
    public void handleSpark3Config() {
        if (CollectionUtil.isEmpty(desiredConfigs)) {
            return;
        }

        for (Map<String, Object> desiredConfig : desiredConfigs) {
            String type = (String)desiredConfig.get("type");
            Map<String, Object> properties = (Map<String, Object>)desiredConfig.get("properties");
            if (type.startsWith("spark") && !type.startsWith("spark3")) {
                type = type.replace("spark", "spark3");
                desiredConfig.put("type", type);
            }
        }
    }
    public void addHostGroupToYarnSite(){
        if (CollectionUtil.isEmpty(desiredConfigs)) {
            return;
        }
        for (Map<String, Object> desiredConfig : desiredConfigs) {
            String type = (String)desiredConfig.get("type");
            Map<String, Object> properties = (Map<String, Object>)desiredConfig.get("properties");
            if ("yarn-site".equalsIgnoreCase(type)) {
                properties.put("host.group",groupName);
            }
        }
    }

    public List<Map> getVersionTags() {
        return versionTags;
    }

    public void setVersionTags(List<Map> versionTags) {
        this.versionTags = versionTags;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Map<String, Object>> getDesiredConfigs() {
        return desiredConfigs;
    }

    public void setDesiredConfigs(List<Map<String, Object>> desiredConfigs) {
        this.desiredConfigs = desiredConfigs;
    }

    public List<HostRole> getHosts() {
        return hosts;
    }

    public void setHosts(List<HostRole> hosts) {
        this.hosts = hosts;
    }

    public String getServiceConfigVersionNote() {
        return serviceConfigVersionNote;
    }

    public void setServiceConfigVersionNote(String serviceConfigVersionNote) {
        this.serviceConfigVersionNote = serviceConfigVersionNote;
    }

    @Override
    public String toString() {
        return "ConfigGroup{" +
                "id=" + id +
                ", serviceName='" + serviceName + '\'' +
                ", groupName='" + groupName + '\'' +
                ", tag='" + tag + '\'' +
                ", clusterName='" + clusterName + '\'' +
                ", description='" + description + '\'' +
                ", desiredConfigs=" + desiredConfigs +
                ", hosts=" + hosts +
                ", serviceConfigVersionNote='" + serviceConfigVersionNote + '\'' +
                ", versionTags=" + versionTags +
                '}';
    }
}
