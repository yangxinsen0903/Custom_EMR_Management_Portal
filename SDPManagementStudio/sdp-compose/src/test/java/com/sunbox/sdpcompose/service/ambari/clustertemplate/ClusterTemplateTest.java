package com.sunbox.sdpcompose.service.ambari.clustertemplate;

import com.google.gson.Gson;
import com.sunbox.sdpcompose.service.ambari.CreateClusterCmd;
import com.sunbox.sdpcompose.service.ambari.blueprint.Blueprint;
import com.sunbox.sdpcompose.service.ambari.blueprint.BlueprintConfiguration;
import com.sunbox.sdpcompose.service.ambari.blueprint.HostGroup;
import com.sunbox.sdpcompose.service.ambari.enums.HostGroupRole;
import com.sunbox.sdpcompose.util.JacksonUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


/**
 * @author: wangda
 * @date: 2022/12/7
 */
class ClusterTemplateTest {

    @Test
    public void hello() {
        String json = "{\"ambarInfo\":{\"ambariApiClient\":{\"authentications\":{\"httpBasicAuth\":{\"password\":\"admin\",\"username\":\"admin\"}},\"basePath\":\"http://10.2.0.17:8080/api/v1\",\"connectTimeout\":10000,\"debugging\":false,\"httpClient\":{\"connectTimeout\":10000,\"dispatcher\":{\"executorService\":{\"activeCount\":0,\"completedTaskCount\":0,\"corePoolSize\":0,\"largestPoolSize\":0,\"maximumPoolSize\":2147483647,\"poolSize\":0,\"queue\":[],\"rejectedExecutionHandler\":{},\"shutdown\":false,\"taskCount\":0,\"terminated\":false,\"terminating\":false,\"threadFactory\":{}},\"maxRequests\":64,\"maxRequestsPerHost\":5,\"queuedCallCount\":0,\"runningCallCount\":0},\"followRedirects\":true,\"followSslRedirects\":true,\"readTimeout\":120000,\"retryOnConnectionFailure\":true,\"writeTimeout\":30000},\"jSON\":{\"gson\":{}},\"readTimeout\":120000,\"verifyingSsl\":true,\"writeTimeout\":30000}},\"ambariHosts\":[{\"disks\":[{\"sizeGB\":200,\"skuName\":\"StandardSSD_LRS\"}],\"hostName\":\"sdp-fceo5nkpvds-ambari-0001\",\"hostRole\":\"ambari\",\"osDiskSize\":100}],\"blueprintName\":\"blue-print-sdp-fCEo5nkpvDs\",\"clusterName\":\"sdpfCEo5nkpvDs\",\"configurations\":{},\"coreHosts\":[{\"disks\":[{\"sizeGB\":200,\"skuName\":\"StandardSSD_LRS\"}],\"hostName\":\"sdp-fceo5nkpvds-core-0001\",\"hostRole\":\"core\",\"osDiskSize\":100},{\"disks\":[{\"$ref\":\"$.coreHosts[0].disks[0]\"}],\"hostName\":\"sdp-fceo5nkpvds-core-0002\",\"hostRole\":\"core\",\"osDiskSize\":100}],\"dbConfigs\":[{\"appType\":\"HIVE_SITE\",\"configType\":\"hive-site\",\"connectionUrl\":\"jdbc:mysql://sunboxdev.mysql.database.azure.com:3306/q13\",\"driverClassName\":\"com.mysql.jdbc.Driver\",\"password\":\"abctest\",\"userName\":\"abctest\"},{\"appType\":\"HIVE_ENV\",\"configType\":\"hive-env\",\"dbName\":\"q13\"}],\"ha\":true,\"masterHosts\":[{\"disks\":[{\"sizeGB\":200,\"skuName\":\"StandardSSD_LRS\"}],\"hostName\":\"sdp-fceo5nkpvds-master-0001\",\"hostRole\":\"master\",\"osDiskSize\":100},{\"disks\":[{\"$ref\":\"$.masterHosts[0].disks[0]\"}],\"hostName\":\"sdp-fceo5nkpvds-master-0002\",\"hostRole\":\"master\",\"osDiskSize\":100}],\"services\":[\"SQOOP\",\"TEZ\",\"YARN\",\"ZOOKEEPER\",\"MAPREDUCE2\",\"SPARK3\",\"HDFS\",\"HIVE\"],\"stackName\":\"SDP\",\"stackVersion\":\"1.0\",\"taskHosts\":[{\"disks\":[{\"sizeGB\":200,\"skuName\":\"StandardSSD_LRS\"}],\"hostName\":\"sdp-fceo5nkpvds-task-0001\",\"hostRole\":\"task\",\"osDiskSize\":100}]}\n";
        CreateClusterCmd cmd = JacksonUtils.toObj(json, CreateClusterCmd.class);
        System.out.println(JacksonUtils.toJson(cmd));
    }

    @Test
    void createTemplate() {
        // 准备数据
        CreateClusterTemplateCmd cmd = buildCreateClusterTemplateCmd();
        JacksonUtils.toJson(cmd);

        CreateClusterCmd cmd2 = new CreateClusterCmd();
        JacksonUtils.toJson(cmd2);

        // 测试
        ClusterTemplate template = new ClusterTemplate(cmd.getClusterName(), cmd.getBlueprint());
        template.initHostGroup(cmd.getHosts());

        template.initOverrideConfiguration(cmd.getConfigurations());

        template.computeConfiguration();

        // 验证结果
        System.out.println(JacksonUtils.toJson(template));

    }

    CreateClusterTemplateCmd buildCreateClusterTemplateCmd() {
        CreateClusterTemplateCmd cmd = new CreateClusterTemplateCmd();
        cmd.setClusterName("test0001");
        cmd.setHosts(buildNonHaHosts());
        cmd.setBlueprint(buildBlueprint());
        return cmd;
    }

    Map<HostGroupRole, List<String>> buildHosts() {
        Map<HostGroupRole, List<String>> hosts = new HashMap<>();
        List<String> master = new ArrayList<>();
        master.add("sunbox-dev-vm08.vmdns.sunbox.com");
//        master.add("master2");
        List<String> core = new ArrayList<>();
        core.add("sunbox-dev-vm10.vmdns.sunbox.com");
//        core.add("core2");
//        core.add("core3");
        List<String> task = new ArrayList<>();
        task.add("sunbox-dev-vm10.vmdns.sunbox.com");
//        task.add("task2");
//        task.add("task3");
//        task.add("task4");
        List<String> ambari = new ArrayList<>();
        ambari.add("ambari");

        hosts.put(HostGroupRole.MASTER, master);
        hosts.put(HostGroupRole.CORE, core);
//        hosts.put(HostGroupRole.TASK, task);
//        hosts.put(HostGroupRole.AMBARI, ambari);

        return hosts;
    }

    Map<HostGroupRole, List<String>> buildNonHaHosts() {
        Map<HostGroupRole, List<String>> hosts = new HashMap<>();
        List<String> master = new ArrayList<>();
        master.add("sunbox-dev-vm08.vmdns.sunbox.com");
        List<String> core = new ArrayList<>();
        core.add("sunbox-dev-vm09.vmdns.sunbox.com");
        List<String> task = new ArrayList<>();
        task.add("sunbox-dev-vm10.vmdns.sunbox.com");

        hosts.put(HostGroupRole.MASTER, master);
        hosts.put(HostGroupRole.CORE, core);
        hosts.put(HostGroupRole.TASK, task);
//        hosts.put(HostGroupRole.AMBARI, ambari);

        return hosts;
    }

    Blueprint buildBlueprint() {
        String configFilePath = "/data/home/wangda/work/01-doc/01-尚博信/08-SDP/05-项目资料/06-Ambari创建集群接口测试";
        byte[] file = new byte[0];
        try {
            file = Files.readAllBytes(Paths.get(configFilePath + "/blueprint.json"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String content = new String(file);
        Gson gson = new Gson();
//        Blueprint blueprint = gson.fromJson(content, Blueprint.class);
        Blueprint blueprint = JacksonUtils.toObj(content, Blueprint.class);
        blueprint.getBlueprints().setBlueprintName("single-node-blueprint");
        for (HostGroup hostGroup : blueprint.getHostGroups()) {
            if (hostGroup.getName().startsWith("AMBARI")) {
                hostGroup.setHostGroupRole(HostGroupRole.AMBARI);
            } else if (hostGroup.getName().startsWith("MASTER")) {
                hostGroup.setHostGroupRole(HostGroupRole.MASTER);
            } else if (hostGroup.getName().startsWith("CORE")) {
                hostGroup.setHostGroupRole(HostGroupRole.CORE);
            } else if (hostGroup.getName().startsWith("TASK")) {
                hostGroup.setHostGroupRole(HostGroupRole.TASK);
            };

        }
        return blueprint;
    }

    Map<String, Map<String, Object>> buildConfiguration() {
        Map<String, Map<String, Object>> config = new HashMap<>();
        // yarn-site
        Map<String, Object> yarnSiteMap = new HashMap<>();
        yarnSiteMap.put("yarnSiteMap", "aaaa");
        config.put("yarn-site", yarnSiteMap);

        // hdfs-site
        Map<String, Object> hdfsSiteMap = new HashMap<>();
        hdfsSiteMap.put("hdfsSiteMap", "bbbb");
        config.put("hdfs-site", hdfsSiteMap);

        // core-site
        Map<String, Object> coreSiteMap = new HashMap<>();
        coreSiteMap.put("coreSiteMap", "ccccc");
        config.put("core-site", coreSiteMap);

        return config;
    }

    @Test
    public void overrideHostGroupConfiguration() {
        Map<String,Object> configuration = new HashMap<>();
        configuration.put("yarn.timeline-service.leveldb-timeline-store.path", "/data/disk0/hadoop/yarn/timeline");
        configuration.put("yarn.timeline-service.leveldb-state-store.path", "/data/disk0/hadoop/yarn/timeline");

        Map<String, Map<String,Object>> configurationMap = new HashMap<>();
        configurationMap.put("yarn-site", configuration);

        ClusterTemplateHostGroup hostGroup = new ClusterTemplateHostGroup();
        hostGroup.setName("task-3");
        hostGroup.setConfigurations(Arrays.asList(configurationMap));

        ClusterTemplate template = new ClusterTemplate("test", buildBlueprint());
        template.getHostGroups().add(hostGroup);


        BlueprintConfiguration config = new BlueprintConfiguration();
        config.setConfigItemName("yarn-site");
        config.putProperties("yarn.timeline-service.leveldb-timeline-store.path", "/data/disk0/hadoop/yarn/timeline,/data/disk1/hadoop/yarn/timeline");
        config.putProperties("yarn.timeline-service.leveldb-state-store.path", "/data/disk0/hadoop/yarn/timeline,/data/disk1/hadoop/yarn/timeline");

        Map<String, BlueprintConfiguration> configMap = new HashMap<>();
        configMap.put(config.getConfigItemName(), config);

        // 测试
        template.overrideHostGroupConfiguration("task-3", configMap);

        // 校验
        Assertions.assertNotNull(template.getHostGroups());
    }

    @Test
    public void overrideHostGroupConfiguration_hostGroupConfigNotExists() {
        ClusterTemplateHostGroup hostGroup = new ClusterTemplateHostGroup();
        hostGroup.setName("task-3");
        hostGroup.setConfigurations(new ArrayList<>());

        ClusterTemplate template = new ClusterTemplate("test", buildBlueprint());
        template.getHostGroups().add(hostGroup);


        BlueprintConfiguration config = new BlueprintConfiguration();
        config.setConfigItemName("yarn-site");
        config.putProperties("yarn.timeline-service.leveldb-timeline-store.path", "/data/disk0/hadoop/yarn/timeline,/data/disk1/hadoop/yarn/timeline");
        config.putProperties("yarn.timeline-service.leveldb-state-store.path", "/data/disk0/hadoop/yarn/timeline,/data/disk1/hadoop/yarn/timeline");

        Map<String, BlueprintConfiguration> configMap = new HashMap<>();
        configMap.put(config.getConfigItemName(), config);

        // 测试
        template.overrideHostGroupConfiguration("task-3", configMap);

        // 校验
        Assertions.assertNotNull(template.getHostGroups());
    }
}