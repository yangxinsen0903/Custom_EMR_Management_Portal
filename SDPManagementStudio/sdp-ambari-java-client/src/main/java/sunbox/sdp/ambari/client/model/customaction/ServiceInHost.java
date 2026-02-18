package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 一个Service安装于的主机的信息
 * @author: wangda
 * @date: 2023/1/14
 */
public class ServiceInHost {

    @SerializedName("Hosts")
    private HostRole host;

    @SerializedName("host_components")
    private List<HostComponent> hostComponents;

    public HostRole getHost() {
        return host;
    }

    public void setHost(HostRole host) {
        this.host = host;
    }

    public List<HostComponent> getHostComponents() {
        return hostComponents;
    }

    public void setHostComponents(List<HostComponent> hostComponents) {
        this.hostComponents = hostComponents;
    }
}
