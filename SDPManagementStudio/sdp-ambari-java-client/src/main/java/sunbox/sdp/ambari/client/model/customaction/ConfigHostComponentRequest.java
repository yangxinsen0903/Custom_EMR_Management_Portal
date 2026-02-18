package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author: wangda
 * @date: 2023/1/8
 */
public class ConfigHostComponentRequest {

    @SerializedName("RequestInfo")
    private RequestInfo requestInfo = new RequestInfo();


    @SerializedName("Body")
    private ConfigHostComponentBody  body = new ConfigHostComponentBody();

    public static ConfigHostComponentRequest of(String clusterName, List<String> hosts, List<String> components) {
        ConfigHostComponentRequest request = new ConfigHostComponentRequest();
        request.requestInfo.setQueryHostName(hosts);
        request.body.addComponent(components);
        return request;
    }

    public RequestInfo getRequestInfo() {
        return requestInfo;
    }

    public void setRequestInfo(RequestInfo requestInfo) {
        this.requestInfo = requestInfo;
    }

    public ConfigHostComponentBody getBody() {
        return body;
    }

    public void setBody(ConfigHostComponentBody body) {
        this.body = body;
    }
}
