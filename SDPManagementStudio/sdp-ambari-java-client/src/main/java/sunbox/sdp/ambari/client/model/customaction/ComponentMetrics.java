package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

/**
 * @author: wangda
 * @date: 2023/1/14
 */
public class ComponentMetrics {

    @SerializedName("dfs")
    private Dfs dfs;

    public Dfs getDfs() {
        return dfs;
    }

    public void setDfs(Dfs dfs) {
        this.dfs = dfs;
    }
}
