package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

/**
 * 包装配置组，很多接口需要对ConfigGroup再包装一道
 * @author: wangda
 * @date: 2023/2/11
 */
public class ConfigGroupWrapper {

    @SerializedName("href")
    private String href;

    @SerializedName("ConfigGroup")
    private ConfigGroup configGroup;

    public static ConfigGroupWrapper of(ConfigGroup config) {
        ConfigGroupWrapper wrapper = new ConfigGroupWrapper();
        wrapper.setConfigGroup(config);
        return wrapper;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public ConfigGroup getConfigGroup() {
        return configGroup;
    }

    public void setConfigGroup(ConfigGroup configGroup) {
        this.configGroup = configGroup;
    }

    @Override
    public String toString() {
        return "ConfigGroupWrapper{" +
                "href='" + href + '\'' +
                ", configGroup=" + configGroup +
                '}';
    }
}
