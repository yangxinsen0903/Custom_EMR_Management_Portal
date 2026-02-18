package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author: wangda
 * @date: 2023/1/14
 */
public class ComponentInHost {

    @SerializedName("ServiceComponentInfo")
    private ServiceComponentInfo serviceComponentInfo;

    @SerializedName("host_components")
    private List<HostComponent> hostComponents;

    public ServiceComponentInfo getServiceComponentInfo() {
        return serviceComponentInfo;
    }

    public void setServiceComponentInfo(ServiceComponentInfo serviceComponentInfo) {
        this.serviceComponentInfo = serviceComponentInfo;
    }

    public List<HostComponent> getHostComponents() {
        return hostComponents;
    }

    public void setHostComponents(List<HostComponent> hostComponents) {
        this.hostComponents = hostComponents;
    }
}
