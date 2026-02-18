package sunbox.sdp.ambari.client.model.customaction;

import com.alibaba.fastjson.JSON;

import java.util.Map;

/**
 * RegionServer在Decommission时, 关闭RegionServer的信息
 * @author: wangda
 * @date: 2023/2/26
 */
public class RegionServerStopInfo {
    /** 任务执行的序号ID */
    private Integer orderId;

    /** 集群名 */
    private String clusterName;

    /** 主机名列表 */
    private String host;

    public static RegionServerStopInfo of(Integer orderId, String clusterName, String host) {
        RegionServerStopInfo info = new RegionServerStopInfo();
        info.setHost(host);
        info.setClusterName(clusterName);
        info.setOrderId(orderId);
        return info;
    }

    public Map<String, Object> toStopMap() {

        String template = "{\n" +
                "    \"order_id\": {{orderId}},\n" +
                "    \"type\": \"PUT\",\n" +
                "    \"uri\": \"/clusters/{{clusterName}}/hosts/{{host}}/host_components/HBASE_REGIONSERVER\",\n" +
                "    \"RequestBodyInfo\": {\n" +
                "        \"RequestInfo\": {\n" +
                "            \"context\": \"Decommission RegionServer - Stop RegionServer: {{host}}\",\n" +
                "            \"exclusive\": true,\n" +
                "            \"operation_level\": {\n" +
                "                \"level\": \"HOST_COMPONENT\",\n" +
                "                \"cluster_name\": \"{{clusterName}}\",\n" +
                "                \"host_name\": \"{{host}}\",\n" +
                "                \"service_name\": \"HBASE\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"Body\": {\n" +
                "            \"HostRoles\": {\n" +
                "                \"state\": \"INSTALLED\"\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";
        // 替换变量
        template = template.replace("{{orderId}}", String.valueOf(orderId));
        template = template.replace("{{clusterName}}", clusterName);
        template = template.replace("{{host}}", host);

        return JSON.parseObject(template, Map.class);
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
