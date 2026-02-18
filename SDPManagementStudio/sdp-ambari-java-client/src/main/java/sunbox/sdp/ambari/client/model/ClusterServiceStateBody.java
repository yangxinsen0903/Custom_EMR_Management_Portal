package sunbox.sdp.ambari.client.model;

import com.google.gson.annotations.SerializedName;

/**
 * 请求Body
 * @author: wangda
 * @date: 2022/12/7
 */
public class ClusterServiceStateBody {

    @SerializedName("ServiceInfo")
    private ServiceRequest serviceInfo;

    public ServiceRequest getServiceInfo() {
        return serviceInfo;
    }

    public void setServiceInfo(ServiceRequest serviceInfo) {
        this.serviceInfo = serviceInfo;
    }
}
