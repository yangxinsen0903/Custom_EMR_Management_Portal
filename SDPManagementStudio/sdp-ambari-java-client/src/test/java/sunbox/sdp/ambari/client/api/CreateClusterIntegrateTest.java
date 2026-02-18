package sunbox.sdp.ambari.client.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.BeforeClass;
import org.junit.Test;
import sunbox.sdp.ambari.client.ApiException;
import sunbox.sdp.ambari.client.Configuration;
import sunbox.sdp.ambari.client.JSON;
import sunbox.sdp.ambari.client.model.BlueprintSwagger;
import sunbox.sdp.ambari.client.model.ClusterRequestSwagger;
import sunbox.sdp.ambari.client.model.ClusterServiceStateRequest;
import sunbox.sdp.ambari.client.model.ServiceOp;
import sunbox.sdp.ambari.client.model.createclusterprocess.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.lang.Thread.sleep;

/**
 * 创建集群的集成接口测试，包括：创建Blueprint，创建集成创建模板，查看进展，查看任务执行情况
 *
 * @author: wangda
 * @date: 2022/12/8
 */
public class CreateClusterIntegrateTest extends BaseTest {

    private static String configFilePath = "/data/home/wangda/work/01-doc/01-尚博信/08-SDP/05-项目资料/06-Ambari创建集群接口测试/复制集群";
    private static String baseUri = "http://20.125.124.59:8080/api/v1";
//    private static String baseUri = "http://10.2.0.10:8080/api/v1";

    private static String referer = "http://20.125.124.59:8080/";

//    private static String blueprintName = "single-node-blueprint2";
    private static String blueprintName = "test0001";

    private static String clusterName = "test0001"; // test0001

    private static ClustersApi clusterApi = null;
    private static BlueprintsApi blueprintApi = null;

    private static ClusterServicesApi clusterServicesApi = null;


    @BeforeClass
    public static void init() {
        initClass();
        Configuration.getDefaultApiClient().setBasePath(baseUri);
        Configuration.getDefaultApiClient().setDebugging(true);
        Configuration.getDefaultApiClient().addDefaultHeader("Referer", referer);

        clusterApi = new ClustersApi();
        blueprintApi = new BlueprintsApi();
        clusterServicesApi = new ClusterServicesApi();
    }

    @Test
    public void deleteCluster() throws ApiException {
        clusterApi.deleteCluster(clusterName);
    }

    @Test
    public void deleteBlueprint() throws ApiException {
        blueprintApi.blueprintServiceDeleteBlueprint(blueprintName);
    }

    @Test
    public void getBlueprint() throws ApiException {
        BlueprintSwagger blueprintSwagger = blueprintApi.blueprintServiceGetBlueprint(clusterName, "Blueprints/*");
        Gson gson = new Gson();
        String json = gson.toJson(blueprintSwagger);
        System.out.println(json);
    }

    @Test
    public void getBlueprintFromCluster() throws ApiException {
        String blueprint= clusterApi.getBlueprintString(clusterName);
        System.out.println(blueprint);
    }



    /**
     * 创建蓝图
     */
    @Test
    public void createBlueprint() throws Exception {
        Configuration.getDefaultApiClient().setDebugging(true);
        BlueprintSwagger body = readBlueprintSwaggerFromFile();
        try {
            blueprintApi.blueprintServiceCreateBlueprint(blueprintName, body);
        } catch (ApiException ex ) {
            if (Objects.equals(ex.getCode(), 409)) {
                System.out.println("同名集群已经失败，可能是重试操作");
            }
        }

    }

    /**
     * 测试创建集群模板
     */
    @Test
    public void createClusterTemplate() throws Exception {
        Configuration.getDefaultApiClient().setDebugging(true);
        ClusterRequestSwagger body = readClusterRequestSwaggerFromFile();
        CreateClusterResponseWrapper response = clusterApi.createCluster(clusterName, body);
        JSON json = new JSON();
        System.out.println(json.getGson().toJson(response));
        System.out.println("===============================================");
        System.out.println("RequestId = " + response.getRequestId());
        sleep(2000);
        queryCreateProgress(Long.valueOf(response.getRequestId()));
    }

    /**
     * 测试查看集群创建过程,每隔3秒钟打印一次创建步骤
     */
    @Test
    public void queryCreateProgress() throws Exception {

        Long requestId = 1L;
        queryCreateProgress(requestId);
    }

    public void queryCreateProgress(Long requestId) throws Exception {
        Configuration.getDefaultApiClient().setDebugging(false);
        Map<Long, String> taskProgress = new HashMap<>();
        while(true) {
            // 请求查询进度
            CreateClusterProcess createResult = getClusterCreateProcessTest(requestId);
            printCreateClusterSummaryInfo(createResult);
            // 每隔3秒,查询一下执行步骤.
            printCreateClusterTaskInfo(createResult, taskProgress);
            if (Objects.equals(createResult.getRequest().getTaskCount(), createResult.getRequest().getCompletedTaskCount())) {
                break;
            }
            sleep(3000);
        }


    }

    private void printCreateClusterSummaryInfo(CreateClusterProcess createResult) {
        Integer total = createResult.getRequest().getTaskCount();
        Integer failed = createResult.getRequest().getFailedTaskCount();
        Integer aborted = createResult.getRequest().getAbortedTaskCount();
        Integer queued = createResult.getRequest().getQueuedTaskCount();
        Integer completed = createResult.getRequest().getCompletedTaskCount();
        Integer timeouted = createResult.getRequest().getTimedOutTaskCount();
        Integer pending = createResult.getRequest().getPendingHostRequestCount();

        System.out.println("============================================");
        System.out.println("集群名称:" + createResult.getRequest().getClusterName());
        System.out.println("安装进展:" + createResult.getRequest().getProgressPercent() + "%    " +  completed + "/" + total);
        System.out.println("成功任务数：" + completed + "    Queued任务数:" + queued + "    超时任务数:" + timeouted
                + "    Pending任务数:" + pending     + "    失败任务数 :"  + failed + "     终止任务数 : " + aborted);
        System.out.println("-----------------------正在查询任务状态----------------------");
    }

    private void printCreateClusterTaskInfo(CreateClusterProcess createResult, Map<Long, String> taskProgress) throws Exception {
        if (taskProgress.size() == 0) {
            for (CreateClusterProcessTaskWrapper task : createResult.getTasks()) {
                taskProgress.put(task.getTasks().getId(), "");
            }
        }

        for (CreateClusterProcessTaskWrapper task : createResult.getTasks()) {
            String taskInfo = taskProgress.get(task.getTasks().getId());
            if (Objects.equals(taskInfo, "") || !taskInfo.startsWith("COMPLETED")) {
                // 查询一次
                Long taskId = task.getTasks().getId();
                Long requestId = task.getTasks().getRequestId();
                CreateClusterProcessTaskDetailWrapper taskResp = getClusterCreateProcessTaskTest(requestId, taskId);

                String commandDetail = taskResp.getTasks().getCommandDetail();
                String command = taskResp.getTasks().getCommand();

                String status = taskResp.getTasks().getStatus();

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Long startTime = taskResp.getTasks().getStartTime();
                String startTimeStr = sdf.format(new Date(startTime));

                Long endTime = taskResp.getTasks().getEndTime();
                String endTimeStr = sdf.format(new Date(endTime));

                String hostName = taskResp.getTasks().getHostName();

                String roleName = taskResp.getTasks().getRole();

                taskInfo = status + "\t<" + command + " " + roleName + ">:" + commandDetail ;
                        // + "\t\t  hostName:" + hostName + "\t " ;
                        // + startTimeStr + "~" + endTimeStr;
                taskProgress.put(taskId, taskInfo);
            }
        }

        for (CreateClusterProcessTaskWrapper task : createResult.getTasks()) {
            String taskInfo = taskProgress.get(task.getTasks().getId());
            System.out.println( "TASK:" + task.getTasks().getId() + " ::: " + taskInfo);
        }


        System.out.println("=======================完成=====================");
        System.out.println("");
    }

    @Test
    public void startServiceTest() throws Exception {
//        String[] services = new String[]{"ZOOKEEPER"};
//        String[] services = new String[]{"HDFS"};
//        String[] services = new String[]{"YARN", };
//        String[] services = new String[]{"MAPREDUCE2"};
//        String[] services = new String[]{"HIVE"};
        String[] services = new String[]{"SPARK3"};
        for (String service : services) {
            System.out.println("=============== 正在启动服务：" + service + " ===============");
            ClusterServiceStateRequest yarnRequest = ClusterServiceStateRequest.buildSerivceRequest(clusterName, service, ServiceOp.START);
            CreateClusterResponseWrapper response = clusterServicesApi.serviceServiceUpdateService(service, clusterName, yarnRequest);
            System.out.println(response.getHref());
            System.out.println("=============== 启动服务完成：" + service + " ===============");
            sleep(3000);
        }
    }

    @Test
    public void stopServiceTest() throws ApiException {
        String serviceName = "HIVE";
        ClusterServiceStateRequest yarnRequest = ClusterServiceStateRequest.buildSerivceRequest(clusterName, serviceName, ServiceOp.STOP);
        CreateClusterResponseWrapper response = clusterServicesApi.serviceServiceUpdateService(serviceName, clusterName, yarnRequest);

        System.out.println(response.getHref());
    }

    @Test
    public void clusterRequestsTest() throws ApiException {
        String clusterName = "test";
        String serviceName = "HDFS";
        String componentName = "NAMENODE";
        String hostName = "sunbox-dev-vm12.vmdns.sunbox.com";
        ClusterRequest req = ClusterRequest.buildFormatRequest(serviceName, componentName, hostName);

        clusterApi.clusterRequests(clusterName, req);

    }

    public CreateClusterProcess getClusterCreateProcessTest(Long requestId) throws Exception {
        CreateClusterProcess resp = clusterApi.getClusterCreateProcess(clusterName, requestId);
        return resp;
    }

    public CreateClusterProcessTaskDetailWrapper getClusterCreateProcessTaskTest(Long requestId, Long taskId) throws Exception {
        CreateClusterProcessTaskDetailWrapper resp = clusterApi.getClusterCreateProcessTask(clusterName, requestId, taskId);
        return  resp;
    }

    public CreateClusterProcess getClusterCreateProcessStageTest(Integer requestId, Integer stageId) throws Exception {
        CreateClusterProcess resp = clusterApi.getClusterCreateProcessStage(clusterName, requestId, stageId);
        return resp;
    }


    private BlueprintSwagger readBlueprintSwaggerFromFile() {
        byte[] file = new byte[0];
        try {
            file = Files.readAllBytes(Paths.get(configFilePath + "/blueprint.json"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String content = new String(file);
        JSON json = new JSON();
        BlueprintSwagger obj = json.deserialize(content, new TypeToken<BlueprintSwagger>() {
        }.getType());
        return obj;
    }

    private ClusterRequestSwagger readClusterRequestSwaggerFromFile() {
        byte[] file = new byte[0];
        try {
            file = Files.readAllBytes(Paths.get(configFilePath + "/clusterTemplate.json"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String content = new String(file);
        JSON json = new JSON();
        ClusterRequestSwagger obj = json.deserialize(content, new TypeToken<ClusterRequestSwagger>() {
        }.getType());
        return obj;
    }



}
