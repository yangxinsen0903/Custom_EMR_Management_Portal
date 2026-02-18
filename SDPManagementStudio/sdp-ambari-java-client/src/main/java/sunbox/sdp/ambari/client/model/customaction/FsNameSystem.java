package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

/**
 * @author: wangda
 * @date: 2023/1/14
 */
public class FsNameSystem {

    @SerializedName("HAState")
    private String HaState;

    public String getHaState() {
        return HaState;
    }

    public void setHaState(String haState) {
        HaState = haState;
    }
}
