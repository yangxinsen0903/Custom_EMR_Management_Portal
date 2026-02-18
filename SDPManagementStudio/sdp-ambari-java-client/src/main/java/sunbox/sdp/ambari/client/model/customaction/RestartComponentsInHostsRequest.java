package sunbox.sdp.ambari.client.model.customaction;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 重启多个主机中的多个组件的请求对象
 * @author: wangda
 * @date: 2023/2/6
 */
public class RestartComponentsInHostsRequest {
    /** RequestInfo节点 */
    @SerializedName("RequestInfo")
    private RequestInfo requestInfo;

    @SerializedName("Requests/resource_filters")
    private List<RequestResourceFilter> filters;

    public static RestartComponentsInHostsRequest of(String clusterName, String serviceName, List<String> hosts, List<String> components) {
        Preconditions.checkNotNull(clusterName, "集群名称不能为空");
        Preconditions.checkNotNull(hosts, "主机列表不能为空");
        Preconditions.checkNotNull(components, "组件列表不能为空");

        RestartComponentsInHostsRequest request = new RestartComponentsInHostsRequest();
        request.requestInfo = new RequestInfo();
        OperationLevel level = OperationLevel.of(OperationLevelEnum.HOST, clusterName, null);
        request.requestInfo.setOperationLevel(level);

        request.requestInfo.setCommand("RESTART");
        request.requestInfo.setContext("Restart all components on the selected hosts");
        request.requestInfo.setOperationLevel(level);

        request.filters = generateResourceFilters(serviceName, hosts, components);

        return request;
    }

    public static RestartComponentsInHostsRequest of(String clusterName, String serviceName, Set<String> hosts) {
        Preconditions.checkNotNull(clusterName, "集群名称不能为空");
        Preconditions.checkNotNull(hosts, "主机列表不能为空");

        RestartComponentsInHostsRequest request = new RestartComponentsInHostsRequest();
        request.requestInfo = new RequestInfo();
        OperationLevel level = OperationLevel.of(OperationLevelEnum.HOST, clusterName, null);
        request.requestInfo.setOperationLevel(level);

        request.requestInfo.setCommand("RESTART");
        request.requestInfo.setContext("Restart all components on the selected hosts");
        request.requestInfo.setOperationLevel(level);

        request.filters = generateResourceFilters(serviceName, hosts);

        return request;
    }

    public static List<RequestResourceFilter> generateResourceFilters(String serviceName, List<String> hosts, List<String> components) {
        List<RequestResourceFilter> filters = new ArrayList<>();
        for (String host : hosts) {
            for (String component : components) {
                RequestResourceFilter filter = new RequestResourceFilter();
                filter.setHosts(host);
                filter.setComponentName(component);
                filter.setServiceName(serviceName);
                filters.add(filter);
            }
        }
        return filters;
    }

    public static List<RequestResourceFilter> generateResourceFilters(String serviceName, Set<String> hosts) {
        List<RequestResourceFilter> filters = new ArrayList<>();
        for (String host : hosts) {
        String[] split = host.split(":");
            RequestResourceFilter filter = new RequestResourceFilter();
            filter.setHosts(split[0]);
            filter.setComponentName(split[1]);
            filter.setServiceName(serviceName);
            filters.add(filter);
        }
        return filters;
    }
}
