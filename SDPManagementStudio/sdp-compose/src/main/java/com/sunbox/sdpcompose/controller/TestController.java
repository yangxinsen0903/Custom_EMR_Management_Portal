package com.sunbox.sdpcompose.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.sunbox.domain.ConfClusterScript;
import com.sunbox.domain.InfoClusterOperationPlanActivityLogWithBLOBs;
import com.sunbox.domain.InfoClusterVm;
import com.sunbox.domain.ResultMsg;
import com.sunbox.domain.ambari.AmbariConfigItem;
import com.sunbox.domain.enums.SceneType;
import com.sunbox.domain.metaData.VMSku;
import com.sunbox.sdpcompose.mapper.AmbariConfigItemMapper;
import com.sunbox.sdpcompose.model.azure.request.*;
import com.sunbox.sdpcompose.service.*;
import com.sunbox.sdpcompose.service.ambari.*;
import com.sunbox.sdpcompose.service.ambari.blueprint.*;
import com.sunbox.sdpcompose.service.ambari.clustertemplate.ClusterTemplate;
import com.sunbox.sdpcompose.service.ambari.clustertemplate.CreateClusterTemplateCmd;
import com.sunbox.sdpcompose.service.ambari.enums.DBAppType;
import com.sunbox.sdpcompose.service.ambari.enums.HostGroupRole;
import com.sunbox.sdpcompose.service.ambari.enums.ProvisionAction;
import com.sunbox.sdpcompose.service.impl.AzureFleetServiceImpl;
import com.sunbox.sdpcompose.service.impl.AzureServiceImpl;
import com.sunbox.sdpcompose.service.impl.ClusterServiceImpl;
import com.sunbox.sdpcompose.service.impl.PlanExecServiceImpl;
import com.sunbox.sdpcompose.util.JacksonUtils;
import com.sunbox.service.AutoCreatedEvictVmService;
import com.sunbox.service.IVMClearLogService;
import com.sunbox.util.DistributedRedisLock;
import com.sunbox.util.KeyVaultUtil;
import io.jsonwebtoken.lang.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sunbox.sdp.ambari.client.api.ClustersApi;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


/**
 * @author : [niyang]
 * @className : TestController
 * @description : [描述说明该类的功能]
 * @createTime : [2022/11/30 3:28 PM]
 */
@RestController
@RequestMapping("/test")
public class TestController {

    Logger logger = LoggerFactory.getLogger(TestController.class);

    private String[] masterService = new String[]{
            "YARN",
            "ZOOKEEPER",
            "MAPREDUCE2",
            "HIVE",
            "HBASE",
            "SPARK3",
            "TEZ",
            "SQOOP"};

    @Autowired
    IMQProducerService mqProducerService;

    @Autowired
    private IAzureService iAzureService;

    @Autowired
    private IAmbariService ambariService;

    @Autowired
    private IVMService ivmService;

    @Autowired
    private DistributedRedisLock redisLock;

    @Autowired
    private IPlayBookService playBookService;

    @Autowired
    private AmbariConfigItemMapper configItemMapper;

    @Autowired
    private IAzureService azureService;

    @Autowired
    private ClusterServiceImpl clusterService;

    @Autowired
    private AzureFleetServiceImpl azureFleetService;

    @Autowired
    private KeyVaultUtil keyVaultUtil;

    @Autowired
    private IVMClearLogService vmClearLog;

    @Autowired
    private PlanExecServiceImpl planExecService;

    @Autowired
    private ThreadLocal<Map<String, Object>> threadLocal;

    @Autowired
    private AutoCreatedEvictVmService autoCreatedEvictVmService;

    @RequestMapping("/testEvictVm")
    public ResultMsg testEvictVm() {
        String param = "  {    \"name\": \"sdp-cluster-3-eastus2-group-1_c2342_18df47d9\",    \"hostName\": \"sdp-cluster-3-eastus2-group-1-6WFW0P\",     \"uniqueId\": \"d34e8bdf-bca3-4fe5-8ee2-b025c371d9db\",     \"privateIp\": \"10.0.0.20\",                \"zone\": \"1\",                             \"tags\": {        \"cluster\": \"sdp-test-070407\",            \"VirtualMachineProfileTimeCreated\": \"6/14/2024 12:18:49 AM +00:00\",        \"clusterName\": \"cluster-3-eastus2\",         \"name\": \"eric\",        \"SYS_CREATE_BATCH\": \"f47f499d0dc8842c8bf186e26b7032d7\",        \"hello\": \"sdp\",        \"SYS_SDP_CLUSTER\": \"cluster-3-eastus2\",        \"group\": \"group-1\",             \"SYS_SDP_GROUP\": \"group-1\"    },    \"priority\": \"Spot\",        \"vmState\": \"Creating\"}";
        ResultMsg resultMsg = autoCreatedEvictVmService.handleEvictVmEvent(param);
        System.out.println(JSON.toJSONString(resultMsg));

        return ResultMsg.SUCCESS();
    }


    @RequestMapping("/testQueryVmsCreateJob")
    public ResultMsg testQueryVmsCreateJob() {
        String param = "{\n" +
                        "    \"activity\": \"azureVMService@queryVmsAppendJob\",\n" +
                        "    \"planId\": \"d41e2035f2b242ef80a87f927d4f2178\",\n" +
                        "    \"clusterId\": \"71ffa1e3-bbf0-470c-8aca-6b244f0db6ef\",\n" +
                        "    \"cvm_jobid\": \"append-cluster-sdp-wd-test-47-95e9b3f3ce3052235931f3ee90c51796-20240720-022928-379\",\n" +
                        "    \"taskId\": \"0f675244-7d65-4dbc-9466-ee3d77df402a\",\n" +
                        "    \"activityLogId\": \"74d6a023654c43b0869b53e1a38037ff\"\n" +
                        "}";
        ResultMsg resultMsg = ivmService.queryVmsCreateJob(param);
        System.out.println(JSON.toJSONString(resultMsg));

        return ResultMsg.SUCCESS();
    }

    // 一定要自己测试一下:
    // 1. 正常创建
    // 2. 失败几台
    @RequestMapping("/testQueryVmsAppendJob")
    public ResultMsg queryVmsAppendJob() {
        String param = "{\n" +
                "    \"activity\": \"azureVMService@queryVmsAppendJob\",\n" +
                "    \"planId\": \"d41e2035f2b242ef80a87f927d4f2178\",\n" +
                "    \"clusterId\": \"71ffa1e3-bbf0-470c-8aca-6b244f0db6ef\",\n" +
                "    \"cvm_jobid\": \"append-cluster-sdp-wd-test-47-95e9b3f3ce3052235931f3ee90c51796-20240720-022928-379\",\n" +
                "    \"taskId\": \"0f675244-7d65-4dbc-9466-ee3d77df402a\",\n" +
                "    \"activityLogId\": \"74d6a023654c43b0869b53e1a38037ff\"\n" +
                "}";
        ResultMsg resultMsg = ivmService.queryVmsAppendJob(param);
        System.out.println(JSON.toJSONString(resultMsg));

        return ResultMsg.SUCCESS();
    }

    @RequestMapping("/testGetSameSku")
    public ResultMsg testGetSameSku() {
        List<VMSku> sameSpecVmSku = azureFleetService.getSameSpecVmSku("Standard_D4s_v5", "westus2", 3);
        System.out.println(JSON.toJSONString(sameSpecVmSku));

        return ResultMsg.SUCCESS();
    }
    @RequestMapping("/testTaskEvent")
    public ResultMsg testTaskEvent() {
        InfoClusterOperationPlanActivityLogWithBLOBs activityLog = new InfoClusterOperationPlanActivityLogWithBLOBs();
        activityLog.setState(-1);
//        activityLog.setPlanId("30bbd2cfa4bd4fa68d994feb5d487dc0"); // 手动缩容
        activityLog.setPlanId("301fcc7e3fd746418cbca48023654d44"); // 清理异常VM

        planExecService.saveTaskFailedEvent(activityLog);

        return ResultMsg.SUCCESS();
    }

    @RequestMapping("/testUpdateConfig")
    public ResultMsg testUpdateConfig() {

        AmbariInfo ambariInfo = AmbariInfo.of("http://20.172.10.47:8765/api/v1", "admin", "admin");

        List<BlueprintConfiguration> clusterDefaultConfigs = new ArrayList<>();
        BlueprintConfiguration coreSite = new BlueprintConfiguration();
        coreSite.setConfigItemName("core-site");
        coreSite.putProperties("fs.trash.interval", "480");

        BlueprintConfiguration yarnSite = new BlueprintConfiguration();
        yarnSite.setConfigItemName("yarn-site");
        yarnSite.putProperties("yarn.resourcemanager.system-metrics-publisher.dispatcher.pool-size", "16");
        yarnSite.putProperties("yarn.log-aggregation.retain-seconds", "6400");

        clusterDefaultConfigs.add(coreSite);
        clusterDefaultConfigs.add(yarnSite);


        InstanceGroupConfiguration config = new InstanceGroupConfiguration("e27ec3be-e0b1-4c01-92ef-768d801d6409");
        Map<String, String> configs = new HashMap<>();
        configs.put("yarn.client.nodemanager-connect.retry-interval-ms", "10240");
        configs.put("yarn.log-aggregation.retain-seconds", "6400");
        config.putConfig("yarn-site", configs);

        List<InstanceGroupConfiguration> groupConfigs = new ArrayList<>();
        groupConfigs.add(config);

        ambariService.updateClusterConfig(ambariInfo,  "95ffd3ee-c3dd-4ca7-a52a-c76937205e24", null, clusterDefaultConfigs);

        return new ResultMsg();
    }

    @RequestMapping("/delete")
    public boolean deleteKv(String kvname,String endpoint){
        return keyVaultUtil.delSecret(kvname,endpoint);
    }

    @RequestMapping("/miList")
    public ResultMsg testAzureMIList(@RequestParam String region) {
        ResultMsg result = new ResultMsg();
        AzureServiceImpl impl = (AzureServiceImpl) azureService;
        impl.setAzureUrl("http://20.125.69.82:8081");
        ResultMsg miList = impl.getMIList(region);
        result.setData(miList.getData());
        return result;
    }


    @RequestMapping("/addclog")
    public ResultMsg addclog() {
        List<String> vmName = new ArrayList<>();
        vmName.add("sdf");
        //debugger.debugMapperXMLFiles();
       vmClearLog.insertClearHosts("123123","234",vmName,"");
       return new ResultMsg();
    }



    @RequestMapping("/downloadBlueprint")
    public ResultMsg downloadBlueprint(HttpServletResponse response) throws Exception {
        ResultMsg result = new ResultMsg();
        AmbariInfo ambariInfo = AmbariInfo.of("http://20.118.166.170:8080/api/v1", "admin", "admin");
        ClustersApi api = new ClustersApi(ambariInfo.getAmbariApiClient());
        String blueprintStr = api.getBlueprintString("sunboxTest1");
        result.setData(blueprintStr);
        response.setHeader("Content-Description","File Transfer");
        response.setHeader("Content-Type", "application/octet-stream");
        response.setHeader("Content-Disposition","attachment; filename=blueprint.json");
        response.setHeader("Expires","0");
        response.setHeader("Cache-Control","must-revalidate, post-check=0, pre-check=0");
        response.setHeader("Pragma","public");
//        response.setHeader("Content-Length: ' . filesize($name));
        response.setHeader("Content-Transfer-Encoding","binary");
        response.getOutputStream().write(blueprintStr.getBytes());
        response.getOutputStream().flush();
        return result;
    }

    @GetMapping("/createCluster")
    public ResultMsg createCluster(@RequestParam("services")String serviceStr ) {
        String[] services = serviceStr.split(",");
        CreateClusterCmd cmd = new CreateClusterCmd();
        cmd.setAmbarInfo(AmbariInfo.of("http://20.172.10.47:8765/api/v1", "admin", "admin"));
        cmd.setStackName("SDP");
        cmd.setStackVersion("1.0");
        cmd.setHa(true);
        cmd.setServices(Lists.newArrayList(services));
        cmd.setBlueprintName("test0001");
        cmd.setClusterId("95ffd3ee-c3dd-4ca7-a52a-c76937205e24");

//        cmd.setAmbariHosts(Lists.newArrayList(new HostInstance()));
//        cmd.setMasterHosts(Lists.newArrayList(new HostInstance(), new HostInstance()));
//        cmd.setCoreHosts(Lists.newArrayList(new HostInstance()));
//        cmd.setTaskHosts(Lists.newArrayList(new HostInstance()));

        if (serviceStr.indexOf("HIVE") > 0) {
            DBConnectInfo connectInfo = new DBConnectInfo();
            connectInfo.setAppType(DBAppType.HIVE_SITE).setConnectionUrl("jdbc:mysql://sunboxdev.mysql.database.azure.com:3306/lang?serverTimezone=Asia/Shanghai&useSSL=false&useUnicode=true&characterEncoding=UTF-8")
                    .setDriverClassName("com.mysql.jdbc.Driver")
                    .setUserName("lang")
                    .setDbName("lang")
                    .setPassword("sunbox@TBD7");
            cmd.addDBConnectInfo(connectInfo);

            connectInfo = new DBConnectInfo();
            connectInfo.setAppType(DBAppType.HIVE_ENV)
                    .setDbName("lang");
            cmd.addDBConnectInfo(connectInfo);
        }

        ambariService.createCluster(cmd);

        ResultMsg resultMsg = new ResultMsg();
        Map<String, String> result = new HashMap<>();
        resultMsg.setData(result);
        return resultMsg;
    }

    @GetMapping("/createCluster2")
    public ResultMsg createCluster2() {
        clusterService.testCreateCluster("95ffd3ee-c3dd-4ca7-a52a-c76937205e24");

        ResultMsg resultMsg = new ResultMsg();
        Map<String, String> result = new HashMap<>();
        resultMsg.setData(result);
        return resultMsg;
    }

    @GetMapping("/duplicateCluster")
    public ResultMsg duplicateCluster(@RequestParam("services")String serviceStr ) {
        String[] services = serviceStr.split(",");
        DuplicateClusterCmd cmd = new DuplicateClusterCmd();
        cmd.setOriginClusterName("sunbox");
        cmd.setAmbarInfo(AmbariInfo.of("http://20.125.124.59:8080/api/v1", "admin", "admin"));
        cmd.setOriginAmbariInfo(AmbariInfo.of("http://20.118.166.170:8080/api/v1", "admin", "admin"));
        cmd.setStackName("SDP");
        cmd.setStackVersion("1.0");
        cmd.setHa(true);
        cmd.setServices(Lists.newArrayList(services));
        cmd.setBlueprintName("test0001");

//        cmd.setAmbariHosts(Lists.newArrayList(new HostInstance()));
//        cmd.setMasterHosts(Lists.newArrayList(new HostInstance(), new HostInstance()));
//        cmd.setCoreHosts(Lists.newArrayList(new HostInstance()));
//        cmd.setTaskHosts(Lists.newArrayList(new HostInstance()));

        if (serviceStr.indexOf("HIVE") > 0) {
            DBConnectInfo connectInfo = new DBConnectInfo();
            connectInfo.setAppType(DBAppType.HIVE_SITE).setConnectionUrl("jdbc:mysql://sunboxdev.mysql.database.azure.com:3306/lang?serverTimezone=Asia/Shanghai&useSSL=false&useUnicode=true&characterEncoding=UTF-8")
                    .setDriverClassName("com.mysql.jdbc.Driver")
                    .setUserName("lang")
                    .setDbName("lang")
                    .setPassword("sunbox@TBD7");
            cmd.addDBConnectInfo(connectInfo);

            connectInfo = new DBConnectInfo();
            connectInfo.setAppType(DBAppType.HIVE_ENV)
                    .setDbName("lang");
            cmd.addDBConnectInfo(connectInfo);
        }

        ambariService.duplicateCluster(cmd);

        ResultMsg resultMsg = new ResultMsg();
        Map<String, String> result = new HashMap<>();
        resultMsg.setData(result);
        return resultMsg;
    }

    @RequestMapping("/blueprint")
    public ResultMsg createBlueprint() {
        CreateBlueprintCmd cmd = new CreateBlueprintCmd();
        cmd.setStackName("SDP");
        cmd.setStackVersion("1.0");
        cmd.setHa(true);
        cmd.setServices(Lists.asList("HDFS", masterService));
        HostInstance host = new HostInstance();
        host.setHostName("10.4.0.39");
//        cmd.setAmbariHosts(Lists.newArrayList(host));
//        cmd.setMasterHosts(Lists.newArrayList(host));
//        cmd.setCoreHosts(Lists.newArrayList(new HostInstance()));
//        cmd.setTaskHosts(Lists.newArrayList(new HostInstance()));

        DBConnectInfo connectInfo = new DBConnectInfo();
        connectInfo.setAppType(DBAppType.HIVE_SITE)
                .setConnectionUrl("jdbc:mysql://helloworld")
                .setDriverClassName("com.mysql.Driver")
                .setUserName("root").setPassword("password");
        cmd.addDBConnectInfo(connectInfo);

        Blueprint blueprint = ambariService.createBlueprint(cmd);

        logger.info("=============== Blueprint ===================");
        logger.info(JacksonUtils.toJson(blueprint));
        logger.info("================ Blueprint End ==================");

        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setMsg(JacksonUtils.toJson(blueprint));
        return resultMsg;
    }

    @RequestMapping("/hbase-blueprint")
    public ResultMsg createHBaseBlueprint() {
        CreateBlueprintCmd cmd = new CreateBlueprintCmd();
        cmd.setStackName("SDP");
        cmd.setStackVersion("1.0");
        cmd.setHa(true);
        cmd.setScene(SceneType.HBASE);
        cmd.setServices(Lists.asList("HDFS", masterService));
//        cmd.setAmbariHosts(Lists.newArrayList(new HostInstance()));
//        cmd.setMasterHosts(Lists.newArrayList(new HostInstance()));
        cmd.setMiClientId("miClientId");
        cmd.setMiTenantId("miTenantId");

        HostInstance host = new HostInstance();
        host.getDisks().add(new DiskInfo());
        host.getDisks().add(new DiskInfo());

        HostInstance host2 = new HostInstance();
        host.getDisks().add(new DiskInfo());
        host.getDisks().add(new DiskInfo());

        HostInstance host3 = new HostInstance();
        host.getDisks().add(new DiskInfo());
        host.getDisks().add(new DiskInfo());

//        cmd.setCoreHosts(Lists.newArrayList(host, host2, host3));
//        cmd.setTaskHosts(Lists.newArrayList(new HostInstance()));

        DBConnectInfo connectInfo = new DBConnectInfo();
        connectInfo.setAppType(DBAppType.HIVE_SITE)
                .setConnectionUrl("jdbc:mysql://helloworld")
                .setDriverClassName("com.mysql.Driver")
                .setUserName("root").setPassword("password");
        cmd.addDBConnectInfo(connectInfo);

        Blueprint blueprint = ambariService.createBlueprint(cmd);

        logger.info("=============== Blueprint ===================");
        logger.info(JacksonUtils.toJson(blueprint));
        logger.info("================ Blueprint End ==================");

        ResultMsg resultMsg = new ResultMsg();
        resultMsg.setMsg(JacksonUtils.toJson(blueprint));
        return resultMsg;
    }

    @RequestMapping("/blueprint-non-ha")
    public ResultMsg createBlueprint1(@RequestParam("services")String serviceStr  ) {
        masterService = new String[]{"HDFS",
                "YARN",
                "ZOOKEEPER",
                "MAPREDUCE2",
//                "HIVE",
//                "HBASE",
//                "SPARK3",
//                "TEZ"
        };
        String[] services = serviceStr.split(",");
        CreateBlueprintCmd cmd = new CreateBlueprintCmd();
        cmd.setStackName("SDP");
        cmd.setStackVersion("1.0");
        cmd.setHa(false);
        cmd.setServices(Lists.newArrayList(services));
        cmd.setBlueprintName("test0001");

//        cmd.setAmbariHosts(Lists.newArrayList(new HostInstance()));
//        cmd.setMasterHosts(Lists.newArrayList(new HostInstance()));
//        cmd.setCoreHosts(Lists.newArrayList(new HostInstance()));
//        cmd.setTaskHosts(Lists.newArrayList(new HostInstance()));

        if (serviceStr.indexOf("HIVE") > 0) {
            DBConnectInfo connectInfo = new DBConnectInfo();
            connectInfo.setAppType(DBAppType.HIVE_SITE).setConnectionUrl("jdbc:mysql://sunboxdev.mysql.database.azure.com:3306/lang?serverTimezone=Asia/Shanghai&useSSL=false&useUnicode=true&characterEncoding=UTF-8")
                    .setDriverClassName("com.mysql.jdbc.Driver")
                    .setUserName("lang")
                    .setPassword("sunbox@TBD7");
            cmd.addDBConnectInfo(connectInfo);

            connectInfo = new DBConnectInfo();
            connectInfo.setAppType(DBAppType.HIVE_ENV)
                    .setDbName("lang");
            cmd.addDBConnectInfo(connectInfo);
        }

        Blueprint blueprint = ambariService.createBlueprint(cmd);


        logger.info("=============== Blueprint ===================");
        logger.info(JacksonUtils.toJson(blueprint));
        logger.info("================ Blueprint End ==================");


        ClusterTemplate clusterTemplate = buildClusterTemplate(blueprint);
        logger.info("=============== CreateClusterTemplate ===================");
        logger.info(JacksonUtils.toJson(clusterTemplate));
        logger.info("================ CreateClusterTemplate End ==================");

        ResultMsg resultMsg = new ResultMsg();
        Map<String, String> result = new HashMap<>();
        result.put("Blueprint", JacksonUtils.toJson(blueprint));
        result.put("ClusterTemplate", JacksonUtils.toJson(clusterTemplate));
        resultMsg.setData(result);
        return resultMsg;
    }

    @RequestMapping("/enableClusterAutoStart")
    public String enableClusterAutoStart(@RequestParam("clusterName") String clusterName, @RequestParam("services") String services) {
        List<String> serviceList = Arrays.asList(services.split(","));
        AmbariInfo ambariInfo = AmbariInfo.of("http://20.118.166.170:8080/api/v1", "admin", "admin");

        ambariService.enableClusterAutoStart(ambariInfo, clusterName, serviceList);

        return "success";
    }

    /*@RequestMapping("/getvmlistByHostName")
    public List<InfoClusterVm> getvmlistByHostName() {
        List<String> hostNames = new ArrayList<>();
        hostNames.add("sdp-leo-cs10001-task-0002.sdp-sit.isd-dev-aks.com");
        return ivmService.getVMListByHostNames("007c56c4-3ac8-46c4-9c77-b8059fa07af3",hostNames);
    }*/

    private ClusterTemplate buildClusterTemplate(Blueprint blueprint) {
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

        CreateClusterTemplateCmd cmd = new CreateClusterTemplateCmd();
        cmd.setClusterName(blueprint.getBlueprints().getBlueprintName());
        cmd.setHosts(hosts);
        cmd.setBlueprint(blueprint);

        // 测试
        ClusterTemplate template = new ClusterTemplate(cmd.getClusterName(), cmd.getBlueprint());
        template.initHostGroup(cmd.getHosts());

        template.initOverrideConfiguration(cmd.getConfigurations());

        template.computeConfiguration();

        return template;
    }

    @RequestMapping("/startCluster")
    public ResultMsg startCluster() {
        byte[] file = new byte[0];
        try {
            file = Files.readAllBytes(Paths.get("/data/home/wangda/work/01-doc/01-尚博信/08-SDP/05-项目资料/06-Ambari创建集群接口测试/clusterTemplate.json"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String content = new String(file);
        ClusterTemplate clusterTemplate = JacksonUtils.toObj(content, ClusterTemplate.class);
        clusterTemplate.setProvisionAction(ProvisionAction.START_ONLY.name());
        AmbariInfo ambariInfo = AmbariInfo.of("http://20.118.166.170:8080/api/v1", "admin", "admin");
        CreateClusterCmd cmd = new CreateClusterCmd();
        cmd.setClusterName(clusterTemplate.getCluster().getClusterName());
        cmd.setAmbarInfo(ambariInfo);
        InProgressResult createClusterResult = ambariService.startAllClusterServices(ambariInfo, clusterTemplate.getCluster().getClusterName(),"");
        logger.info(JacksonUtils.toJson(createClusterResult));
        return new ResultMsg();
    }


    @RequestMapping("/startHACluster")
    public ResultMsg startHAClusterr() {
        AmbariInfo ambariInfo = AmbariInfo.of("http://20.125.124.59:8080/api/v1", "admin", "admin");
        InProgressResult sunbox = ambariService.startAllClusterServicesHA(ambariInfo, "sunbox","");

        ResultMsg msg = new ResultMsg();
        msg.setData(sunbox);
        return msg;
    }

    @RequestMapping("/restartService")
    public ResultMsg restartService() {
        AmbariInfo ambariInfo = AmbariInfo.of("http://20.125.124.59:8080/api/v1", "admin", "admin");
        List<String> hosts = Arrays.asList("sunbox-dev-vm12.vmdns.sunbox.com",
                "sunbox-dev-vm13.vmdns.sunbox.com",
                "sunbox-dev-vm14.vmdns.sunbox.com",
                "sunbox-dev-vm15.vmdns.sunbox.com",
                "sunbox-dev-vm16.vmdns.sunbox.com");
        InProgressResult result = ambariService.restartService(ambariInfo, "sunbox", "HDFS");

        ResultMsg msg = new ResultMsg();
        msg.setData(result);
        return msg;
    }

    @RequestMapping("/importBlueprint")
    public ResultMsg importBlueprint() {
        // 加载Blueprint文件.
        Blueprint blueprint = readBlueprintSwaggerFromFile();

        Map<String, String> typeMapping = buildMapping();
        Set<String> nameSet = new HashSet<>();
        // 遍历配置,检查是否存在,如果存在,就如果不存在就保存.
        for (Map<String, BlueprintConfiguration> map : blueprint.getConfigurations()) {
            for (Map.Entry<String, BlueprintConfiguration> config : map.entrySet()) {
                String configType = config.getKey();
                nameSet.add(configType);
                for (Map.Entry<String, Object> item : config.getValue().getProperties().entrySet()) {
                    String s = configType + "\t\t" + item.getKey() + "\t ====>>>> " + item.getValue();
                    String service = typeMapping.get(configType);
                    AmbariConfigItem  cfgItem = new AmbariConfigItem();
                    cfgItem.setKey(item.getKey());
                    cfgItem.setConfigTypeCode(configType);
                    List<AmbariConfigItem> list = configItemMapper.queryAllByLimit(cfgItem, PageRequest.of(0, 10));
                    if (list.size() == 0) {
                        cfgItem = new AmbariConfigItem();
                        cfgItem.setServiceCode(typeMapping.get(configType));
                        cfgItem.setStackCode("SDP-1.0");
                        cfgItem.setComponentCode("NULL");
                        cfgItem.setConfigTypeCode(configType);
                        cfgItem.setKey(item.getKey());
                        cfgItem.setValue(String.valueOf(item.getValue()));
                        cfgItem.setIsDynamic(0);
                        cfgItem.setIsContentProp(Objects.equals(item.getKey(), "content")? 1: 0);
                        cfgItem.setItemType("NON_HA");
                        cfgItem.setState("VALID");
                        cfgItem.setCreatedBy("system");
                        cfgItem.setUpdatedBy("system");
                        cfgItem.setCreatedTime(new Date());
                        cfgItem.setUpdatedTime(new Date());
                        System.out.println(s);
                        configItemMapper.insert(cfgItem);
                    } else {
                        System.out.println(item.getKey() + "已经存在, 不进行添加");
                    }
                }
            }
        }

        nameSet.stream().forEach( s -> {
            System.out.println(s);
        });

        return new ResultMsg();
    }

    @RequestMapping("/importHbaseBlueprint")
    public ResultMsg importHBaseBlueprint() {
        Set<String> fileSet = new HashSet<>();
        fileSet.add("yarn-hbase-env");
        fileSet.add("yarn-hbase-log4j");
        fileSet.add("yarn-hbase-policy");
        fileSet.add("yarn-hbase-site");
        fileSet.add("hbase-atlas-application-properties");
        fileSet.add("hbase-env");
        fileSet.add("hbase-log4j");
        fileSet.add("hbase-policy");
        fileSet.add("hbase-site");
        fileSet.add("ranger-hbase-audit");
        fileSet.add("ranger-hbase-plugin-properties");
        fileSet.add("ranger-hbase-policymgr-ssl");
        fileSet.add("ranger-hbase-security");

        // 加载Blueprint文件.
        Blueprint blueprint = readHBaseBlueprintSwaggerFromFile();

        Map<String, String> typeMapping = buildMapping();
        Set<String> nameSet = new HashSet<>();
        // 遍历配置,检查是否存在,如果存在,就如果不存在就保存.
        for (Map<String, BlueprintConfiguration> map : blueprint.getConfigurations()) {
            for (Map.Entry<String, BlueprintConfiguration> config : map.entrySet()) {
                String configType = config.getKey();
                if (!fileSet.contains(configType)) {
                    break;
                }
                nameSet.add(configType);
                for (Map.Entry<String, Object> item : config.getValue().getProperties().entrySet()) {
                    String s = configType + "\t\t" + item.getKey() + "\t ====>>>> " + item.getValue();
                    String service = typeMapping.get(configType);
                    AmbariConfigItem  cfgItem = new AmbariConfigItem();
                    cfgItem.setKey(item.getKey());
                    cfgItem.setConfigTypeCode(configType);
                    List<AmbariConfigItem> list = configItemMapper.queryAllByLimit(cfgItem, PageRequest.of(0, 10));
                    if (list.size() == 0) {
                        cfgItem = new AmbariConfigItem();
                        cfgItem.setServiceCode(typeMapping.get(configType));
                        cfgItem.setStackCode("SDP-1.0");
                        cfgItem.setComponentCode("NULL");
                        cfgItem.setConfigTypeCode(configType);
                        cfgItem.setKey(item.getKey());
                        cfgItem.setValue(String.valueOf(item.getValue()));
                        cfgItem.setIsDynamic(0);
                        cfgItem.setIsContentProp(Objects.equals(item.getKey(), "content")? 1: 0);
                        cfgItem.setItemType("NON_HA");
                        cfgItem.setState("VALID");
                        cfgItem.setCreatedBy("system");
                        cfgItem.setUpdatedBy("system");
                        cfgItem.setCreatedTime(new Date());
                        cfgItem.setUpdatedTime(new Date());
                        System.out.println(s);
                        configItemMapper.insert(cfgItem);

                        cfgItem = new AmbariConfigItem();
                        cfgItem.setServiceCode(typeMapping.get(configType));
                        cfgItem.setStackCode("SDP-1.0");
                        cfgItem.setComponentCode("NULL");
                        cfgItem.setConfigTypeCode(configType);
                        cfgItem.setKey(item.getKey());
                        cfgItem.setValue(String.valueOf(item.getValue()));
                        cfgItem.setIsDynamic(0);
                        cfgItem.setIsContentProp(Objects.equals(item.getKey(), "content")? 1: 0);
                        cfgItem.setItemType("HA");
                        cfgItem.setState("VALID");
                        cfgItem.setCreatedBy("system");
                        cfgItem.setUpdatedBy("system");
                        cfgItem.setCreatedTime(new Date());
                        cfgItem.setUpdatedTime(new Date());
                        System.out.println(s);
                        configItemMapper.insert(cfgItem);
                    } else {
                        System.out.println(item.getKey() + "已经存在, 不进行添加");
                    }
                }
            }
        }

        nameSet.stream().forEach( s -> {
            System.out.println(s);
        });

        return new ResultMsg();
    }

    private Blueprint readHBaseBlueprintSwaggerFromFile() {
        byte[] file = new byte[0];
        try {
            file = Files.readAllBytes(Paths.get("/home/wangda/Desktop/blueprint.json"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String content = new String(file);
        Blueprint blueprint = JacksonUtils.toObj(content, Blueprint.class);
        return blueprint;
    }

    private Blueprint readBlueprintSwaggerFromFile() {
        byte[] file = new byte[0];
        try {
            file = Files.readAllBytes(Paths.get("/data/home/wangda/work/01-doc/01-尚博信/08-SDP/05-项目资料/06-Ambari创建集群接口测试/blueprint-all.json"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String content = new String(file);
        Blueprint blueprint = JacksonUtils.toObj(content, Blueprint.class);
        return blueprint;
    }

    @RequestMapping("/test")
    public ResultMsg selectAbnormalCard(){
        String a = "{\"activity\":\"clusterservice@querySDPClusterInstallProcess\",\"clusterName\":\"sdp-Au7uTusj271\",\"ambariInfo\":{\"ambariApiClient\":{\"authentications\":{\"httpBasicAuth\":{\"password\":\"admin\",\"username\":\"admin\"}},\"basePath\":\"http://10.2.0.10:8080/api/v1\",\"connectTimeout\":10000,\"debugging\":false,\"httpClient\":{\"connectTimeout\":10000,\"dispatcher\":{\"executorService\":{\"activeCount\":0,\"completedTaskCount\":0,\"corePoolSize\":0,\"largestPoolSize\":0,\"maximumPoolSize\":2147483647,\"poolSize\":0,\"queue\":[],\"rejectedExecutionHandler\":{},\"shutdown\":false,\"taskCount\":0,\"terminated\":false,\"terminating\":false,\"threadFactory\":{}},\"maxRequests\":64,\"maxRequestsPerHost\":5,\"queuedCallCount\":0,\"runningCallCount\":0},\"followRedirects\":true,\"followSslRedirects\":true,\"readTimeout\":120000,\"retryOnConnectionFailure\":true,\"writeTimeout\":30000},\"jSON\":{\"gson\":{}},\"readTimeout\":120000,\"verifyingSsl\":true,\"writeTimeout\":30000}},\"install_cluster_app_jobid\":1,\"activityLogId\":\"86ce0e6f12de4f5f927b35718559110b\"}";
        mqProducerService.sendMessage("p1",a);
        return null;
    }

    @GetMapping("/getSubnet")
    public ResultMsg getSubnet(@RequestParam String region){
        return iAzureService.getSubnet(region);
    }

    @PostMapping("/geVmSkus")
    public ResultMsg geVmSkus(@RequestParam String region){
        return iAzureService.geVmSkus(region);
    }

    @GetMapping("/getDiskSku")
    public ResultMsg getDiskSku(@RequestParam String region){
        return iAzureService.getDiskSku(region);
    }

    @PostMapping("/getSSHKeyPair")
    public ResultMsg getSSHKeyPair(@RequestParam String region){
        return iAzureService.getSSHKeyPair(region);
    }

    @GetMapping("/getNSGSku")
    public ResultMsg getNSGSku(@RequestParam String region){
        return iAzureService.getNSGSku(region);
    }

    @PostMapping("/createVms")
    public ResultMsg createVms(@RequestBody String reqStr){
        AzureVmsRequest azureVmsRequest = JSON.parseObject(reqStr, AzureVmsRequest.class);
        return iAzureService.createVms(azureVmsRequest);
    }

    @GetMapping("/getJobsStatus")
    public ResultMsg getJobsStatus(@RequestParam String jobId,@RequestParam String region) {
        return iAzureService.getJobsStatus(jobId,region);
    }

    @PostMapping("/executeJobPlaybook")
    public ResultMsg executeJobPlaybook(@RequestBody String reqStr){
        AzureExecuteJobPlaybookRequest azureExecuteJobPlaybookRequest = JSON.parseObject(reqStr, AzureExecuteJobPlaybookRequest.class);
        return iAzureService.executeJobPlaybook(azureExecuteJobPlaybookRequest);
    }

    @PostMapping("/queryPlaybookExecuteResult")
    public ResultMsg queryPlaybookExecuteResult(@RequestParam String transactionId,
                                                @RequestParam String subscriptionId,
                                                @RequestParam String keyVaultResourceName,
                                                @RequestParam String secretResourceId){
        return iAzureService.queryPlaybookExecuteResult(transactionId,subscriptionId,keyVaultResourceName,secretResourceId);
    }

    @PostMapping("/ruinCluster")
    public ResultMsg ruinCluster(@RequestParam String clusterName,@RequestParam String region){
        return iAzureService.ruinCluster(clusterName,region);
    }

    @RequestMapping("/cvmtest")
    public ResultMsg createvms2(@RequestParam String message){
        return ivmService.createVms("{\"planId\":\"\",\"clusterId\":\"db5f6178-9a39-4542-8e11-2f6a0ada08df\"," +
                "\"transactionId\":\"db5f6178-9a39-4542-8e11-2f6a0ada08df\"}");
    }


    @RequestMapping("/lock")
    public ResultMsg locktest( ){
        boolean lock=redisLock.tryLock("test");
        if (lock){
            redisLock.unlock("test");
            return new ResultMsg();
        }else{
            return null;
        }
    }

    @RequestMapping("/generatePlaybookUri")
    public ResultMsg generatePlaybookUri() {
        List<ConfClusterScript> list = new ArrayList<>();
        ConfClusterScript script1 = new ConfClusterScript();
        script1.setScriptName("scriptName1");
        script1.setScriptPath("/usr/local/tomcat");
        list.add(script1);
        ConfClusterScript script2 = new ConfClusterScript();
        script2.setScriptName("scriptName2");
        script2.setScriptPath("/home/java/1.8");
        list.add(script2);
//        playBookService.getPlaybookYmlUri(list, "remoteUser_1", "playbook.yml");
        return new ResultMsg();
    }

    /**
     * 创建资源组
     * @return resultMsg
     */
    @RequestMapping(value = "/createResourceGroup", method = RequestMethod.POST)
    public ResultMsg createResourceGroup(@RequestBody AzureResourceGroupTagsRequest azureResourceGroupTagsRequest) {
        ResultMsg resultMsg = iAzureService.createResourceGroup(azureResourceGroupTagsRequest);
        return resultMsg;
    }

    /**
     * 查看资源组
     * @param clusterId
     * @return resultMsg
     */
    @RequestMapping(value = "/getResourceGroup", method = RequestMethod.GET)
    public ResultMsg getResourceGroup(@RequestParam("clusterId") String clusterId,@RequestParam String region) {
        ResultMsg resultMsg = iAzureService.getResourceGroup(clusterId,region);
        return resultMsg;
    }

    /**
     * 更新资源组标签-全量
     * @param azureResourceGroupTagsRequest
     * @return resultMsg
     */
    @RequestMapping(value = "/updateResourceGroupTags", method = RequestMethod.POST)
    public ResultMsg updateResourceGroupTags(@RequestBody AzureResourceGroupTagsRequest azureResourceGroupTagsRequest) {
        ResultMsg resultMsg = iAzureService.updateResourceGroupTags(azureResourceGroupTagsRequest);
        return resultMsg;
    }

    /**
     * 更新资源组标签-增量
     * @param azureResourceGroupAddTagsRequest
     * @return resultMsg
     */
    @RequestMapping(value = "/addResourceGroupTags", method = RequestMethod.POST)
    public ResultMsg addResourceGroupTags(@RequestBody AzureResourceGroupAddTagsRequest azureResourceGroupAddTagsRequest) {
        ResultMsg resultMsg = iAzureService.addResourceGroupTags(azureResourceGroupAddTagsRequest);
        return resultMsg;
    }

    /**
     * 删除资源组标签
     * @param azureResourceGroupAddTagsRequest
     * @return resultMsg
     */
    @RequestMapping(value = "/deleteResourceGroupTags", method = RequestMethod.POST)
    public ResultMsg deleteResourceGroupTags(@RequestBody AzureResourceGroupAddTagsRequest azureResourceGroupAddTagsRequest) {
        ResultMsg resultMsg = iAzureService.deleteResourceGroupTags(azureResourceGroupAddTagsRequest);
        return resultMsg;
    }

    /**
     * 查询VM Sku列表增加HBase主机的NVme信息
     * @return resultMsg
     */
    @RequestMapping(value = "/metas/supportedVMSkuList", method = RequestMethod.GET)
    public ResultMsg supportedVMSkuList(@RequestParam String region) {
        ResultMsg resultMsg = iAzureService.supportedVMSkuList(region);
        return resultMsg;
    }

    /**
     * 单个创建VM实例
     * @param azureVMInstanceRequest
     * @return resultMsg
     */
    @RequestMapping(value = "/createVMInstance", method = RequestMethod.POST)
    public ResultMsg createVMInstance(@RequestBody AzureVMInstanceRequest azureVMInstanceRequest) {
        ResultMsg resultMsg = iAzureService.createVMInstance(azureVMInstanceRequest);
        return resultMsg;
    }

    /**
     * 单个删除VM实例
     * @return
     */
    @RequestMapping(value = "/deleteVMInstance", method = RequestMethod.GET)
    public ResultMsg deleteVMInstance(@RequestParam("vmName") String vmName,@RequestParam String region) {
        ResultMsg resultMsg = iAzureService.deleteVMInstance(vmName, "", region);
        return resultMsg;
    }

    /**
     * 批量/单个VM扩容磁盘
     * @return
     */
    @RequestMapping(value = "/updateVirtualMachinesDiskSize", method = RequestMethod.POST)
    public ResultMsg updateVirtualMachinesDiskSize(@RequestBody AzureUpdateVirtualMachinesDiskSizeRequest request) {
        ResultMsg resultMsg = iAzureService.updateVirtualMachinesDiskSize(request);
        return resultMsg;
    }

    /**
     * 获取AZ列表
     */
    @RequestMapping(value = "/getazlist")
    public ResultMsg getAzList(@RequestParam String region) {
        ResultMsg resultMsg = iAzureService.getAzList(region);
        return resultMsg;
    }

    /**
     * 查询vmSku价格
     */
    @RequestMapping(value = "/getInstancePrice")
    public ResultMsg getInstancePrice(@RequestParam("skuName") String skuName,@RequestParam String region) {
        ResultMsg resultMsg = iAzureService.getInstancePrice(skuName,region);
        return resultMsg;
    }

    /**
     * 竞价实例驱逐率
     */
    @PostMapping(value = "/spotEvictionRate")
    public ResultMsg spotEvictionRate(@RequestBody List<String> skuNames,@RequestParam String region) {
        ResultMsg resultMsg = iAzureService.spotEvictionRate(skuNames,region);
        return resultMsg;
    }

    /**
     * 竞价实例历史价格
     */
    @PostMapping(value = "/spotPriceHistory")
    public ResultMsg spotPriceHistory(@RequestBody List<String> skuNames,@RequestParam String region) {
        ResultMsg resultMsg = iAzureService.spotPriceHistory(skuNames,region);
        return resultMsg;
    }

    @GetMapping(value = "/provisionDetail")
    public ResultMsg provisionDetail(@RequestParam("jobId") String jobId,@RequestParam("region") String region) {
        ResultMsg resultMsg = iAzureService.provisionDetail(jobId,region);
        return resultMsg;
    }

    /**
     * 磁盘扩容
     */
    @PostMapping(value = "/addPart")
    public ResultMsg addPart(@RequestBody AzureAddPartRequest addPartRequest) {
        ResultMsg resultMsg = iAzureService.addPart(addPartRequest);
        return resultMsg;
    }

    @GetMapping("/restartHostsComponents")
    public ResultMsg restartHostsComponents() {
        AmbariInfo ambariInfo = AmbariInfo.of("http://20.172.10.47:8765/api/v1", "admin", "admin");
        String clusterName = "sdpWeWt1KOMBQP";
        String clusterId = "8475812e-91f4-4243-a57e-a1c755388b23";
        String groupName = "master";
        String serviceName = "YARN";
        InProgressResult result = ambariService.restartHostsComponents(ambariInfo, clusterId, serviceName, groupName);
        ResultMsg resultMsg = ResultMsg.SUCCESS();
        resultMsg.setData(result);
        return resultMsg;
    }

    @GetMapping("/saveAzureApiLogs")
    public ResultMsg saveAzureApiLogs() {
        ResultMsg resultMsg = ResultMsg.SUCCESS();
        Map<String, Object> azureApiLogs = new HashMap<>();
        azureApiLogs.put("clusterId", "abcdefghigkl5678");
        azureApiLogs.put("activityLogId", null);
        azureApiLogs.put("planId", "abcdefghigkl5678");
        threadLocal.set(azureApiLogs);
        azureService.saveAzureApiLogs(null, null, null,
                new Date(), "{\"requestData\":\"请求参数\"}", "{\"responseData\":\"响应数据\"}");
        return resultMsg;
    }

    @GetMapping("/getinfovmbyhostname")
    public List<InfoClusterVm> getInfoclustervm(String hostname, String clusterId) {
        ResultMsg resultMsg = new ResultMsg();
        List<String> hostNames = new ArrayList<>();
        hostNames.add(hostname);
        resultMsg.setRows(hostNames);
        return ivmService.getVMListByHostNames(clusterId,resultMsg.getRows());
    }

    private Map<String, String> buildMapping() {
        Map<String, String> map = new HashMap<>();
        map.put("hbase-log4j", "HBASE");
        map.put("hbase-site", "HBASE");
        map.put("hbase-atlas-application-properties", "HBASE");
        map.put("yarn-hbase-log4j", "HBASE");
        map.put("yarn-hbase-policy", "HBASE");
        map.put("hbase-policy", "HBASE");
        map.put("hbase-env", "HBASE");
        map.put("yarn-hbase-env","HBASE");
        map.put("yarn-hbase-site","HBASE");
        map.put("ranger-hbase-security","HBASE");
        map.put("ranger-hbase-policymgr-ssl","HBASE");
        map.put("ranger-hbase-audit","HBASE");
        map.put("ranger-hbase-plugin-properties","HBASE");
        map.put("hadoop-policy","HDFS");
        map.put("core-site","HDFS");
        map.put("hdfs-site","HDFS");
        map.put("hadoop-metrics2.properties","HDFS");
        map.put("hdfs-log4j","HDFS");
        map.put("hadoop-env","HDFS");
        map.put("ranger-hdfs-policymgr-ssl","HDFS");
        map.put("ranger-hdfs-audit","HDFS");
        map.put("ranger-hdfs-plugin-properties","HDFS");
        map.put("ranger-hdfs-security","HDFS");
        map.put("hive-log4j2","HIVE");
        map.put("hiveserver2-site","HIVE");
        map.put("hive-interactive-env","HIVE");
        map.put("hiveserver2-interactive-site","HIVE");
        map.put("hive-site","HIVE");
        map.put("hivemetastore-site","HIVE");
        map.put("hive-exec-log4j2","HIVE");
        map.put("hive-env","HIVE");
        map.put("hive-interactive-site","HIVE");
        map.put("ranger-hive-policymgr-ssl","HIVE");
        map.put("ranger-hive-plugin-properties","HIVE");
        map.put("hive-atlas-application.properties","HIVE");
        map.put("ranger-hive-audit","HIVE");
        map.put("ranger-hive-security","HIVE");
        map.put("mapred-site","MAPREDUCE2");
        map.put("mapred-env","MAPREDUCE2");
        map.put("livy3-log4j-properties","SPARK3");
        map.put("spark3-metrics-properties","SPARK3");
        map.put("spark3-thrift-fairscheduler","SPARK3");
        map.put("livy3-conf","SPARK3");
        map.put("spark3-hive-site-override","SPARK3");
        map.put("spark3-atlas-application-properties-override","SPARK3");
        map.put("livy3-spark-blacklist","SPARK3");
        map.put("spark3-defaults","SPARK3");
        map.put("spark3-atlas-application-properties-yarn","SPARK3");
        map.put("spark3-log4j-properties","SPARK3");
        map.put("spark3-env","SPARK3");
        map.put("livy3-client-conf","SPARK3");
        map.put("livy3-env","SPARK3");
        map.put("spark3-thrift-sparkconf","SPARK3");
        map.put("sqoop-env","SQOOP");
        map.put("sqoop-site","SQOOP");
        map.put("sqoop-atlas-application.properties","SQOOP");
        map.put("tez-interactive-site","TEZ");
        map.put("tez-env","TEZ");
        map.put("tez-site","TEZ");
        map.put("yarn-site","YARN");
        map.put("yarn-env","YARN");
        map.put("yarn-log4j","YARN");
        map.put("ranger-yarn-policymgr-ssl","YARN");
        map.put("ranger-yarn-security","YARN");
        map.put("ranger-yarn-audit","YARN");
        map.put("ranger-yarn-plugin-properties","YARN");
        map.put("zoo.cfg","ZOOKEEPER");
        map.put("zookeeper-log4j","ZOOKEEPER");
        map.put("zookeeper-env","ZOOKEEPER");
        map.put("ssl-server","HDFS");
        map.put("container-executor","YARN");
        map.put("viewfs-mount-table","HDFS");
        map.put("beeline-log4j2","HIVE");
        map.put("resource-types","HDFS");
        map.put("llap-cli-log4j2","HDFS");
        map.put("llap-daemon-log4j","HDFS");
        map.put("capacity-scheduler","YARN");
        map.put("parquet-logging","HIVE");
        map.put("ssl-client","HDFS");
        map.put("cluster-env","Stack");
        return map;
    }

    public static void main(String[] args) {
        String s = "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u000010.2.1.229";
        String s1 = Strings.trimWhitespace(s);
        String s2 = s.replace("\u0000", "");
        System.out.println(s2);
    }
    @PostMapping("/test04")
    public void test04(@RequestParam("file") MultipartFile file){
        playBookService.getPlaybookUri(file,"test.sh","westus2");
    }
}
