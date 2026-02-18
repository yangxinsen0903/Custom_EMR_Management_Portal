package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

/**
 * @author: wangda
 * @date: 2023/2/15
 */
public class DefaultClusterConfigWrapper {

    @SerializedName("Clusters")
    private DefaultClusterConfig clusterDesiredConfig = new DefaultClusterConfig();

    /**
     * 增加一个配置文件的配置
     * @param config
     */
    public void addConfig(DefaultConfigDesiredConfig config) {
        clusterDesiredConfig.addDesiredConfig(config);
    }

    public DefaultClusterConfig getClusterDesiredConfig() {
        return clusterDesiredConfig;
    }

    public void setClusterDesiredConfig(DefaultClusterConfig clusterDesiredConfig) {
        this.clusterDesiredConfig = clusterDesiredConfig;
    }
}
