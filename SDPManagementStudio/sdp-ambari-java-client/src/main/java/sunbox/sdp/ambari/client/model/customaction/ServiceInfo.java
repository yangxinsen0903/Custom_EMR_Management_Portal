package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

/**
 * Service信息，尽量作为一个通用的Model
 * @author: wangda
 * @date: 2023/1/17
 */
public class ServiceInfo {

    /** 状态 */
    @SerializedName("state")
    private String state;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
