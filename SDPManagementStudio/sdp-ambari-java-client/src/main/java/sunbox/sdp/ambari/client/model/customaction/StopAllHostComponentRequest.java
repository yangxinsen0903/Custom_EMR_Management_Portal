package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 关闭主机所有组件的请求对象
 * @author: wangda
 * @date: 2023/1/11
 */
public class StopAllHostComponentRequest {

    @SerializedName("RequestInfo")
    private RequestInfo requestInfo;

    @SerializedName("Body")
    private CommonHostRoleBody body;

    public static StopAllHostComponentRequest of(String clusterName, List<String> hosts, List<String> components) {
        StopAllHostComponentRequest request = new StopAllHostComponentRequest();

        OperationLevel level = new OperationLevel();
        level.setLevel(OperationLevelEnum.CLUSTER.name());
        level.setClusterName(clusterName);

        RequestInfo reqInfo = new RequestInfo();
        reqInfo.setOperationLevel(level);
        reqInfo.setQuery(buildQueryString(hosts, components));

        CommonHostRoleBody body = new CommonHostRoleBody();
        body.setHostRole(HostRole.of(null, "INSTALLED"));

        request.body = body;
        request.requestInfo = reqInfo;

        return request;
    }

    public static StopAllHostComponentRequest of(String clusterName, String serviceName, String host, String component) {
        StopAllHostComponentRequest request = new StopAllHostComponentRequest();


        OperationLevel level = new OperationLevel();
        level.setLevel(OperationLevelEnum.HOST_COMPONENT.name());
        level.setClusterName(clusterName);
        level.setHostName(host);
        level.setServiceName(serviceName);

        RequestInfo reqInfo = new RequestInfo();
        reqInfo.setOperationLevel(level);
        reqInfo.setContext("Stop " + component);

        CommonHostRoleBody body = new CommonHostRoleBody();
        body.setHostRole(HostRole.of(null, "INSTALLED"));

        request.body = body;
        request.requestInfo = reqInfo;

        return request;
    }

    private static String buildQueryString(List<String> hosts, List<String> components) {
        // (HostRoles/component_name.in(DATANODE,NODEMANAGER)&HostRoles/host_name=sunbox-dev-vm09.vmdns.sunbox.com)
        // |(HostRoles/component_name.in(DATANODE,NODEMANAGER)&HostRoles/host_name=sunbox-dev-vm11.vmdns.sunbox.com)
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        String componentStr = "HostRoles/component_name.in(" + StringUtils.join(components, ",") + ")";
        for (String host : hosts) {
            if (sb.length() > 1) {
                sb.append("|");
            }
            sb.append(componentStr)
                    .append("&HostRoles/host_name=")
                    .append(host);
        }

        sb.append(")");

        return sb.toString();
    }


    public RequestInfo getRequestInfo() {
        return requestInfo;
    }

    public void setRequestInfo(RequestInfo requestInfo) {
        this.requestInfo = requestInfo;
    }

    public CommonHostRoleBody getBody() {
        return body;
    }

    public void setBody(CommonHostRoleBody body) {
        this.body = body;
    }
}
