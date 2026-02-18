package sunbox.sdp.ambari.client.model.customaction;

import cn.hutool.core.collection.CollectionUtil;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Objects;

/**
 * 查询默认的集群配置的响应
 * @author: wangda
 * @date: 2023/2/15
 */
public class DefaultClusterConfigResponse {

    @SerializedName("href")
    private String href;

    @SerializedName("items")
    private List<DefaultClusterServiceConfig> items;

    /**
     * 根据配置类型查找对应的配置项
     * @param configType 配置项，如 core-site
     * @return 具体的配置，如果没查到， 返回null
     */
    public DefaultConfigDesiredConfig findConfigByConfigType(String configType) {
        if (CollectionUtil.isEmpty(items)) {
            return null;
        }

        for (DefaultClusterServiceConfig serviceConfig : items) {
            List<DefaultConfigDesiredConfig> configurations = serviceConfig.getConfigurations();
            if (CollectionUtil.isEmpty(configurations)) {
                continue;
            }

            for (DefaultConfigDesiredConfig configuration : configurations) {
                if (Objects.equals(configType, configuration.getType())) {
                    return configuration;
                }
            }
        }

        return null;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public List<DefaultClusterServiceConfig> getItems() {
        return items;
    }

    public void setItems(List<DefaultClusterServiceConfig> items) {
        this.items = items;
    }
}
