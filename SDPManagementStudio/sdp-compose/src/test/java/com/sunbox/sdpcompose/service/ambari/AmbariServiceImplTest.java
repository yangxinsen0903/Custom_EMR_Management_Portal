package com.sunbox.sdpcompose.service.ambari;

import com.google.gson.reflect.TypeToken;
import com.sunbox.domain.ambari.AmbariConfigItem;
import com.sunbox.sdpcompose.enums.AmbariHostState;
import com.sunbox.sdpcompose.mapper.AmbariConfigItemMapper;
import com.sunbox.sdpcompose.service.ambari.blueprint.*;
import com.sunbox.sdpcompose.service.ambari.clustertemplate.ClusterTemplate;
import com.sunbox.sdpcompose.service.ambari.clustertemplate.ClusterTemplateHostGroup;
import com.sunbox.sdpcompose.service.ambari.enums.BDComponent;
import com.sunbox.sdpcompose.service.ambari.enums.ConfigItemType;
import com.sunbox.sdpcompose.service.ambari.enums.HostGroupRole;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sunbox.sdp.ambari.client.JSON;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;

/**
 * @author: wangda
 * @date: 2022/12/14
 */
class AmbariServiceImplTest {
    AmbariServiceImpl svc = new AmbariServiceImpl();

    @Test
    void startAllClusterServicesHA() {

    }

    @Test
    void overrideDefaultConfig_OverrideConfigIsNull() {
        // 准备测试数据
        Blueprint blueprint = readBlueprintSwaggerFromFile();

        // 测试
        svc.overrideDefaultConfig(blueprint, null);
    }


    @Test
    void testreplace(){
        String a="https://{wgetpath}/sunbox3/shell/2.0.1/hdfs.sh";

        System.out.println(a.replace("\\{wgetpath\\}","sdfsdf"));
    }

    @Test
    void overrideDefaultConfig_OverrideConfigIsNotNull() {
        // 准备测试数据
        Blueprint blueprint = readBlueprintSwaggerFromFile();
        List<Map<String, BlueprintConfiguration>> overrideConfig = buildOverrideConfig();
        Blueprint originBlueprint = new Blueprint();
        originBlueprint.setConfigurations(overrideConfig);
        // 测试
        svc.overrideDefaultConfig(blueprint, originBlueprint);

        // 验证
        BlueprintConfiguration config = blueprint.findConfigByConfigType("hdfs-site");
        String p1 = (String)config.getProperty("dfs.namenode.http-address.sunboxcluster.nn1");
        String p2 = (String)config.getProperty("dfs.nameservices");
        Assertions.assertNotEquals(p1, "master1.dev.sunbox.com:50070");
        Assertions.assertEquals(p2, "testNameServices");
    }

    private Blueprint readBlueprintSwaggerFromFile() {
        String configFilePath = "/data/home/wangda/work/01-doc/01-尚博信/08-SDP/05-项目资料/06-Ambari创建集群接口测试/复制集群";
        byte[] file = new byte[0];
        try {
            file = Files.readAllBytes(Paths.get(configFilePath + "/blueprint.json"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String content = new String(file);
        JSON json = new JSON();
        Blueprint obj = json.deserialize(content, new TypeToken<Blueprint>() {
        }.getType());
        return obj;
    }

    private List<Map<String, BlueprintConfiguration>> buildOverrideConfig() {
        List configList = new ArrayList();
        Map<String, BlueprintConfiguration> coreSiteConfig = new HashMap<>();
        BlueprintConfiguration cfg = new BlueprintConfiguration();
        coreSiteConfig.put("hdfs-site", cfg);
        cfg.setConfigItemName("hdfs-site");
        // 带HOSTGROUP的参数
        cfg.getProperties().put("dfs.namenode.http-address.sunboxcluster.nn1", "master1.dev.sunbox.com:50070");
        cfg.getProperties().put("dfs.nameservices", "testNameServices");

        configList.add(coreSiteConfig);
        return configList;
    }

    @Test
    void getServiceVersionFromServiceNames() {
        Integer ver = svc.getServiceVersionFromServiceNames("spark", Arrays.asList("Sqoop", "Spark3", "HDFS"));
        Assertions.assertEquals(3, ver.intValue());
    }

    @Test
    void handleSparkConfig() {
        Map<String, Map<String, Object>> configMap = new HashMap<>();
        Map<String, Object> coreSite = new HashMap<>();
        coreSite.put("key1", "value1");
        coreSite.put("key2", "value2");

        Map<String, Object> sparkSite = new HashMap<>();
        sparkSite.put("key3", "value3");
        sparkSite.put("key4", "value4");

        configMap.put("core-site", coreSite);
        configMap.put("spark-defaults", sparkSite);


        svc.handleSparkConfig(Arrays.asList("Sqoop", "Spark3", "HDFS"), configMap);

        Assertions.assertNotNull(configMap.get("spark3-defaults"));
    }

    @Test
    void handleSparkConfig_Spark3() {
        Map<String, Map<String, Object>> configMap = new HashMap<>();
        Map<String, Object> coreSite = new HashMap<>();
        coreSite.put("key1", "value1");
        coreSite.put("key2", "value2");

        Map<String, Object> sparkSite = new HashMap<>();
        sparkSite.put("key3", "value3");
        sparkSite.put("key4", "value4");

        configMap.put("core-site", coreSite);
        configMap.put("spark3-defaults", sparkSite);


        svc.handleSparkConfig(Arrays.asList("Sqoop", "Spark3", "HDFS"), configMap);

        Assertions.assertNotNull(configMap.get("spark3-defaults"));
        Assertions.assertNull(configMap.get("spark33-defaults"));
    }

    @Test
    void decommissionComponent() {
        AmbariInfo info = AmbariInfo.of("http://20.125.124.59:8080/api/v1", "admin", "admin");
        InProgressResult result = svc.decommissionComponent(info, "sunbox", Arrays.asList("sunbox-dev-vm15.vmdns.sunbox.com", "sunbox-dev-vm16.vmdns.sunbox.com"),
                BDComponent.DATANODE.name());
        System.out.println(result.getRequestId());

        result = svc.decommissionComponent(info, "sunbox", Arrays.asList("sunbox-dev-vm15.vmdns.sunbox.com", "sunbox-dev-vm16.vmdns.sunbox.com"),
                BDComponent.NODEMANAGER.name());
        System.out.println(result.getRequestId());
    }

    @Test
    void stopHostAllComponents() {
        AmbariInfo info = AmbariInfo.of("http://20.125.124.59:8080/api/v1", "admin", "admin");
        InProgressResult result = svc.stopHostAllComponents(info, "sunbox", Arrays.asList("sunbox-dev-vm15.vmdns.sunbox.com", "sunbox-dev-vm16.vmdns.sunbox.com"));
        System.out.println(result.getRequestId());
    }

    @Test
    void deleteHosts() {
        AmbariInfo info = AmbariInfo.of("http://20.125.124.59:8080/api/v1", "admin", "admin");
        DeleteHostResult result = svc.deleteHosts(info, "sunbox", Arrays.asList("sunbox-dev-vm15.vmdns.sunbox.com", "sunbox-dev-vm16.vmdns.sunbox.com"));
        System.out.println(result.isAllDeleted());
        System.out.println(StringUtils.join(result.getDeleteFailHosts(), ","));
        System.out.println(StringUtils.join(result.getDeleteSuccessHosts(), ","));
    }

    @Test
    void queryHosts() {
        AmbariInfo info = AmbariInfo.of("http://20.172.10.47:8765/api/v1", "admin", "admin");
        List<String> result = svc.queryHosts(info, "v301dahuadev33001",
                AmbariHostState.HEALTHY);
        System.out.println(result);
    }

    @Test
    void handleGangliaConfig() {
        CreateBlueprintCmd cmd = new CreateBlueprintCmd();
        cmd.setHa(true);
        HostInstance host = new HostInstance();
        host.setHostName("ambar-server-name");
//        cmd.setAmbariHosts(Arrays.asList(host));
//        cmd.setMasterHosts(Arrays.asList(host));


        Blueprint blueprint = new Blueprint();
        BlueprintConfiguration config = new BlueprintConfiguration();
        config.putProperties("content", content);
        Map configMap = new HashMap<String, Object>();
        configMap.put("hadoop-metrics2.properties", config);
        blueprint.setConfigurations(Arrays.asList(configMap));

        blueprint.setHa(ConfigItemType.HA);

        svc.handleGangliaConfig(cmd, blueprint);

        cmd.setHa(false);
        blueprint.setHa(ConfigItemType.NON_HA);
        svc.handleGangliaConfig(cmd, blueprint);
    }


    private String content = "*.period=300\n" +
            "\n" +
            "*.sink.ganglia.class=org.apache.hadoop.metrics2.sink.ganglia.GangliaSink31\n" +
            "*.sink.ganglia.period=10\n" +
            "\n" +
            "# default for supportsparse is false\n" +
            "*.sink.ganglia.supportsparse=true\n" +
            "\n" +
            ".sink.ganglia.slope=jvm.metrics.gcCount=zero,jvm.metrics.memHeapUsedM=both\n" +
            ".sink.ganglia.dmax=jvm.metrics.threadsBlocked=70,jvm.metrics.memHeapUsedM=40\n" +
            "\n" +
            "# Hook up to the server\n" +
            "namenode.sink.ganglia.servers=239.2.11.71\n" +
            "datanode.sink.ganglia.servers=239.2.11.71\n" +
            "jobtracker.sink.ganglia.servers=239.2.11.71\n" +
            "tasktracker.sink.ganglia.servers=239.2.11.71\n" +
            "maptask.sink.ganglia.servers=239.2.11.71\n" +
            "reducetask.sink.ganglia.servers=239.2.11.71\n" +
            "resourcemanager.sink.ganglia.servers=239.2.11.71\n" +
            "nodemanager.sink.ganglia.servers=239.2.11.71\n" +
            "historyserver.sink.ganglia.servers=239.2.11.71\n" +
            "journalnode.sink.ganglia.servers=239.2.11.71\n" +
            "nimbus.sink.ganglia.servers=239.2.11.71\n" +
            "supervisor.sink.ganglia.servers=239.2.11.71\n" +
            "\n" +
            "resourcemanager.sink.ganglia.tagsForPrefix.yarn=Queue\n" +
            "\n" +
            "\n" +
            "{% if has_metric_collector %}\n" +
            "\n" +
            "*.period={{metrics_collection_period}}\n" +
            "*.sink.timeline.plugin.urls=file:///usr/lib/ambari-metrics-hadoop-sink/ambari-metrics-hadoop-sink.jar\n" +
            "*.sink.timeline.class=org.apache.hadoop.metrics2.sink.timeline.HadoopTimelineMetricsSink\n" +
            "*.sink.timeline.period={{metrics_collection_period}}\n" +
            "*.sink.timeline.sendInterval={{metrics_report_interval}}000\n" +
            "*.sink.timeline.slave.host.name={{hostname}}\n" +
            "*.sink.timeline.zookeeper.quorum={{zookeeper_quorum}}\n" +
            "*.sink.timeline.protocol={{metric_collector_protocol}}\n" +
            "*.sink.timeline.port={{metric_collector_port}}\n" +
            "*.sink.timeline.instanceId = {{cluster_name}}\n" +
            "*.sink.timeline.set.instanceId = {{set_instanceId}}\n" +
            "*.sink.timeline.host_in_memory_aggregation = {{host_in_memory_aggregation}}\n" +
            "*.sink.timeline.host_in_memory_aggregation_port = {{host_in_memory_aggregation_port}}\n" +
            "{% if is_aggregation_https_enabled %}\n" +
            "*.sink.timeline.host_in_memory_aggregation_protocol = {{host_in_memory_aggregation_protocol}}\n" +
            "{% endif %}";

    @Test
    void generateNodeManagerResourceConfig() {
        HostInstance host = new HostInstance();
        host.setvCpu(8);
        host.setMemoryGB(64);

        ClusterHostGroup hostGroup = new ClusterHostGroup();
        hostGroup.setRole(HostGroupRole.TASK);
        hostGroup.setHosts(Arrays.asList(host));
        hostGroup.setGroupName("Task-1");
        CreateBlueprintCmd cmd = new CreateBlueprintCmd();
        cmd.setHostGroups(Arrays.asList(hostGroup));

        ClusterTemplate createTemplate = new ClusterTemplate();

        svc.generateNodeManagerResourceConfig(Arrays.asList(hostGroup), createTemplate);

        org.assertj.core.api.Assertions.assertThat(createTemplate.getHostGroups()).isEmpty();

        ClusterTemplateHostGroup group = new ClusterTemplateHostGroup();
        group.setName("Task-1");
        createTemplate.getHostGroups().add(group);

        svc.generateNodeManagerResourceConfig(Arrays.asList(hostGroup), createTemplate);

        svc.generateNodeManagerResourceConfig(Arrays.asList(hostGroup), createTemplate);

    }

    @Test
    public void handleHBaseConfig_包括Tez场景() {
        CreateBlueprintCmd cmd = new CreateBlueprintCmd();
        Blueprint blueprint = new Blueprint();
        cmd.setServices(Arrays.asList("TEZ"));
        svc.handleTezConfig(cmd, blueprint);
    }

    @Test
    public void handleHBaseConfig_不包括Tez_正常替换() {
        CreateBlueprintCmd cmd = new CreateBlueprintCmd();
        Blueprint blueprint = new Blueprint();

        BlueprintConfiguration configuration = new BlueprintConfiguration("yarn-site");
        configuration.putProperties("yarn.timeline-service.entity-group-fs-store.group-id-plugin-classes", "yarn.timeline-service.entity-group-fs-store.group-id-plugin-classes");

        Map<String, BlueprintConfiguration> configurationMap = new HashMap<>();
        configurationMap.put("yarn-site", configuration);
        blueprint.setConfigurations(Arrays.asList(configurationMap));

        svc.handleTezConfig(cmd, blueprint);
        Assertions.assertNull(configuration.getProperty("yarn.timeline-service.entity-group-fs-store.group-id-plugin-classes"));
    }

    @Test
    public void handleTezConfig() {
        CreateBlueprintCmd cmd = new CreateBlueprintCmd();
        Blueprint blueprint = new Blueprint();

        cmd.setServices(Arrays.asList("TEZ"));
        BlueprintConfiguration configuration = new BlueprintConfiguration("yarn-site");
        configuration.putProperties("yarn.timeline-service.entity-group-fs-store.group-id-plugin-classes", "yarn.timeline-service.entity-group-fs-store.group-id-plugin-classes");

        Map<String, BlueprintConfiguration> configurationMap = new HashMap<>();
        configurationMap.put("yarn-site", configuration);
        blueprint.setConfigurations(Arrays.asList(configurationMap));

        svc.handleTezConfig(cmd, blueprint);
        Assertions.assertNotNull(configuration.getProperty("yarn.timeline-service.entity-group-fs-store.group-id-plugin-classes"));
    }

    @Test
    void generateResourceConfig() {
        HostInstance hostInstance = new HostInstance();
        hostInstance.setMemoryGB(16);
        hostInstance.setvCpu(4);
        List<ClusterHostGroup> hostGroups = new ArrayList<>();
        ClusterHostGroup core = new ClusterHostGroup();
        core.setRole(HostGroupRole.CORE);
        core.setHosts(Arrays.asList(hostInstance));
        ClusterHostGroup task = new ClusterHostGroup();
        task.setRole(HostGroupRole.TASK);
        task.setHosts(Arrays.asList(hostInstance));
        hostGroups.add(core);
        hostGroups.add(task);
        svc.generateResourceConfig(hostGroups);

    }

    @Test
    public void queryCreateClusterProgressWithAllTask() {
        AmbariInfo info = AmbariInfo.of("http://20.172.10.47:8765/api/v1", "admin", "admin");
        QueryProgressCmd cmd = new QueryProgressCmd();
        cmd.setClusterName("sdp62lo0q2m21w");
        cmd.setRequestId(1L);
        cmd.setAmbariInfo(info);
        long start = System.currentTimeMillis();
        QueryProgressResult queryProgressResult = svc.queryCreateClusterProgressWithAllTask(cmd);
        long end = System.currentTimeMillis();
        System.out.println("调用接口耗时：" + (end-start) + " ms");
        System.out.println(com.alibaba.fastjson.JSON.toJSONString(queryProgressResult.getFailHosts()));
    }

    @Test
    public void verifyHostsFromAmbari() {
        AmbariServiceImpl impl = Mockito.spy(svc);
        Mockito.doReturn(Arrays.asList("a","b","c")).when(impl).queryAllHosts(any(), any());

        List<String> verifiedHosts = impl.verifyHostsFromAmbari(null, null, Arrays.asList("a", "d"));

        Assertions.assertEquals(1, verifiedHosts.size());
        Assertions.assertEquals("a", verifiedHosts.get(0));
    }

    @Test
    public void generateOneTaskGroupMultiDiskConfig() {
        // 准备数据
        List<AmbariConfigItem> configItems = buildConfigItems();

        AmbariConfigItemMapper configItemMapper = Mockito.mock(AmbariConfigItemMapper.class);
        Mockito.doReturn(configItems).when(configItemMapper).queryByDynamicTypeAndItemType(any(), any(), any());

        AmbariServiceImpl impl = Mockito.spy(svc);
        impl.configItemMapper = configItemMapper;

        DiskInfo diskInfo = new DiskInfo();
        diskInfo.setCount(3);
        HostInstance host = new HostInstance();
        host.setDisks(Arrays.asList(diskInfo));

        List<HostInstance> hosts = new ArrayList<>();
        hosts.add(host);

        // 测试
        List<Map<String, BlueprintConfiguration>> configs = impl.generateOneTaskGroupMultiDiskConfig("", hosts, ConfigItemType.HA);

        // 验证
        Assertions.assertNotNull(configs);
    }

    private List<AmbariConfigItem> buildConfigItems() {
        List<AmbariConfigItem> configItems = new ArrayList<>();
        AmbariConfigItem item = new AmbariConfigItem();
        item.setConfigTypeCode("yarn-site");
        item.setKey("yarn.nodemanager.local-dirs");
        item.setValue("/data/disk0/hadoop/yarn/local");
        item.setItemType("HA");
        configItems.add(item);

        item = new AmbariConfigItem();
        item.setConfigTypeCode("yarn-site");
        item.setKey("yarn.timeline-service.leveldb-timeline-store.path");
        item.setValue("/data/disk0/hadoop/yarn/timeline");
        item.setItemType("HA");
        configItems.add(item);

        item = new AmbariConfigItem();
        item.setConfigTypeCode("yarn-site");
        item.setKey("yarn.timeline-service.leveldb-state-store.path");
        item.setValue("/data/disk0/hadoop/yarn/timeline");
        item.setItemType("HA");
        configItems.add(item);

        item = new AmbariConfigItem();
        item.setConfigTypeCode("yarn-site");
        item.setKey("yarn.nodemanager.log-dirs");
        item.setValue("/data/disk0/hadoop/yarn/log");
        item.setItemType("HA");
        configItems.add(item);

        return configItems;
    }
}