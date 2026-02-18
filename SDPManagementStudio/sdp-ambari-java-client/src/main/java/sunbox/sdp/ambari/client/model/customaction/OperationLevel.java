package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;

/**
 * @author: wangda
 * @date: 2023/1/9
 */
public class OperationLevel {

    @SerializedName("level")
    private String level;

    @SerializedName("cluster_name")
    private String clusterName;

    @SerializedName("service_name")
    private String serviceName;

    @SerializedName("host_name")
    private String hostName;

    /**
     * 构建一个OperationLevel实例
     * @param level 操作级别
     * @param clusterName 集群名
     * @return
     */
    public static OperationLevel of(OperationLevelEnum level, String clusterName) {
        return of(level, clusterName, null);
    }

    public static OperationLevel of(OperationLevelEnum level, String clusterName, String serviceName) {
        OperationLevel operLevel = new OperationLevel();
        operLevel.level = level.name();
        operLevel.clusterName = clusterName;
        operLevel.serviceName = serviceName;
        return operLevel;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }
}
