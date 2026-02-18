package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

/**
 * 关闭所有服务的请求对象
 * @author: wangda
 * @date: 2023/1/17
 */
public class StopAllServiceRequest {

    /**
     * 创建一个关闭所有服务的请求对象
     * @param clusterName 集群名
     * @return
     */
    public static StopAllServiceRequest of(String clusterName) {
        StopAllServiceRequest request = new StopAllServiceRequest();
        request.requestInfo = new RequestInfo();
        request.requestInfo.setContext("_PARSE_.STOP.ALL_SERVICES");
        request.requestInfo.setOperationLevel(new OperationLevel());
        request.requestInfo.getOperationLevel().setLevel(OperationLevelEnum.CLUSTER.name());
        request.requestInfo.getOperationLevel().setClusterName(clusterName);

        request.body = new StopAllServiceBody();
        request.body.setServiceInfo(new ServiceInfo());
        request.body.getServiceInfo().setState("INSTALLED");

        return  request;
    }
    @SerializedName("RequestInfo")
    private RequestInfo requestInfo;

    @SerializedName("Body")
    private StopAllServiceBody body;

    public RequestInfo getRequestInfo() {
        return requestInfo;
    }

    public void setRequestInfo(RequestInfo requestInfo) {
        this.requestInfo = requestInfo;
    }

    public StopAllServiceBody getBody() {
        return body;
    }

    public void setBody(StopAllServiceBody body) {
        this.body = body;
    }
}
