package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

/**
 * @author: wangda
 * @date: 2023/1/17
 */
public class StopAllServiceBody {

    @SerializedName("ServiceInfo")
    private ServiceInfo serviceInfo;

    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }
}
