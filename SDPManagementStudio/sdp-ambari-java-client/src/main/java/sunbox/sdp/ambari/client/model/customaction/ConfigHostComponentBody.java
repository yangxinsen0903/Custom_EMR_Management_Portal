package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: wangda
 * @date: 2023/1/8
 */
public class ConfigHostComponentBody {

    @SerializedName("host_components")
    private List<Map<String, HostRole>> hostComponents = new ArrayList<>();

    /**
     * 增加组件
     * @param components
     */
    public void addComponent(List<String> components) {
        for (String component : components) {
            HostRole hostRole = HostRole.of(component, null);
            Map<String, HostRole> hostRoleMap = new HashMap<>();
            hostRoleMap.put("HostRoles", hostRole);
            hostComponents.add(hostRoleMap);
        }
    }

    public List<Map<String, HostRole>> getHostComponents() {
        return hostComponents;
    }

    public void setHostComponents(List<Map<String, HostRole>> hostComponents) {
        this.hostComponents = hostComponents;
    }
}
