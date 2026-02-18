package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 更新集群配置的响应对象
 * @author: wangda
 * @date: 2023/2/15
 */
public class UpdateClusterDefaultConfigResponse {

    @SerializedName("resources")
    private List<UpdatedDefaultConfigWrapper> resources;

    public List<UpdatedDefaultConfigWrapper> getResources() {
        return resources;
    }

    public void setResources(List<UpdatedDefaultConfigWrapper> resources) {
        this.resources = resources;
    }
}
