package sunbox.sdp.ambari.client.model.createclusterprocess;

import com.google.gson.annotations.SerializedName;

/**
 * 格式化，启动等 的ResourceFilter，与原来的RequestResourceFilter不一样在于序列化后的字段名称不一样。
 * @author: wangda
 * @date: 2022/12/14
 */
public class ResourceFilter {
    @SerializedName("service_name")
    private String serviceName;

    @SerializedName("component_name")
    private String componentName;

    @SerializedName("hosts")
    private String hosts;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getHosts() {
        return hosts;
    }

    public void setHosts(String hosts) {
        this.hosts = hosts;
    }
}
