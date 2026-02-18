package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 查询主机上所有组件信息的请求
 * @author: wangda
 * @date: 2023/1/12
 */
public class QueryHostsComponentsRequest {
    @SerializedName("RequestInfo")
    private RequestInfo request;

    public static QueryHostsComponentsRequest of(List<String> hosts) {
        QueryHostsComponentsRequest req = new QueryHostsComponentsRequest();
        req.request = new RequestInfo();
        req.request.setQueryHostName(hosts);
        return req;
    }
}
