package sunbox.sdp.ambari.client.model;

import com.google.gson.annotations.SerializedName;

/**
 * 控制集群中服务状态的请求对象：启动， 关闭，重启
 * @author: wangda
 * @date: 2022/12/7
 */
public class ClusterServiceStateRequest {

    @SerializedName("RequestInfo")
    private RequestInfo requestInfo;

    @SerializedName("Body")
    private ClusterServiceStateBody body;

    /**
     * 构造一个控制服务的请求对象：启动。关闭
     * @param clusterName 集群
     * @param serviceName 服务名，如：YARN，HADOOP
     * @param op 操作类型
     * @return
     */
    public static ClusterServiceStateRequest buildSerivceRequest(String clusterName, String serviceName, ServiceOp op) {
        ClusterServiceStateRequest request = new ClusterServiceStateRequest();


        // RequestInfo
        RequestInfo info = new RequestInfo();
        info.setContext("_PARSE_."+op.name()+"." + serviceName);
        OperationLevel level = new OperationLevel();
        level.setLevel("SERVICE");
        level.setClusterName(clusterName);
        level.setServiceName(serviceName);
        info.setOperationLevel(level);

        // Body
        ClusterServiceStateBody body = new ClusterServiceStateBody();
        ServiceRequest serviceInfo = new ServiceRequest();
        serviceInfo.setState(op.getState());
        body.setServiceInfo(serviceInfo);

        request.setRequestInfo(info);
        request.setBody(body);

        return request;
    }

    public static ClusterServiceStateRequest buildStartAllSerivceRequest(String clusterName, ServiceOp op) {
        ClusterServiceStateRequest request = new ClusterServiceStateRequest();


        // RequestInfo
        RequestInfo info = new RequestInfo();
        info.setContext("_PARSE_."+op.name()+".ALL_SERVICES");
        OperationLevel level = new OperationLevel();
        level.setLevel("CLUSTER");
        level.setClusterName(clusterName);
        info.setOperationLevel(level);

        // Body
        ClusterServiceStateBody body = new ClusterServiceStateBody();
        ServiceRequest serviceInfo = new ServiceRequest();
        serviceInfo.setState(op.getState());
        body.setServiceInfo(serviceInfo);

        request.setRequestInfo(info);
        request.setBody(body);

        return request;
    }

    public RequestInfo getRequestInfo() {
        return requestInfo;
    }

    public void setRequestInfo(RequestInfo requestInfo) {
        this.requestInfo = requestInfo;
    }

    public ClusterServiceStateBody getBody() {
        return body;
    }

    public void setBody(ClusterServiceStateBody body) {
        this.body = body;
    }
}
