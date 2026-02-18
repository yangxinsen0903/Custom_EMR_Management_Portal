package sunbox.sdp.ambari.client.model.createclusterprocess;

import com.google.gson.annotations.SerializedName;
import sunbox.sdp.ambari.client.model.RequestInfo;

/**
 * 格式化节点时RequestInfo，多了一个字段
 * @author: wangda
 * @date: 2022/12/14
 */
public class FormatRequestInfo extends RequestInfo {

    @SerializedName("dfs_ha_namenode_active")
    private String dfsHaNamenodeActive = "b";

    public String getDfsHaNamenodeActive() {
        return dfsHaNamenodeActive;
    }

    public void setDfsHaNamenodeActive(String dfsHaNamenodeActive) {
        this.dfsHaNamenodeActive = dfsHaNamenodeActive;
    }
}
