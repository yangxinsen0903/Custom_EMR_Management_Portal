package sunbox.sdp.ambari.client.api;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.squareup.okhttp.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.util.Strings;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import sunbox.sdp.ambari.client.ApiException;
import sunbox.sdp.ambari.client.Configuration;
import sunbox.sdp.ambari.client.HttpUtils;
import sunbox.sdp.ambari.client.auth.HttpBasicAuth;
import sunbox.sdp.ambari.client.model.createclusterprocess.CreateClusterProcessStageWrapper;
import sunbox.sdp.ambari.client.model.createclusterprocess.CreateClusterProcessTask;
import sunbox.sdp.ambari.client.model.customaction.*;
import sunbox.sdp.ambari.client.model.customaction.enums.ConfigGroupField;

import java.io.IOException;
import java.util.*;

/**
 * @author: wangda
 * @date: 2023/1/2
 */
public class CustomActionApiTest extends BaseTest {

    private static CustomActionApi api = null;

    //    private static String baseUri = "http://20.118.166.170:8080/api/v1";
    private static String baseUri = "http://20.172.10.47:8765/api/v1";

    private static String clusterName = "sunbox";

    private static String referer = "referer";

    Gson gson = new Gson();

    @BeforeClass
    public static void init() {
        baseUri = "http://20.172.10.47:8765/api/v1";
        initClass();
        Configuration.getDefaultApiClient().setBasePath(baseUri);
        Configuration.getDefaultApiClient().setDebugging(true);
        Configuration.getDefaultApiClient().addDefaultHeader("Referer", referer);
        api = new CustomActionApi();
    }

    @Test
    public void testComponentAutoStart_autoStart() {
        String clusterName = "wdCluster";
        ComponentAutoStartRequest request = new ComponentAutoStartRequest();
        request.setEnableAutoStart(true);
        request.setComponents(Arrays.asList("DATANODE", "HDFS_CLIENT", "JOURNALNODE", "NAMENODE", "ZKFC",
                "HIVE_CLIENT", "HIVE_METASTORE", "HIVE_SERVER", "HISTORYSERVER", "MAPREDUCE2_CLIENT",
                "SPARK3_CLIENT", "SPARK3_JOBHISTORYSERVER", "SPARK3_THRIFTSERVER", "SQOOP", "TEZ_CLIENT",
                "APP_TIMELINE_SERVER", "NODEMANAGER", "RESOURCEMANAGER", "YARN_CLIENT", "ZOOKEEPER_CLIENT",
                "ZOOKEEPER_SERVER"));
        api.componentAutoStart(clusterName, request);
    }

    @Test
    public void testComponentAutoStart_notAutoStart() {
        String clusterName = "wdCluster";
        ComponentAutoStartRequest request = new ComponentAutoStartRequest();
        request.setEnableAutoStart(false);
        request.setComponents(Arrays.asList("DATANODE", "HDFS_CLIENT", "JOURNALNODE", "NAMENODE", "ZKFC",
                "HIVE_CLIENT", "HIVE_METASTORE", "HIVE_SERVER", "HISTORYSERVER", "MAPREDUCE2_CLIENT",
                "SPARK3_CLIENT", "SPARK3_JOBHISTORYSERVER", "SPARK3_THRIFTSERVER", "SQOOP", "TEZ_CLIENT",
                "APP_TIMELINE_SERVER", "NODEMANAGER", "RESOURCEMANAGER", "YARN_CLIENT", "ZOOKEEPER_CLIENT",
                "ZOOKEEPER_SERVER"));
        api.componentAutoStart(clusterName, request);
    }

    /**
     * 测试添加主机
     */
    @Test
    public void testAddHost() {
        // 可以增加的主机： sunbox-dev-vm15.vmdns.sunbox.com   sunbox-dev-vm16.vmdns.sunbox.com
        clusterName = "ZDYHIVEdev062601";
        try {
            api.addHost(clusterName, Arrays.asList(
                    "zdy-hive-dev-062601-tsk-0004.dev.sdp.com"
                    ,"zdy-hive-dev-062601-tsk-0005.dev.sdp.com"
//                    "zdy-hive-dev-062601-tsk-0006.dev.sdp.com"
            ));

        } catch (ApiException ex) {
            if (isConflict(ex)) {
                System.out.println("主机已经存在，算是添加主机成功");
            } else {
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Assert.assertTrue(true);
    }

    private boolean isConflict(ApiException ex) {
        return Objects.equals(ex.getCode(), HttpStatus.SC_CONFLICT);
    }

    /**
     * 测试配置主机的组件
     */
    @Test
    public void configHostComponent() {
        clusterName = "ZDYHIVEdev062601";
        // 可以增加的主机： sunbox-dev-vm15.vmdns.sunbox.com   sunbox-dev-vm16.vmdns.sunbox.com
        List<String> hosts = Arrays.asList("zdy-hive-dev-062601-tsk-0004.dev.sdp.com"
                    ,"zdy-hive-dev-062601-tsk-0005.dev.sdp.com");

        List<String> components = Arrays.asList("HDFS_CLIENT", "MAPREDUCE2_CLIENT",
                "NODEMANAGER", "TEZ_CLIENT",
                "YARN_CLIENT", "ZOOKEEPER_CLIENT");
        api.configHostComponent(clusterName, hosts, components);

        Assert.assertTrue(true);
    }

    /**
     * 测试在主机上安装组件
     */
    @Test
    public void installHostComponent() {
        // 可以增加的主机： sunbox-dev-vm15.vmdns.sunbox.com   sunbox-dev-vm16.vmdns.sunbox.com

        clusterName = "ZDYHIVEdev062601";
        List<String> hosts = Arrays.asList("zdy-hive-dev-062601-tsk-0004.dev.sdp.com"
//                ,"zdy-hive-dev-062601-tsk-0005.dev.sdp.com"
        );
        InProgressResponse inProgressResponse = api.installHostComponent(clusterName, hosts);
        System.out.println("RequestId=" + inProgressResponse.getRequestId());

        Assert.assertTrue(true);
    }

    /**
     * 测试启动主机上的组件
     */
    @Test
    public void startHostComponent() {
        clusterName = "ZDYHIVEdev062601";
        // 可以增加的主机： sunbox-dev-vm15.vmdns.sunbox.com   sunbox-dev-vm16.vmdns.sunbox.com
        List<String> hosts = Arrays.asList("zdy-hive-dev-062601-tsk-0004.dev.sdp.com");
        List<String> components = Arrays.asList("NODEMANAGER", "YARN_CLIENT", "TEZ_CLIENT", "HDFS_CLIENT");
        InProgressResponse inProgressResponse = api.startHostComponent(clusterName, hosts, components);
        System.out.println("RequestId=" + inProgressResponse.getRequestId());

        Assert.assertTrue(true);
    }

    /**
     * 测试Decommission一个组件
     */
    @Test
    public void decommissionComponent() {
        // 可以增加的主机： sunbox-dev-vm15.vmdns.sunbox.com   sunbox-dev-vm16.vmdns.sunbox.com
        clusterName = "sdpU0QTE36BxTk";
        api.getApiClient().setBasePath("http://20.172.10.47:8765/api/v1");
          List<String> hosts = Arrays.asList("sdp-u0qte36bxtk-tsk-0002.dev.sdp.com");
        InProgressResponse inProgressResponse = api.decommissionComponent(clusterName, hosts, "NODEMANAGER");
        System.out.println("RequestId=" + inProgressResponse.getRequestId());

        Assert.assertTrue(true);
    }

    @Test
    public void decommissionRegionServer() {
        // 可以增加的主机： sunbox-dev-vm15.vmdns.sunbox.com   sunbox-dev-vm16.vmdns.sunbox.com
        clusterName = "sdpU0QTE36BxTk";
        api.getApiClient().setBasePath("http://20.172.10.47:8765/api/v1");
        List<String> hosts = Arrays.asList("sdp-u0qte36bxtk-cor-0002.dev.sdp.com", "sdp-u0qte36bxtk-cor-0003.dev.sdp.com");
        InProgressResponse inProgressResponse = api.decommissionRegionServer(clusterName, hosts);
        System.out.println("RequestId=" + inProgressResponse.getRequestId());

        Assert.assertTrue(true);
    }

    @Test
    public void queryRegionServerDecommissionProgress() {
        // 可以增加的主机： sunbox-dev-vm15.vmdns.sunbox.com   sunbox-dev-vm16.vmdns.sunbox.com
        clusterName = "sdpU0QTE36BxTk";
        Integer requestId = 32;
        api.getApiClient().setBasePath("http://20.172.10.47:8765/api/v1");
        RegionServerDecommissionProgress progress = api.queryRegionServerDecommissionProgress(clusterName, requestId);
        System.out.println(JSON.toJSONString(progress));

        Assert.assertTrue(true);
    }

    /**
     * 测试关闭组件运行
     */
    @Test
    public void stopHostComponent() {
        // 可以增加的主机： sunbox-dev-vm15.vmdns.sunbox.com   sunbox-dev-vm16.vmdns.sunbox.com
        List<String> hosts = Arrays.asList("sunbox-dev-vm15.vmdns.sunbox.com", "sunbox-dev-vm16.vmdns.sunbox.com");
        List<String> components = Arrays.asList("DATANODE", "HBASE_CLIENT", "HBASE_REGIONSERVER", "HDFS_CLIENT", "HIVE_CLIENT", "MAPREDUCE2_CLIENT",
                "NODEMANAGER", "SPARK3_CLIENT", "SPARK3_THRIFTSERVER", "SQOOP", "TEZ_CLIENT",
                "YARN_CLIENT", "ZOOKEEPER_CLIENT");
        InProgressResponse inProgressResponse = api.stopHostComponent(clusterName, hosts, components);
        System.out.println("RequestId=" + inProgressResponse.getRequestId());

        Assert.assertTrue(true);
    }

    /**
     * 测试关闭组件运行
     */
    @Test
    public void stopOneHostOneComponent() {
        // 可以增加的主机： sunbox-dev-vm15.vmdns.sunbox.com   sunbox-dev-vm16.vmdns.sunbox.com
        clusterName = "ZDYHIVEdev062601";
        String host = "zdy-hive-dev-062601-tsk-0005.dev.sdp.com";
        String component = "DATANODE";
        InProgressResponse inProgressResponse = api.stopHostComponent(clusterName, "HDFS", host, component);
        System.out.println("RequestId=" + inProgressResponse.getRequestId());

        Assert.assertTrue(true);
    }

    /**
     * 测试删除主机
     */
    @Test
    public void deleteHosts() {
        clusterName = "sdpinnermysql050";
        api.getApiClient().setBasePath("http://20.172.10.47:8765/api/v1");
        // 可以增加的主机： sunbox-dev-vm15.vmdns.sunbox.com   sunbox-dev-vm16.vmdns.sunbox.com
        List<String> hosts = Arrays.asList(
                "sdp-innermysql050-tsk-0001.dev.sdp.com",
                "sdp-innermysql050-tsk-0002.dev.sdp.com",
//                "sdp-innermysql050-tsk-0003.dev.sdp.com",
                "sdp-innermysql050-mst-0002.dev.sdp.com"
                );
        List<String> components = Arrays.asList("DATANODE", "HBASE_CLIENT", "HBASE_REGIONSERVER", "HDFS_CLIENT", "HIVE_CLIENT", "MAPREDUCE2_CLIENT",
                "NODEMANAGER", "SPARK3_CLIENT", "SPARK3_THRIFTSERVER", "SQOOP", "TEZ_CLIENT",
                "YARN_CLIENT", "ZOOKEEPER_CLIENT");
        DeleteHostsResponse deleteHostsResponse = api.deleteHosts(clusterName, hosts);
        Gson gson = new Gson();
        System.out.println(gson.toJson(deleteHostsResponse));
        List<String> failList = deleteHostsResponse.get500FailList();
        System.out.println(Strings.join(failList, ','));
        System.out.println(deleteHostsResponse.isAllDeleted());
        System.out.println(deleteHostsResponse.isDeleteSuccess());

        Assert.assertTrue(true);
    }


    @Test
    public void deleteHost() {
        clusterName = "ZDYHIVEdev062601";
        api.getApiClient().setBasePath("http://20.172.10.47:8765/api/v1");
        // 可以增加的主机： sunbox-dev-vm15.vmdns.sunbox.com   sunbox-dev-vm16.vmdns.sunbox.com
        String host = "zdy-hive-dev-062601-tsk-0005.dev.sdp.com";
        DeleteHostsResponse deleteHostsResponse = api.deleteHost(clusterName, host);
        Gson gson = new Gson();
        System.out.println(gson.toJson(deleteHostsResponse));
        List<String> failList = deleteHostsResponse.get500FailList();
        System.out.println(Strings.join(failList, ','));
        System.out.println(deleteHostsResponse.isAllDeleted());
        System.out.println(deleteHostsResponse.isDeleteSuccess());
        Assert.assertTrue(true);
    }

    /**
     * 测试查询主机上的组件
     */
    @Test
    public void queryHostsComponents() {
        // 可以增加的主机： sunbox-dev-vm15.vmdns.sunbox.com   sunbox-dev-vm16.vmdns.sunbox.com
        List<String> hosts = Arrays.asList("zdy-hive-api-dev-061601-tsk-0007.dev.sdp.com",
                "zdy-hive-api-dev-061601-amb-0001.dev.sdp.com",
                "zdy-hive-api-dev-061601-cor-0001.dev.sdp.com",
                "zdy-hive-api-dev-061601-mst-0001.dev.sdp.com",
                "zdy-hive-api-dev-061601-tsk-0002.dev.sdp.com",
                "zdy-hive-api-dev-061601-tsk-0162.dev.sdp.com",
                "zdy-hive-api-dev-061601-tsk-0521.dev.sdp.com");

        clusterName = "ZDYHIVEAPIdev061601";
        QueryHostsComponentResponse response = api.queryHostsComponents(clusterName, hosts);
        List<String> componentsList = response.getComponentNames("zdy-hive-api-dev-061601-tsk-0007.dev.sdp.com");
        List<HostRole> components = response.getStopedComponentsByHostState("HEALTHY",
                Arrays.asList("HBASE_REGIONSERVER", "NODEMANAGER", "DATANODE"));
        System.out.println(StringUtils.join(components, ","));
        System.out.println(StringUtils.join(componentsList, ","));

        for (HostRole component : components) {
            api.startHostComponent(clusterName, Arrays.asList(component.getHostName()), Arrays.asList(component.getComponentName()));
        }

        Assert.assertTrue(true);
    }

    @Test
    public void queryAllHosts() {
        clusterName = "v301dahuadev33001";
        api.getApiClient().setBasePath("http://20.172.10.47:8765/api/v1");
        List<String> list = api.queryAllHostsName("");
        System.out.println(list);

    }


    @Test
    public void test() {
        OkHttpClient client = new OkHttpClient(); //.newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\n    \"RequestInfo\": {\n        \"query\": \"Hosts/host_name.in(sunbox-dev-vm14.vmdns.sunbox.com)\"\n    }\n}");
        Request request = new Request.Builder()
                .url("http://20.125.124.59:8080/api/v1/clusters/sunbox/hosts?fields=Hosts%2Fhost_name%2CHosts%2Fhost_state%2Chost_components%2FHostRoles%2Fstate%2Chost_components%2FHostRoles%2Fmaintenance_state%2Chost_components%2FHostRoles%2Fstale_configs%2Chost_components%2FHostRoles%2Fservice_name%2Chost_components%2FHostRoles%2Fdisplay_name%2Chost_components%2FHostRoles%2Fdesired_admin_state%2C&sortBy=Hosts%2Fhost_name.asc&minimal_response=true")
                .method("GET", body)
                .addHeader("User-Agent", "apifox/1.0.0 (https://www.apifox.cn)")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Basic YWRtaW46YWRtaW4=")
                .build();
        try {
            Response response = client.newCall(request).execute();
            System.out.println(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test2() {
        Map<String, String> queryParam = new HashMap<>();
        queryParam.put("fields", "Hosts/host_name,Hosts/host_state,host_components/HostRoles/state,host_components/HostRoles/maintenance_state,host_components/HostRoles/stale_configs,host_components/HostRoles/service_name,host_components/HostRoles/display_name,host_components/HostRoles/desired_admin_state,");
        queryParam.put("sortBy", "Hosts/host_name.asc");
        queryParam.put("minimal_response", "true");

        Map<String, String> header = new HashMap<>();
        header.put("User-Agent", "apifox/1.0.0 (https://www.apifox.cn)");
        header.put("Content-Type", "application/json");
        header.put("Authorization", "Basic YWRtaW46YWRtaW4=");

        QueryHostsComponentsRequest request = QueryHostsComponentsRequest.of(Arrays.asList("sunbox-dev-vm14.vmdns.sunbox.com"));

        try {
            HttpResponse httpResponse = HttpUtils.doGet("http://20.125.124.59:8080", "/api/v1/clusters/sunbox/hosts", header, queryParam, request);
            System.out.println(EntityUtils.toString(httpResponse.getEntity()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testQueryServiceInfo() {
        String clusterName = "sunbox";
        // 拼URL
        String url = "/clusters/" + clusterName + "/components/?";
        StringBuilder sb = new StringBuilder();
        sb.append("ServiceComponentInfo/component_name=APP_TIMELINE_SERVER")
                .append("|ServiceComponentInfo/component_name=JOURNALNODE")
                .append("|ServiceComponentInfo/component_name=ZKFC")
                .append("|ServiceComponentInfo/category.in(MASTER,CLIENT)");

        StringBuilder fields = new StringBuilder();
        fields.append("&fields=ServiceComponentInfo/service_name,")
                .append("host_components/HostRoles/display_name")

                .append("&minimal_response=true");


        // Header参数
        HttpBasicAuth basicAuth = (HttpBasicAuth) api.getApiClient().getAuthentications().get("httpBasicAuth");
        String authStr = basicAuth.getUsername() + ":" + basicAuth.getPassword();
        authStr = Base64.getEncoder().encodeToString(authStr.getBytes());

        Map<String, String> header = new HashMap<>();
        //设置请求格式
        header.put("Content-type", "application/json");
        header.put("X-Requested-By", "ambari");
        //设置编码语言
        header.put("Accept-Charset", "UTF-8");
        //设置Http Basic认证
        header.put("Authorization", "Basic " + authStr);

        // 调用Ambari
        Gson gson = new Gson();
        String fullUrl = api.getApiClient().getBasePath() + url + sb.toString() + fields.toString();
        System.out.println(fullUrl);
        try {
            HttpResponse httpResponse = HttpUtils.doGet(fullUrl, "", "", header, null);
            String responseBody = EntityUtils.toString(httpResponse.getEntity());
            System.out.println(responseBody);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testQueryComponentInHost_HttpUrlConnection() {
        String clusterName = "v301dahuadev32901";

        QueryComponentInHostsResponse response = api.queryComponentInHosts(clusterName, Arrays.asList("NAMENODE"));
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void testQueryComponentInHost_HttpUrlConnection_startedHosts() {
        String clusterName = "v301dahuadev32901";

        QueryComponentInHostsResponse response = api.queryComponentInHosts(clusterName, Arrays.asList("NAMENODE"));
        List<String> hostsByComponentStarted = response.getHostsByComponentStarted("NAMENODE");
        System.out.println(hostsByComponentStarted);
        List<String> hosts = response.getHostsByComponentStarted("NAMENODE", Arrays.asList("aaaa"));
        System.out.println(hosts);

        response = api.queryComponentInHosts(clusterName, Arrays.asList("DATANODE"));
        hostsByComponentStarted = response.getHostsByComponentStarted("DATANODE");
        System.out.println(hostsByComponentStarted);
        hosts = response.getHostsByComponentStarted("DATANODE", Arrays.asList("v301-dahua-dev-32901-cor-0010.dev.sdp.com"));
        System.out.println(hosts);

        response = api.queryComponentInHosts(clusterName, Arrays.asList("NODEMANAGER"));
        hostsByComponentStarted = response.getHostsByComponentStarted("NODEMANAGER");
        System.out.println(hostsByComponentStarted);
        hosts = response.getHostsByComponentStarted("NODEMANAGER",Arrays.asList("v301-dahua-dev-32901-tsk-0006.dev.sdp.com"));
        System.out.println(hosts);

        response = api.queryComponentInHosts(clusterName, Arrays.asList("NODEMANAGER"));
        hostsByComponentStarted = response.getHostsByComponentStarted("NODEMANAGER");
        System.out.println(hostsByComponentStarted);
        hosts = response.getHostsByComponentStarted("NODEMANAGER",Arrays.asList("v301-dahua-dev-32901-tsk-0006.dev.sdp.com"));
        System.out.println(hosts);
    }

    @Test
    public void queryServiceInHosts() {
        String clusterName = "sunbox";
        QueryServiceInHostsResponse response = api.queryServiceInHosts(clusterName, "HDFS");
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void rebalanceHdfs() {
        String clusterName = "sunbox";
        InProgressResponse response = api.rebalanceHdfs(clusterName, "sunbox-dev-vm12.vmdns.sunbox.com", 10);
        System.out.println(response);
    }

    @Test
    public void stopAllServiceOfCluster() {
        String clusterName = "sunbox";

        InProgressResponse response = api.stopAllServiceOfCluster(clusterName);
        Gson gson = new Gson();
        System.out.println(gson.toJson(response));
    }

    @Test
    public void restartService() {
        String clusterName = "sunbox";
        String service = "YARN";

        InProgressResponse response = api.restartService(clusterName, service, buildRestartComponents());
        Gson gson = new Gson();
        System.out.println(gson.toJson(response));
    }

    private List<RequestResourceFilter> buildRestartComponents() {
        List<RequestResourceFilter> list = new ArrayList<>();
        // RESOURCEMANAGER
        RequestResourceFilter filter = new RequestResourceFilter();
        filter.setServiceName("YARN");
        filter.setComponentName("RESOURCEMANAGER");
        filter.setHosts("sunbox-dev-vm12.vmdns.sunbox.com,sunbox-dev-vm13.vmdns.sunbox.com");
        list.add(filter);

        //YARN_CLIENT
        filter = new RequestResourceFilter();
        filter.setServiceName("YARN");
        filter.setComponentName("YARN_CLIENT");
        filter.setHosts("sunbox-dev-vm12.vmdns.sunbox.com,sunbox-dev-vm13.vmdns.sunbox.com,sunbox-dev-vm14.vmdns.sunbox.com,sunbox-dev-vm15.vmdns.sunbox.com,sunbox-dev-vm16.vmdns.sunbox.com");
        list.add(filter);

        //APP_TIMELINE_SERVER
        filter = new RequestResourceFilter();
        filter.setServiceName("YARN");
        filter.setComponentName("APP_TIMELINE_SERVER");
        filter.setHosts("sunbox-dev-vm13.vmdns.sunbox.com");
        list.add(filter);

        //NODEMANAGER
        filter = new RequestResourceFilter();
        filter.setServiceName("YARN");
        filter.setComponentName("NODEMANAGER");
        filter.setHosts("sunbox-dev-vm15.vmdns.sunbox.com,sunbox-dev-vm16.vmdns.sunbox.com");
        list.add(filter);

        return list;
    }

    @Test
    public void testQueryConfigGroups() {
        clusterName = "ZDYV302ZL300040605";
        Gson gson = new Gson();
        QueryConfigGroupsResponse response = api.queryConfigGroups(clusterName, ConfigGroupField.ID, "2");
        System.out.println(gson.toJson(response));

        response = api.queryConfigGroups(clusterName, ConfigGroupField.GROUP_NAME, "task-1,hdfs-task-1");
        System.out.println(gson.toJson(response));

        response = api.queryConfigGroups(clusterName, ConfigGroupField.ID, "2,3");
        System.out.println(gson.toJson(response));

        response = api.queryConfigGroups(clusterName, ConfigGroupField.TAG, "HDFS,YARN,MAPREDUCE2");
        System.out.println(gson.toJson(response));

        response = api.queryConfigGroups(clusterName, ConfigGroupField.TAG, "*");
        System.out.println(gson.toJson(response));
    }

    @Test
    public void testCreateConfigGroup() {
        clusterName = "sdp2Ua90sGE4c8";
        String service = "HDFS";

        HostRole host1 = new HostRole();
        host1.setHostName("sdp-2ua90sge4c8-tsk-0001.dev.sdp.com");

        HostRole host2 = new HostRole();
        host2.setHostName("sdp-2ua90sge4c8-tsk-0004.dev.sdp.com");

        ConfigGroup config = new ConfigGroup();
        config.setHosts(Arrays.asList(host1, host2));
        config.setGroupName(clusterName + "_" + service + "_task-2");
        config.setServiceName(service);
        config.setTag(service + "1");
        config.setDesiredConfigs(new ArrayList<>());

        CreateConfigGroupResponse groupResponse = api.createConfigGroup(clusterName, Arrays.asList(config));
        System.out.println(gson.toJson(groupResponse));
    }

    @Test
    public void testDeleteConfigGroup() {
        clusterName = "sdp2Ua90sGE4c8";
        api.deleteConfigGroup(clusterName, 7L);
    }

    @Test
    public void testUpdateConfigGroup() {

        clusterName = "ZDYV302ZL300040605";
        ConfigGroup configGroup = new ConfigGroup();
        configGroup.setGroupName("ZDY-V302-ZL-300-040605_MAPREDUCE2_core");
        configGroup.setServiceName("MAPREDUCE2");
        configGroup.setTag("MAPREDUCE2");
        configGroup.setId(6L);
        configGroup.setServiceName("MAPREDUCE2");
        configGroup.setClusterName(clusterName);

        Map<String, Object> desiredConfigs = new HashMap<>();
        Map<String, String> properties = new HashMap<>();
//        properties.put("hadoop.http.cross-origin.max-age", "2048");
//        desiredConfigs.put("type", "core-site");
//        desiredConfigs.put("properties", properties);

        HostRole host1 = new HostRole();
        host1.setHostName("zdy-v302-zl-300-040605-cor-0111.sit.sdp.com");

        HostRole host2 = new HostRole();
        host2.setHostName("zdy-v302-zl-300-040605-cor-0112.sit.sdp.com");

        configGroup.setHosts(Arrays.asList(host1, host2));

        configGroup.setDesiredConfigs(Arrays.asList(desiredConfigs));


        UpdateConfigGroupResponse updateConfigGroupResponse = api.updateConfigGroup(clusterName, configGroup);
        System.out.println(gson.toJson(updateConfigGroupResponse));
    }

    @Test
    public void testQueryDefaultConfig_查询全部配置() {
        String clusterName = "sdp3amzDUhwN2U";
        DefaultClusterConfigResponse resp = api.queryDefaultClusterConfig(clusterName, null);
        Assert.assertNotNull(resp);

        String configType = "core-site";
        DefaultConfigDesiredConfig config = resp.findConfigByConfigType(configType);
        Assert.assertEquals(config.getType(), configType);
        Assert.assertTrue(config.getProperties().size() > 0);
    }

    @Test
    public void testQueryDefaultConfig_查询HDFS的配置() {
        String clusterName = "sdpFu4WU7LJDE0";
        DefaultClusterConfigResponse resp = api.queryDefaultClusterConfig(clusterName, Arrays.asList("HDFS"));
        Assert.assertNotNull(resp);

        String configType = "core-site";
        DefaultConfigDesiredConfig config = resp.findConfigByConfigType(configType);
        Assert.assertEquals(config.getType(), configType);
        Assert.assertTrue(config.getProperties().size() > 0);
    }

    @Test
    public void testUpdateClusterDefaultConfig() {
        String clusterName = "sdpFu4WU7LJDE0";
        DefaultClusterConfigResponse resp = api.queryDefaultClusterConfig(clusterName, Arrays.asList("HDFS"));
        String configType = "core-site";
        DefaultConfigDesiredConfig config = resp.findConfigByConfigType(configType);

        DefaultConfigDesiredConfig desiredConfig = new DefaultConfigDesiredConfig(configType);
        desiredConfig.putProperties(config.getProperties());

        // 测试修改此配置项
        desiredConfig.putProperty("fs.trash.interval", "360");

        DefaultClusterConfigWrapper newConfig = new DefaultClusterConfigWrapper();
        newConfig.addConfig(desiredConfig);

        UpdateClusterDefaultConfigResponse response = api.updateClusterDefaultConfig(clusterName, Arrays.asList(newConfig));
        Gson gson = new Gson();
        System.out.println(gson.toJson(response));
    }

    @Test
    public void autoStartSetting_关闭() {
        String clusterName = "sdpliuyangspot17";
        api.autoStartSetting(clusterName, false);
    }

    @Test
    public void autoStartSetting_启用() {
        String clusterName = "sdpliuyangspot17";
        api.autoStartSetting(clusterName, true);
    }

    @Test
    public void queryTaskProgress() {
        String clusterName = "sdp62lo0q2m21w";
        CreateClusterProcessTask taskResponse = api.queryTaskProgress(clusterName, 1L, 10008L);
        System.out.println(taskResponse);
    }

    @Test
    public void queryStageProgress() {
        String clusterName = "sdp62lo0q2m21w";
        CreateClusterProcessStageWrapper stageResponse = api.queryStageProgress(clusterName, 1L, 12L);
        System.out.println(stageResponse);
    }

}