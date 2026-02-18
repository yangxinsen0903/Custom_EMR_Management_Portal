package sunbox.sdp.ambari.client.model.customaction;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 重启一个服务的请求对象
 * @author: wangda
 * @date: 2023/2/6
 */
public class RestartServiceRequest {
    /** RequestInfo节点 */
    @SerializedName("RequestInfo")
    private RequestInfo requestInfo;

    @SerializedName("Requests/resource_filters")
    private List<RequestResourceFilter> filters;

    public static RestartServiceRequest of(String clusterName, String serviceName, List<RequestResourceFilter> filters) {
        Preconditions.checkNotNull(filters, "通过Ambari重启服务时，重启服务的组件信息不能为空");

        RestartServiceRequest request = new RestartServiceRequest();
        request.requestInfo = new RequestInfo();
        request.filters = filters;

        OperationLevel level = OperationLevel.of(OperationLevelEnum.SERVICE, clusterName, serviceName);
        request.requestInfo.setOperationLevel(level);

        request.requestInfo.setCommand("RESTART");
        request.requestInfo.setContext("Restart all components for " + serviceName);
        request.requestInfo.setOperationLevel(level);

        return request;
    }

    public RequestInfo getRequestInfo() {
        return requestInfo;
    }

    public void setRequestInfo(RequestInfo requestInfo) {
        this.requestInfo = requestInfo;
    }

    public List<RequestResourceFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<RequestResourceFilter> filters) {
        this.filters = filters;
    }
}
