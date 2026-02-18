package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

/**
 * @author: wangda
 * @date: 2023/1/8
 */
public class HostRole {

    @SerializedName("href")
    private String href;

    @SerializedName("component_name")
    private String componentName;

    @SerializedName("state")
    private String state;

    @SerializedName("desired_admin_state")
    private String desiredAdminState;

    @SerializedName("display_name")
    private String displayName;

    @SerializedName("maintenance_state")
    private String maintenanceState;

    @SerializedName("service_name")
    private String serviceName;

    @SerializedName("stale_configs")
    private Boolean staleConfigs;

    @SerializedName("host_name")
    private String hostName;

    @SerializedName("ha_state")
    private String ha_state;

    public static HostRole of(String componentName, String state) {
        HostRole hostRole = new HostRole();
        hostRole.setComponentName(componentName);
        hostRole.setState(state);
        return hostRole;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDesiredAdminState() {
        return desiredAdminState;
    }

    public void setDesiredAdminState(String desiredAdminState) {
        this.desiredAdminState = desiredAdminState;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getMaintenanceState() {
        return maintenanceState;
    }

    public void setMaintenanceState(String maintenanceState) {
        this.maintenanceState = maintenanceState;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Boolean getStaleConfigs() {
        return staleConfigs;
    }

    public void setStaleConfigs(Boolean staleConfigs) {
        this.staleConfigs = staleConfigs;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHa_state() {
        return ha_state;
    }

    public void setHa_state(String ha_state) {
        this.ha_state = ha_state;
    }

    @Override
    public String toString() {
        return "HostRole{" +
                "href='" + href + '\'' +
                ", componentName='" + componentName + '\'' +
                ", state='" + state + '\'' +
                ", desiredAdminState='" + desiredAdminState + '\'' +
                ", displayName='" + displayName + '\'' +
                ", maintenanceState='" + maintenanceState + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", staleConfigs=" + staleConfigs +
                ", hostName='" + hostName + '\'' +
                ", ha_state='" + ha_state + '\'' +
                '}';
    }
}
