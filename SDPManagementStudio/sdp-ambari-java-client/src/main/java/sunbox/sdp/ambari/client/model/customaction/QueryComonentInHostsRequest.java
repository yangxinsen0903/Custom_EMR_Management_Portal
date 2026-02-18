package sunbox.sdp.ambari.client.model.customaction;

import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * @author: wangda
 * @date: 2023/1/14
 */
public class QueryComonentInHostsRequest {

    private List<String> components;

    public static QueryComonentInHostsRequest of(List<String> components) {
        QueryComonentInHostsRequest request = new QueryComonentInHostsRequest();
        request.components = components;
        return request;
    }

    public String generateQueryString() {
        if (Objects.isNull(components)) {
            return "";
        }

        StringBuilder param = new StringBuilder();
        for (String component : components) {
            if (param.length() > 0) {
                param.append("|");
            }
            param.append("ServiceComponentInfo/component_name=")
                    .append(component);
        }

        param.append("&fields=ServiceComponentInfo/service_name,")
                .append("host_components/HostRoles/display_name,")
                .append("host_components/HostRoles/host_name,")
                .append("host_components/HostRoles/state,")
                .append("host_components/HostRoles/ip,")
                .append("host_components/metrics/dfs/FSNamesystem/HAState,");

        param.append("&minimal_response=true")
                .append("&_=" + new Random().nextInt());

        return param.toString();
    }

    public List<String> getComponents() {
        return components;
    }

    public void setComponents(List<String> components) {
        this.components = components;
    }
}
