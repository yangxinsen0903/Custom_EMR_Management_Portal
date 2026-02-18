package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author: wangda
 * @date: 2023/1/9
 */
public class StartHostComponentRequest {
    @SerializedName("RequestInfo")
    private RequestInfo requestInfo = new RequestInfo();


    @SerializedName("Body")
    private CommonHostRoleBody body = CommonHostRoleBody.of("STARTED");

    public static StartHostComponentRequest of(String clusterName, List<String> hosts, List<String> components) {
        StartHostComponentRequest request = new StartHostComponentRequest();
        request.requestInfo = new RequestInfo();
        OperationLevel level = new OperationLevel();
        request.requestInfo.setOperationLevel(level);
        request.requestInfo.getOperationLevel().setLevel(OperationLevelEnum.CLUSTER.name());
        request.requestInfo.getOperationLevel().setClusterName(clusterName);
        request.requestInfo.setQueryHostComponentAndHostAndState(hosts, components, "INSTALLED");
        return request;
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
