package sunbox.sdp.ambari.client.model.customaction;

import com.alibaba.fastjson.JSON;
import org.apache.logging.log4j.util.Strings;

import java.util.List;
import java.util.Map;

/**
 * @author: wangda
 * @date: 2023/2/26
 */
public class RegionServerDrainInfo {
    /** 任务执行的序号ID */
    private Integer orderId;

    /** 集群名 */
    private String clusterName;

    /** 主机名列表 */
    private List<String> hosts;

    public static RegionServerDrainInfo of(Integer orderId, String clusterName, List<String> hosts) {
        RegionServerDrainInfo info = new RegionServerDrainInfo();
        info.setOrderId(orderId);
        info.setClusterName(clusterName);
        info.setHosts(hosts);
        return info;
    }

    public Map<String, Object> toDrainOnMap() {
        String joinedHost = Strings.join(hosts, ',');

        String template = "{\n" +
                "    \"order_id\": {{orderId}},\n" +
                "    \"type\": \"POST\",\n" +
                "    \"uri\": \"/clusters/{{clusterName}}/requests\",\n" +
                "    \"RequestBodyInfo\": {\n" +
                "        \"RequestInfo\": {\n" +
                "            \"context\": \"Decommission RegionServer - Turn drain mode on \",\n" +
                "            \"command\": \"DECOMMISSION\",\n" +
                "            \"exclusive\": \"true\",\n" +
                "            \"parameters\": {\n" +
                "                \"slave_type\": \"HBASE_REGIONSERVER\",\n" +
                "                \"excluded_hosts\": \"{{hosts}}\"\n" +
                "            },\n" +
                "            \"operation_level\": {\n" +
                "                \"level\": \"CLUSTER\",\n" +
                "                \"cluster_name\": \"{{clusterName}}\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"Requests/resource_filters\": [\n" +
                "            {\n" +
                "                \"service_name\": \"HBASE\",\n" +
                "                \"component_name\": \"HBASE_MASTER\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}";
        // 替换变量
        template = template.replace("{{orderId}}", String.valueOf(orderId));
        template = template.replace("{{clusterName}}", String.valueOf(clusterName));
        template = template.replace("{{hosts}}", String.valueOf(joinedHost));

        return JSON.parseObject(template, Map.class);
    }

    public Map<String, Object> toDrainOffMap() {
        String joinedHost = Strings.join(hosts, ',');

        String template = "{\n" +
                "    \"order_id\": {{orderId}},\n" +
                "    \"type\": \"POST\",\n" +
                "    \"uri\": \"/clusters/{{clusterName}}/requests\",\n" +
                "    \"RequestBodyInfo\": {\n" +
                "        \"RequestInfo\": {\n" +
                "            \"context\": \"Decommission RegionServer - Turn drain mode off \",\n" +
                "            \"command\": \"DECOMMISSION\",\n" +
                "            \"service_name\": \"HBASE\",\n" +
                "            \"component_name\": \"HBASE_MASTER\",\n" +
                "            \"parameters\": {\n" +
                "                \"slave_type\": \"HBASE_REGIONSERVER\",\n" +
                "                \"excluded_hosts\": \"{{hosts}}\",\n" +
                "                \"mark_draining_only\": true\n" +
                "            },\n" +
                "            \"operation_level\": {\n" +
                "                \"level\": \"CLUSTER\",\n" +
                "                \"cluster_name\": \"{{clusterName}}\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"Requests/resource_filters\": [\n" +
                "            {\n" +
                "                \"service_name\": \"HBASE\",\n" +
                "                \"component_name\": \"HBASE_MASTER\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}";
        // 替换变量
        template = template.replace("{{orderId}}", String.valueOf(orderId));
        template = template.replace("{{clusterName}}", String.valueOf(clusterName));
        template = template.replace("{{hosts}}", String.valueOf(joinedHost));

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

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }
}
