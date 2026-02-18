package sunbox.sdp.ambari.client.model.customaction;

import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @author: wangda
 * @date: 2023/1/9
 */
public class DecommissionHostComponentRequest {
    @SerializedName("RequestInfo")
    private RequestInfo request;

    @SerializedName("Requests/resource_filters")
    private List<Map<String, String>> resourceFilter;

    /**
     * 构造一个Decommission请求
     * @param clusterName 集群ID
     * @param hosts 停用组件的主机列表
     * @param componentName 需停用的组件
     * @return
     */
    public static DecommissionHostComponentRequest of(String clusterName, List<String> hosts, String componentName) {
        if (!Objects.equals("DATANODE", componentName)
                && !Objects.equals("NODEMANAGER", componentName)
                && !Objects.equals("HBASE_REGIONSERVER", componentName)
        ) {
            throw new RuntimeException("组件不支持Decommission，只有 DATANODE, NODEMANAGER 和 HBASE_REGIONSERVER 支持。传入组件名称：" + componentName);
        }
        DecommissionHostComponentRequest request = new DecommissionHostComponentRequest();
        // RequestInfo.parameters
        Map<String, String> parameter = new HashMap<>();
        parameter.put("slave_type", StringUtils.upperCase(componentName));
        parameter.put("excluded_hosts", StringUtils.join(hosts.toArray(), ","));

        OperationLevel level = new OperationLevel();
        level.setLevel(OperationLevelEnum.CLUSTER.name());
        level.setClusterName(clusterName);

        RequestInfo requestInfo = new RequestInfo();
        request.request = requestInfo;
        requestInfo.setContext("Decommission " + componentName);
        requestInfo.setCommand("DECOMMISSION");
        requestInfo.setParameters(parameter);
        requestInfo.setOperationLevel(level);

        Map<String, String> filters = new HashMap<>();
        if (Objects.equals("DATANODE", StringUtils.upperCase(componentName))) {
            filters.put("service_name", "HDFS");
            filters.put("component_name", "NAMENODE");
        } else if (Objects.equals("HBASE_REGIONSERVER", StringUtils.upperCase(componentName))) {
            filters.put("service_name", "HBASE");
            filters.put("component_name", "HBASE_MASTER");
            request.getRequest().setExclusive(true);
        } else {
            filters.put("service_name", "YARN");
            filters.put("component_name", "RESOURCEMANAGER");
        }

        List filterList = new ArrayList<>();
        filterList.add(filters);
        request.resourceFilter = filterList;

        return request;
    }


    /**
     * 构造一个Decommission请求
     * @param clusterName 集群ID
     * @param host 停用组件的主机列表
     * @param componentName 需停用的组件
     * @return
     */
    public static DecommissionHostComponentRequest of(String clusterName, String host, String componentName) {
        if (!Objects.equals("DATANODE", componentName)
                && !Objects.equals("NODEMANAGER", componentName)
                && !Objects.equals("HBASE_REGIONSERVER", componentName)
        ) {
            throw new RuntimeException("组件不支持Decommission，只有 DATANODE, NODEMANAGER 和 HBASE_REGIONSERVER 支持。传入组件名称：" + componentName);
        }
        DecommissionHostComponentRequest request = new DecommissionHostComponentRequest();
        // RequestInfo.parameters
        Map<String, String> parameter = new HashMap<>();
        parameter.put("slave_type", StringUtils.upperCase(componentName));
        parameter.put("excluded_hosts", host);

        OperationLevel level = new OperationLevel();
        level.setLevel(OperationLevelEnum.HOST_COMPONENT.name());
        level.setClusterName(clusterName);
        level.setHostName(host);


        RequestInfo requestInfo = new RequestInfo();
        request.request = requestInfo;
        requestInfo.setContext("Decommission " + componentName);
        requestInfo.setCommand("DECOMMISSION");
        requestInfo.setParameters(parameter);
        requestInfo.setOperationLevel(level);

        Map<String, String> filters = new HashMap<>();
        if (Objects.equals("DATANODE", StringUtils.upperCase(componentName))) {
            filters.put("service_name", "HDFS");
            filters.put("component_name", "NAMENODE");
        } else if (Objects.equals("HBASE_REGIONSERVER", StringUtils.upperCase(componentName))) {
            filters.put("service_name", "HBASE");
            filters.put("component_name", "HBASE_MASTER");
            request.getRequest().setExclusive(true);
        } else {
            filters.put("service_name", "YARN");
            filters.put("component_name", "RESOURCEMANAGER");
            level.setServiceName("YARN");
        }

        List filterList = new ArrayList<>();
        filterList.add(filters);
        request.resourceFilter = filterList;

        return request;
    }

    public RequestInfo getRequest() {
        return request;
    }

    public void setRequest(RequestInfo request) {
        this.request = request;
    }

    public List<Map<String, String>> getResourceFilter() {
        return resourceFilter;
    }

    public void setResourceFilter(List<Map<String, String>> resourceFilter) {
        this.resourceFilter = resourceFilter;
    }

    /**
     * 增加一个资源过滤器(ResourceFilter).<br/>
     * 包含两个字段: <br/>
     * service_name -> "HDFS"  <br/>
     * component_name  -> "NAMENODE"
     * @param filter 资源过滤器
     * @return
     */
    public DecommissionHostComponentRequest addFilter(Map<String, String> filter) {
        if (Objects.isNull(resourceFilter)) {
            this.resourceFilter = new ArrayList<>();
        }
        if (Objects.isNull(filter)) {
            return this;
        }
        this.resourceFilter.add(filter);
        return this;
    }
}
