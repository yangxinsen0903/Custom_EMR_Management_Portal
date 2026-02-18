package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

/**
 * @author: wangda
 * @date: 2023/1/2
 */
public class ServiceComponentInfo {

    @SerializedName("recovery_enabled")
    private String recoveryEnabled;

    @SerializedName("component_name")
    private String componentName;

    @SerializedName("service_name")
    private String serviceName;

    /** 设置为有效 */
    public void enableRecoveryEnabled() {
        this.recoveryEnabled = "true";
    }

    /** 设置为无效 */
    public void disableRecoveryEnabled() {
        this.recoveryEnabled = "false";
    }

    public String getRecoveryEnabled() {
        return recoveryEnabled;
    }

    public void setRecoveryEnabled(String recoveryEnabled) {
        this.recoveryEnabled = recoveryEnabled;
    }

    public void setRecoveryEnabled(boolean recoveryEnabled) {
        this.recoveryEnabled = String.valueOf(recoveryEnabled);
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
