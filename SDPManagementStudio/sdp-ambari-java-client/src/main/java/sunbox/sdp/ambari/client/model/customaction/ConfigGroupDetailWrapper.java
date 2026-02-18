package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

public class ConfigGroupDetailWrapper {
    @SerializedName("href")
    private String href;
    @SerializedName("tag")
    private String tag;
    @SerializedName("type")
    private String type;
    @SerializedName("version")
    private Integer version;

    private String groupName;


    @SerializedName("properties")
    private HashMap<String,Object> properties;

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }


    public HashMap<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(HashMap<String, Object> properties) {
        this.properties = properties;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
