package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: wangda
 * @date: 2023/1/14
 */
public class RebalanceRequest {
    @SerializedName("RequestInfo")
    private RequestInfo requestInfo;

    @SerializedName("Requests/resource_filters")
    private List<RequestResourceFilter> filter;

    public static RebalanceRequest of(String hostname, int threshold) {
        RebalanceRequest request = new RebalanceRequest();
        request.requestInfo = new RequestInfo();
        request.filter = new ArrayList<>();
        RequestResourceFilter filter = new RequestResourceFilter();
        request.filter.add(filter);

        request.requestInfo.setContext("Rebalance HDFS");
        request.requestInfo.setCommand("REBALANCEHDFS");
        request.requestInfo.setNameNode("{\"threshold\":\"" +threshold + "\"}");

        filter.setHosts(hostname);
        filter.setComponentName("NAMENODE");
        filter.setServiceName("HDFS");

        return request;
    }
}
