package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * @author: wangda
 * @date: 2023/1/2
 */
public class RequestInfo {
    /** 上下文，名称 */
    @SerializedName("context")
    private String context;

    @SerializedName("command")
    private String command;

    /** 操作级别 */
    @SerializedName("operation_level")
    private OperationLevel operationLevel;

    @SerializedName("parameters")
    private Map<String, String> parameters;

    @SerializedName("namenode")
    private String nameNode;

    /** 查询条件 */
    @SerializedName("query")
    private String query;

    @SerializedName("exclusive")
    private Boolean exclusive;


    public void setQueryComponentName(List<String> components) {
        StringBuilder sb = new StringBuilder();
        sb.append("ServiceComponentInfo/component_name.in(");

        String joinHosts = StringUtils.join(components.toArray(), ",");
        sb.append(joinHosts).append(")");
        query = sb.toString();
    }

    public void setQueryHostName(List<String> hosts) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hosts/host_name.in(");

        String joinHosts = StringUtils.join(hosts.toArray(), ",");
        sb.append(joinHosts).append(")");
        query = sb.toString();
    }

    public void setQueryHostRoleHostName(List<String> hosts) {
        StringBuilder sb = new StringBuilder();
        sb.append("HostRoles/host_name.in(");

        String joinHosts = StringUtils.join(hosts.toArray(), ",");
        sb.append(joinHosts).append(")");
        query = sb.toString();
    }

    public void setQueryHostComponentAndHostAndState(List<String> hosts, List<String> components, String state) {
        String joinComponent = StringUtils.join(components, ",");
        String joinHost = StringUtils.join(hosts, ",");

        StringBuilder sb = new StringBuilder();
        sb.append("HostRoles/component_name.in(").append(joinComponent).append(")&HostRoles/state=").append(state)
                .append("&HostRoles/host_name.in(").append(joinHost).append(")");
        query = sb.toString();
    }

    public Boolean getExclusive() {
        return exclusive;
    }

    public void setExclusive(Boolean exclusive) {
        this.exclusive = exclusive;
    }

    public String getQuery() {
        return query;
    }

    public RequestInfo setQuery(String query) {
        this.query = query;
        return this;
    }

    public String getContext() {
        return context;
    }

    public RequestInfo setContext(String context) {
        this.context = context;
        return this;
    }

    public OperationLevel getOperationLevel() {
        return operationLevel;
    }

    public RequestInfo setOperationLevel(OperationLevel operationLevel) {
        this.operationLevel = operationLevel;
        return this;
    }

    public String getCommand() {
        return command;
    }

    public RequestInfo setCommand(String command) {
        this.command = command;
        return this;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public RequestInfo setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
        return this;
    }

    public String getNameNode() {
        return nameNode;
    }

    public void setNameNode(String nameNode) {
        this.nameNode = nameNode;
    }
}
