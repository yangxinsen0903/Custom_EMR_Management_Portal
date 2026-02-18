package sunbox.sdp.ambari.client.model.createclusterprocess;

import com.google.gson.annotations.SerializedName;
import sunbox.sdp.ambari.client.model.RequestInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * POST /api/v1/clusters/sunbox/requests 接口的请求对象
 * @author: wangda
 * @date: 2022/12/14
 */
public class ClusterRequest {
    /** 对集群请求操作的信息，如：格式化，启动等  */
    @SerializedName("RequestInfo")
    private RequestInfo requestInfo;

    /**
     * 操作对象：如哪个服务的哪个组件， 哪台主机
     */
    @SerializedName("Requests/resource_filters")
    private List<ResourceFilter> resourceFilter;


    public RequestInfo getRequestInfo() {
        return requestInfo;
    }

    public void setRequestInfo(RequestInfo requestInfo) {
        this.requestInfo = requestInfo;
    }

    public List<ResourceFilter> getResourceFilter() {
        return resourceFilter;
    }

    public void setResourceFilter(List<ResourceFilter> resourceFilter) {
        this.resourceFilter = resourceFilter;
    }

    /**
     * 构建一个格式化的请求对象
     * @param service 格式化的服务名
     * @param component 格式化的组件名
     * @param host 格式化的主机名
     * @return
     */
    public static ClusterRequest buildFormatRequest(String service, String component, String host) {
        ClusterRequest request = new ClusterRequest();
        FormatRequestInfo formatRequestInfo = new FormatRequestInfo();
        formatRequestInfo.setCommand("FORMAT");
        formatRequestInfo.setContext("FORMAT");
        request.setRequestInfo(formatRequestInfo);

        ResourceFilter filter = new ResourceFilter();
        filter.setServiceName(service);
        filter.setComponentName(component);
        filter.setHosts(host);
        List<ResourceFilter> list = new ArrayList<>();
        list.add(filter);
        request.setResourceFilter(list);
        return request;
    }

    /**
     * 构建一个Bootstrap的请求对象
     * @param service 服务名
     * @param component 组件名
     * @param host 主机名
     * @return
     */
    public static ClusterRequest buildBootstrapStandByRequest(String service, String component, String host) {
        ClusterRequest request = new ClusterRequest();
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setCommand("BOOTSTRAP_STANDBY");
        requestInfo.setContext("BOOTSTRAP_STANDBY");
        request.setRequestInfo(requestInfo);

        ResourceFilter filter = new ResourceFilter();
        filter.setServiceName(service);
        filter.setComponentName(component);
        filter.setHosts(host);
        List<ResourceFilter> list = new ArrayList<>();
        list.add(filter);
        request.setResourceFilter(list);
        return request;
    }
}
