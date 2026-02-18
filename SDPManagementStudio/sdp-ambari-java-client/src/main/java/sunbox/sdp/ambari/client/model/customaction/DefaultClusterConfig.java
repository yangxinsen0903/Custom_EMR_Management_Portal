package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author: wangda
 * @date: 2023/2/15
 */
public class DefaultClusterConfig {

    @SerializedName("desired_config")
    List<DefaultConfigDesiredConfig> desiredConfig;

    /**
     * 增加一个配置
     * @param config
     */
    public void addDesiredConfig(DefaultConfigDesiredConfig config) {
        if (Objects.isNull(config)) {
            return;
        }

        if (Objects.isNull(desiredConfig)) {
            desiredConfig = new ArrayList<>();
        }
        desiredConfig.add(config);
    }

    public List<DefaultConfigDesiredConfig> getDesiredConfig() {
        return desiredConfig;
    }

    public void setDesiredConfig(List<DefaultConfigDesiredConfig> desiredConfig) {
        this.desiredConfig = desiredConfig;
    }
}
