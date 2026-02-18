package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 组件自启动请求对象
 * @author: wangda
 * @date: 2023/1/2
 */
public class ComponentAutoStartRequest {
    /** 请求信息 */
    @SerializedName("RequestInfo")
    private RequestInfo requestInfo = new RequestInfo();

    /** 服务 */
    @SerializedName("ServiceComponentInfo")
    private ServiceComponentInfo serviceComponentInfo = new ServiceComponentInfo();

    /**
     * 是否自动运行
     * @param enableAutoStart true:是； false: 否
     * @return
     */
    public ComponentAutoStartRequest setEnableAutoStart(boolean enableAutoStart) {
        this.serviceComponentInfo.setRecoveryEnabled(enableAutoStart);
        return this;
    }

    /**
     * 需要进行控制的组件
     * @param components 组件列表
     * @return
     */
    public ComponentAutoStartRequest setComponents(List<String> components) {
        this.requestInfo.setQueryComponentName(components);
        return this;
    }

}
