package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

/**
 * @author: wangda
 * @date: 2023/1/8
 */
public class Hosts {

    @SerializedName("host_name")
    private String hostName;

    @SerializedName("host_state")
    private String hostState;

    @SerializedName("host_status")
    private String hostStatus;

    public static Hosts of(String hostName) {
        Hosts host = new Hosts();
        host.setHostName(hostName);
        return host;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHostState() {
        return hostState;
    }

    public String getHostStatus() {
        return hostStatus;
    }

    public void setHostStatus(String hostStatus) {
        this.hostStatus = hostStatus;
    }

    public void setHostState(String hostState) {
        this.hostState = hostState;
    }
}
