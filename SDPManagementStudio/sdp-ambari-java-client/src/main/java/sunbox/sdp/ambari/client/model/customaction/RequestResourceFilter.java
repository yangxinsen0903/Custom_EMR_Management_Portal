package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

/**
 * 请求资源过滤器
 * @author: wangda
 * @date: 2023/1/14
 */
public class RequestResourceFilter {

    /** 大数据服务的名字 */
    @SerializedName("service_name")
    private String serviceName;

    /** 大数据组件的名称 */
    @SerializedName("component_name")
    private String componentName;

    /** 主机名， 多个主机时使用半角的逗号分隔 */
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
