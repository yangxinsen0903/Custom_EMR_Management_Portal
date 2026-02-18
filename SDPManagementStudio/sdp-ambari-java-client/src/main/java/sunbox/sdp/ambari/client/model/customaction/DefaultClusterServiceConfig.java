package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * @author: wangda
 * @date: 2023/2/15
 */
public class DefaultClusterServiceConfig {
    private static Logger logger = LoggerFactory.getLogger(DefaultClusterServiceConfig.class);

    @SerializedName("href")
    private String href;

    @SerializedName("cluster_name")
    private String clusterName;


    @SerializedName("configurations")
    private List<DefaultConfigDesiredConfig> configurations;

    /**
     * 从Ambari返回的URL中提取出Service名
     * @return
     */
    public String extractServiceName() {
        if (Objects.isNull(href)) {
            return "";
        }

        try {
            int index = href.indexOf("service_name=");
            String s = href.substring(index);
            index = s.indexOf("&");
            s = s.substring(0, index);
            String[] params = s.split("=");
            if (Objects.equals(2, params.length)) {
                return params[1];
            } else {
                return "";
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return "";
        }
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public List<DefaultConfigDesiredConfig> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(List<DefaultConfigDesiredConfig> configurations) {
        this.configurations = configurations;
    }
}
