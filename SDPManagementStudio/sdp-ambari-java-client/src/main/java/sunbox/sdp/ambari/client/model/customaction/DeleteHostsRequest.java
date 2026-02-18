package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author: wangda
 * @date: 2023/1/12
 */
public class DeleteHostsRequest {

    @SerializedName("RequestInfo")
    private RequestInfo requestInfo;

    public static DeleteHostsRequest of(List<String> hosts) {
        DeleteHostsRequest request = new DeleteHostsRequest();
        request.requestInfo = new RequestInfo();
        request.requestInfo.setQueryHostName(hosts);
        return request;
    }

    public RequestInfo getRequestInfo() {
        return requestInfo;
    }

    public void setRequestInfo(RequestInfo requestInfo) {
        this.requestInfo = requestInfo;
    }
}
