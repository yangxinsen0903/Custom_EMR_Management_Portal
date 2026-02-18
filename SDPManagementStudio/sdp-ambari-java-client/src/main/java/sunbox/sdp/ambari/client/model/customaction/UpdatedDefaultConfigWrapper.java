package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author: wangda
 * @date: 2023/2/15
 */
public class UpdatedDefaultConfigWrapper {

    @SerializedName("href")
    private String href;

    @SerializedName("group_id")
    private Integer groupId;

    @SerializedName("group_name")
    private String groupName;

    @SerializedName("service_config_version")
    private String serviceConfigVersion;

    @SerializedName("service_config_version_note")
    private String serviceConfigVersionNote;

    @SerializedName("service_name")
    private String serviceName;

    @SerializedName("configurations")
    private List<UpdatedDefaultConfig> configurations;

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getServiceConfigVersion() {
        return serviceConfigVersion;
    }

    public void setServiceConfigVersion(String serviceConfigVersion) {
        this.serviceConfigVersion = serviceConfigVersion;
    }

    public String getServiceConfigVersionNote() {
        return serviceConfigVersionNote;
    }

    public void setServiceConfigVersionNote(String serviceConfigVersionNote) {
        this.serviceConfigVersionNote = serviceConfigVersionNote;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<UpdatedDefaultConfig> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(List<UpdatedDefaultConfig> configurations) {
        this.configurations = configurations;
    }
}
