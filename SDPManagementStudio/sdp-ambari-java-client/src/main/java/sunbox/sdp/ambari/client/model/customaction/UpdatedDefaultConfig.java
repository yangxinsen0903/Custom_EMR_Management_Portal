package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * @author: wangda
 * @date: 2023/2/15
 */
public class UpdatedDefaultConfig {
    @SerializedName("clusterName")
    private String clusterName;

    @SerializedName("stackId")
    private Map<String, String> stackId;

    @SerializedName("type")
    private String type;

    @SerializedName("versionTag")
    private String versionTag;

    @SerializedName("version")
    private Integer version;

    @SerializedName("serviceConfigVersions")
    private Object serviceConfigVersions;

    @SerializedName("configs")
    private Map<String, String> configs;

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Map<String, String> getStackId() {
        return stackId;
    }

    public void setStackId(Map<String, String> stackId) {
        this.stackId = stackId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVersionTag() {
        return versionTag;
    }

    public void setVersionTag(String versionTag) {
        this.versionTag = versionTag;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Object getServiceConfigVersions() {
        return serviceConfigVersions;
    }

    public void setServiceConfigVersions(Object serviceConfigVersions) {
        this.serviceConfigVersions = serviceConfigVersions;
    }

    public Map<String, String> getConfigs() {
        return configs;
    }

    public void setConfigs(Map<String, String> configs) {
        this.configs = configs;
    }
}
