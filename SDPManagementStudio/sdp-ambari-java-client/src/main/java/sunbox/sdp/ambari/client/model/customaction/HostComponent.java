package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

/**
 * 主机组件对象
 * @author: wangda
 * @date: 2023/1/12
 */
public class HostComponent {

    @SerializedName("href")
    private String href;

    @SerializedName("HostRoles")
    private HostRole hostRole;

    @SerializedName("metrics")
    private ComponentMetrics metrics;


    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public HostRole getHostRole() {
        return hostRole;
    }

    public void setHostRole(HostRole hostRole) {
        this.hostRole = hostRole;
    }

    public ComponentMetrics getMetrics() {
        return metrics;
    }

    public void setMetrics(ComponentMetrics metrics) {
        this.metrics = metrics;
    }
}
