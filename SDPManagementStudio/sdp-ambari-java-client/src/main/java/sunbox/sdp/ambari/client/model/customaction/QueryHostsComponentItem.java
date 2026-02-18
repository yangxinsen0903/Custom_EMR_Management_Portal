package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 查询主机组件返回的一个主机项
 * @author: wangda
 * @date: 2023/1/12
 */
public class QueryHostsComponentItem {

    @SerializedName("Hosts")
    private Hosts host;

    @SerializedName("host_components")
    private List<HostComponent> hostComponents;

    public Hosts getHost() {
        return host;
    }

    public void setHost(Hosts host) {
        this.host = host;
    }

    public List<HostComponent> getHostComponents() {
        return hostComponents;
    }

    public void setHostComponents(List<HostComponent> hostComponents) {
        this.hostComponents = hostComponents;
    }
}
