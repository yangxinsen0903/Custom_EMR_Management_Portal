package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 安装主机的组件
 * @author: wangda
 * @date: 2023/1/8
 */
public class InstallHostComponentRequest {

    @SerializedName("RequestInfo")
    private RequestInfo requestInfo = new RequestInfo();


    @SerializedName("Body")
    private CommonHostRoleBody body = new CommonHostRoleBody();

    public static InstallHostComponentRequest of(String clusterName, List<String> hosts) {
        InstallHostComponentRequest request = new InstallHostComponentRequest();
        request.requestInfo.setQueryHostRoleHostName(hosts);
        OperationLevel level = new OperationLevel();
        level.setLevel(OperationLevelEnum.HOST_COMPONENT.name());
        level.setClusterName(clusterName);
        request.requestInfo.setOperationLevel(level);
        request.requestInfo.setContext("Install Components");
        return request;
    }

    public RequestInfo getRequestInfo() {
        return requestInfo;
    }

    public void setRequestInfo(RequestInfo requestInfo) {
        this.requestInfo = requestInfo;
    }

}
