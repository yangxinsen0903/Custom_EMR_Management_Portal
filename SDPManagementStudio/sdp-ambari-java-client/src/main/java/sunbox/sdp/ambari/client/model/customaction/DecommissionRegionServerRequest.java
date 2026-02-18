package sunbox.sdp.ambari.client.model.customaction;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Decommission RegionServer的请求对象
 * @author: wangda
 * @date: 2023/2/26
 */
public class DecommissionRegionServerRequest {

    /** 集群名称 */
    private String clusterName;

    /** 主机名列表 */
    private List<String> hosts;

    /**
     * 构造一个请求对象
     * @param clusterName
     * @param hosts
     * @return
     */
    public static DecommissionRegionServerRequest of(String clusterName, List<String> hosts) {
        DecommissionRegionServerRequest request = new DecommissionRegionServerRequest();
        request.setClusterName(clusterName);
        request.setHosts(hosts);
        return request;
    }

    /**
     * 转为可以请求Ambari的的Map对象.
     * 结构为:
     * [ // 最外层是数组
     *     { // 数组里有一个对象
     *         "RequestSchedule": { // 对象里只有一个属性: RequestSchedule
     *              "batch": [  // RequestSchedule 对象里面只有一个属性: batch
     *                  {
     *                      "requests": [
     *                          {
     *                              requests里面的对象是真正执行的任务.
     *                          }
     *                      ]
     *                  },
     *                  {
     *                      "batch_settings": {
     *                          // batch_settings 有两个
     *                      }
     *                  }
     *              ]
     *         }
     *     }
     * ]
     * @return
     */
    public List<Map> toRequestMap() {
        List<Map> resutl = new ArrayList<>();
        List<Map> requests = buildRequestMap();

        Map<String, Object> batchSettingMap = new HashMap<>();
        batchSettingMap.put("batch_settings", buildBatchSettingMap());

        Map<String, Object> requestsMap = new HashMap<>();
        requestsMap.put("requests", buildRequestMap());

        List batchList = Arrays.asList(requestsMap, batchSettingMap);

        Map batchMap = new HashMap();
        batchMap.put("batch", batchList);

        Map requestScheduleMap = new HashMap();
        requestScheduleMap.put("RequestSchedule", batchMap);

        resutl.add(requestScheduleMap);
        return resutl;
    }

    @NotNull
    private List<Map> buildRequestMap() {
        List<Map> requests = new ArrayList<>();
        int orderId = 0;
        // 生成成请求的任务对象
        RegionServerDrainInfo drainInfo = RegionServerDrainInfo.of(orderId++, clusterName, hosts);
        requests.add(drainInfo.toDrainOnMap());
        for (String host : hosts) {
            RegionServerStopInfo stopInfo = RegionServerStopInfo.of(orderId++, clusterName, host);
            requests.add(stopInfo.toStopMap());
        }
        drainInfo = RegionServerDrainInfo.of(orderId++, clusterName, hosts);
        requests.add(drainInfo.toDrainOffMap());
        return requests;
    }

    private Map buildBatchSettingMap() {
        Map map = new HashMap();
        map.put("batch_separation_in_seconds", 1);
        map.put("task_failure_tolerance", 0);
        return map;
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
