package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 更新配置组响应对象
 * @author: wangda
 * @date: 2023/2/11
 */
public class UpdateConfigGroupResponse {

    @SerializedName("resources")
    private List<ConfigGroupWrapper> resources;


    public List<ConfigGroupWrapper> getResources() {
        return resources;
    }

    public void setResources(List<ConfigGroupWrapper> resources) {
        this.resources = resources;
    }

    @Override
    public String toString() {
        return "UpdateConfigGroupResponse{" +
                "resources=" + resources +
                '}';
    }
}
