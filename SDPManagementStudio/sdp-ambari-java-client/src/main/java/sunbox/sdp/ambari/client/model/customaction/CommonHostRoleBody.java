package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

/**
 * @author: wangda
 * @date: 2023/1/8
 */
public class CommonHostRoleBody {

    @SerializedName("HostRoles")
    private HostRole hostRole;

    public static CommonHostRoleBody of(String state) {
        CommonHostRoleBody body = new CommonHostRoleBody();
        body.hostRole = new HostRole();
        body.hostRole.setState(state);
        return body;
    }

    public CommonHostRoleBody() {
        hostRole = new HostRole();
        hostRole.setState("INSTALLED");
    }

    public HostRole getHostRole() {
        return hostRole;
    }

    public void setHostRole(HostRole hostRole) {
        this.hostRole = hostRole;
    }
}
