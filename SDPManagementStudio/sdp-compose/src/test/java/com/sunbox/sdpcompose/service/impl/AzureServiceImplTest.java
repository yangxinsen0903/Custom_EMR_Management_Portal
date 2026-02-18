package com.sunbox.sdpcompose.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sunbox.domain.ConfCluster;
import com.sunbox.sdpcompose.model.azure.request.AzureVmsRequest;
import com.sunbox.sdpcompose.service.ambari.AmbariInfo;
import com.sunbox.sdpcompose.service.ambari.AmbariServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author: wangda
 * @date: 2022/12/24
 */
class AzureServiceImplTest {

    @Test
    void createVms() {
        AzureServiceImpl service = new AzureServiceImpl();
        service.setAzureUrl("http://localhost");
        //AzureVmsRequest req =  JSON.parseObject(loadCreateVMRequest(), AzureVmsRequest.class);
        //service.createVms(req);
    }

    @Test
    void checkScaleOutJobReduce(){
        JSONObject jsonObject = JSON.parseObject(getss2());
        AzureVMServiceImpl service = new AzureVMServiceImpl();

        //service.checkScaleOutJobReduce(jsonObject);
        //service.processCreateOrScaleOutJobDetailMessage(new ConfCluster(),jsonObject,null,"scaleout");
    }


    @Test
    void testdecommissionone(){
        AmbariInfo ambariInfo=AmbariInfo.of("http://localhost:8080/api/v1","admin","admin");
        AmbariServiceImpl ambariService = new AmbariServiceImpl();
        ambariService.decommissionComponent(ambariInfo,
                "ZDYHIVEabfsV2dev080801",
                "zdy-hive-abfsv2-dev-080801-tsk-0069.dev.sdp.com",
                "NODEMANAGER");


        //service.checkScaleOutJobReduce(jsonObject);
        //service.processCreateOrScaleOutJobDetailMessage(new ConfCluster(),jsonObject,null,"scaleout");
    }

    private String getss(){
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("{\n" +
                "  \"jobId\": \"append-cluster-sdp-mc-spot-300-0424-B-01-vms-task-393-50-20230425-052715-292\",\n" +
                "  \"clusterName\": \"sdp-mc-spot-300-0424-B-01\",\n" +
                "  \"provisionedVmGroups\": [\n" +
                "    {\n" +
                "      \"groupName\": \"task\",\n" +
                "      \"count\": 18,\n" +
                "      \"virtualMachines\": [\n" +
                "        {\n" +
                "          \"name\": \"vm-sdp-mc-spot-300-0424-b-01-tsk-0393\",\n" +
                "          \"hostName\": \"sdp-mc-spot-300-0424-b-01-tsk-0393.sdp.azure.com\",\n" +
                "          \"privateIp\": \"10.209.1.170\",\n" +
                "          \"zone\": \"1\",\n" +
                "          \"tags\": {\n" +
                "            \"sdp-purchasetype\": \"2\",\n" +
                "            \"Physical Zone\": \"1\",\n" +
                "            \"OpsCloud Zone\": \"westus3b\",\n" +
                "            \"Logical Zone\": \"3\",\n" +
                "            \"sdp-spot-demand-price\": \"4.032\",\n" +
                "            \"sdp-spot-bid-price\": \"1.6128\",\n" +
                "            \"VMIndex\": \"393\",\n" +
                "            \"sdp-version\": \"SDP-1.0\",\n" +
                "            \"sdp-group\": \"task-task\",\n" +
                "            \"svcid\": \"1fbaa9cb-8c80-49d1-b7a4-fd6d73e101e2\",\n" +
                "            \"sdp-role\": \"task\",\n" +
                "            \"svc\": \"dataAbcService-uswest5-prod-center\",\n" +
                "            \"for\": \"PatrickSheng\"\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"vm-sdp-mc-spot-300-0424-b-01-tsk-0396\",\n" +
                "          \"hostName\": \"sdp-mc-spot-300-0424-b-01-tsk-0396.sdp.azure.com\",\n" +
                "          \"privateIp\": \"10.209.19.247\",\n" +
                "          \"zone\": \"1\",\n" +
                "          \"tags\": {\n" +
                "            \"sdp-purchasetype\": \"2\",\n" +
                "            \"Physical Zone\": \"1\",\n" +
                "            \"OpsCloud Zone\": \"westus3b\",\n" +
                "            \"Logical Zone\": \"3\",\n" +
                "            \"sdp-spot-demand-price\": \"4.032\",\n" +
                "            \"sdp-spot-bid-price\": \"1.6128\",\n" +
                "            \"VMIndex\": \"396\",\n" +
                "            \"sdp-version\": \"SDP-1.0\",\n" +
                "            \"sdp-group\": \"task-task\",\n" +
                "            \"svcid\": \"1fbaa9cb-8c80-49d1-b7a4-fd6d73e101e2\",\n" +
                "            \"sdp-role\": \"task\",\n" +
                "            \"svc\": \"dataAbcService-uswest5-prod-center\",\n" +
                "            \"for\": \"PatrickSheng\"\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"vm-sdp-mc-spot-300-0424-b-01-tsk-0399\",\n" +
                "          \"hostName\": \"sdp-mc-spot-300-0424-b-01-tsk-0399.sdp.azure.com\",\n" +
                "          \"privateIp\": \"10.209.1.130\",\n" +
                "          \"zone\": \"1\",\n" +
                "          \"tags\": {\n" +
                "            \"sdp-purchasetype\": \"2\",\n" +
                "            \"Physical Zone\": \"1\",\n" +
                "            \"OpsCloud Zone\": \"westus3b\",\n" +
                "            \"Logical Zone\": \"3\",\n" +
                "            \"sdp-spot-demand-price\": \"4.032\",\n" +
                "            \"sdp-spot-bid-price\": \"1.6128\",\n" +
                "            \"VMIndex\": \"399\",\n" +
                "            \"sdp-version\": \"SDP-1.0\",\n" +
                "            \"sdp-group\": \"task-task\",\n" +
                "            \"svcid\": \"1fbaa9cb-8c80-49d1-b7a4-fd6d73e101e2\",\n" +
                "            \"sdp-role\": \"task\",\n" +
                "            \"svc\": \"dataAbcService-uswest5-prod-center\",\n" +
                "            \"for\": \"PatrickSheng\"\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"vm-sdp-mc-spot-300-0424-b-01-tsk-0401\",\n" +
                "          \"hostName\": \"sdp-mc-spot-300-0424-b-01-tsk-0401.sdp.azure.com\",\n" +
                "          \"privateIp\": \"10.209.1.172\",\n" +
                "          \"zone\": \"1\",\n" +
                "          \"tags\": {\n" +
                "            \"sdp-purchasetype\": \"2\",\n" +
                "            \"Physical Zone\": \"1\",\n" +
                "            \"OpsCloud Zone\": \"westus3b\",\n" +
                "            \"Logical Zone\": \"3\",\n" +
                "            \"sdp-spot-demand-price\": \"4.032\",\n" +
                "            \"sdp-spot-bid-price\": \"1.6128\",\n" +
                "            \"VMIndex\": \"401\",\n" +
                "            \"sdp-version\": \"SDP-1.0\",\n" +
                "            \"sdp-group\": \"task-task\",\n" +
                "            \"svcid\": \"1fbaa9cb-8c80-49d1-b7a4-fd6d73e101e2\",\n" +
                "            \"sdp-role\": \"task\",\n" +
                "            \"svc\": \"dataAbcService-uswest5-prod-center\",\n" +
                "            \"for\": \"PatrickSheng\"\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"vm-sdp-mc-spot-300-0424-b-01-tsk-0403\",\n" +
                "          \"hostName\": \"sdp-mc-spot-300-0424-b-01-tsk-0403.sdp.azure.com\",\n" +
                "          \"privateIp\": \"10.209.0.244\",\n" +
                "          \"zone\": \"1\",\n" +
                "          \"tags\": {\n" +
                "            \"sdp-purchasetype\": \"2\",\n" +
                "            \"Physical Zone\": \"1\",\n" +
                "            \"OpsCloud Zone\": \"westus3b\",\n" +
                "            \"Logical Zone\": \"3\",\n" +
                "            \"sdp-spot-demand-price\": \"4.032\",\n" +
                "            \"sdp-spot-bid-price\": \"1.6128\",\n" +
                "            \"VMIndex\": \"403\",\n" +
                "            \"sdp-version\": \"SDP-1.0\",\n" +
                "            \"sdp-group\": \"task-task\",\n" +
                "            \"svcid\": \"1fbaa9cb-8c80-49d1-b7a4-fd6d73e101e2\",\n" +
                "            \"sdp-role\": \"task\",\n" +
                "            \"svc\": \"dataAbcService-uswest5-prod-center\",\n" +
                "            \"for\": \"PatrickSheng\"\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"vm-sdp-mc-spot-300-0424-b-01-tsk-0407\",\n" +
                "          \"hostName\": \"sdp-mc-spot-300-0424-b-01-tsk-0407.sdp.azure.com\",\n" +
                "          \"privateIp\": \"10.209.0.34\",\n" +
                "          \"zone\": \"1\",\n" +
                "          \"tags\": {\n" +
                "            \"sdp-purchasetype\": \"2\",\n" +
                "            \"Physical Zone\": \"1\",\n" +
                "            \"OpsCloud Zone\": \"westus3b\",\n" +
                "            \"Logical Zone\": \"3\",\n" +
                "            \"sdp-spot-demand-price\": \"4.032\",\n" +
                "            \"sdp-spot-bid-price\": \"1.6128\",\n" +
                "            \"VMIndex\": \"407\",\n" +
                "            \"sdp-version\": \"SDP-1.0\",\n" +
                "            \"sdp-group\": \"task-task\",\n" +
                "            \"svcid\": \"1fbaa9cb-8c80-49d1-b7a4-fd6d73e101e2\",\n" +
                "            \"sdp-role\": \"task\",\n" +
                "            \"svc\": \"dataAbcService-uswest5-prod-center\",\n" +
                "            \"for\": \"PatrickSheng\"\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"vm-sdp-mc-spot-300-0424-b-01-tsk-0410\",\n" +
                "          \"hostName\": \"sdp-mc-spot-300-0424-b-01-tsk-0410.sdp.azure.com\",\n" +
                "          \"privateIp\": \"10.209.20.0\",\n" +
                "          \"zone\": \"1\",\n" +
                "          \"tags\": {\n" +
                "            \"sdp-purchasetype\": \"2\",\n" +
                "            \"Physical Zone\": \"1\",\n" +
                "            \"OpsCloud Zone\": \"westus3b\",\n" +
                "            \"Logical Zone\": \"3\",\n" +
                "            \"sdp-spot-demand-price\": \"4.032\",\n" +
                "            \"sdp-spot-bid-price\": \"1.6128\",\n" +
                "            \"VMIndex\": \"410\",\n" +
                "            \"sdp-version\": \"SDP-1.0\",\n" +
                "            \"sdp-group\": \"task-task\",\n" +
                "            \"svcid\": \"1fbaa9cb-8c80-49d1-b7a4-fd6d73e101e2\",\n" +
                "            \"sdp-role\": \"task\",\n" +
                "            \"svc\": \"dataAbcService-uswest5-prod-center\",\n" +
                "            \"for\": \"PatrickSheng\"\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"vm-sdp-mc-spot-300-0424-b-01-tsk-0411\",\n" +
                "          \"hostName\": \"sdp-mc-spot-300-0424-b-01-tsk-0411.sdp.azure.com\",\n" +
                "          \"privateIp\": \"10.209.1.67\",\n" +
                "          \"zone\": \"1\",\n" +
                "          \"tags\": {\n" +
                "            \"sdp-purchasetype\": \"2\",\n" +
                "            \"Physical Zone\": \"1\",\n" +
                "            \"OpsCloud Zone\": \"westus3b\",\n" +
                "            \"Logical Zone\": \"3\",\n" +
                "            \"sdp-spot-demand-price\": \"4.032\",\n" +
                "            \"sdp-spot-bid-price\": \"1.6128\",\n" +
                "            \"VMIndex\": \"411\",\n" +
                "            \"sdp-version\": \"SDP-1.0\",\n" +
                "            \"sdp-group\": \"task-task\",\n" +
                "            \"svcid\": \"1fbaa9cb-8c80-49d1-b7a4-fd6d73e101e2\",\n" +
                "            \"sdp-role\": \"task\",\n" +
                "            \"svc\": \"dataAbcService-uswest5-prod-center\",\n" +
                "            \"for\": \"PatrickSheng\"\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"vm-sdp-mc-spot-300-0424-b-01-tsk-0413\",\n" +
                "          \"hostName\": \"sdp-mc-spot-300-0424-b-01-tsk-0413.sdp.azure.com\",\n" +
                "          \"privateIp\": \"10.209.19.251\",\n" +
                "          \"zone\": \"1\",\n" +
                "          \"tags\": {\n" +
                "            \"sdp-purchasetype\": \"2\",\n" +
                "            \"Physical Zone\": \"1\",\n" +
                "            \"OpsCloud Zone\": \"westus3b\",\n" +
                "            \"Logical Zone\": \"3\",\n" +
                "            \"sdp-spot-demand-price\": \"4.032\",\n" +
                "            \"sdp-spot-bid-price\": \"1.6128\",\n" +
                "            \"VMIndex\": \"413\",\n" +
                "            \"sdp-version\": \"SDP-1.0\",\n" +
                "            \"sdp-group\": \"task-task\",\n" +
                "            \"svcid\": \"1fbaa9cb-8c80-49d1-b7a4-fd6d73e101e2\",\n" +
                "            \"sdp-role\": \"task\",\n" +
                "            \"svc\": \"dataAbcService-uswest5-prod-center\",\n" +
                "            \"for\": \"PatrickSheng\"\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"vm-sdp-mc-spot-300-0424-b-01-tsk-0415\",\n" +
                "          \"hostName\": \"sdp-mc-spot-300-0424-b-01-tsk-0415.sdp.azure.com\",\n" +
                "          \"privateIp\": \"10.209.19.246\",\n" +
                "          \"zone\": \"1\",\n" +
                "          \"tags\": {\n" +
                "            \"sdp-purchasetype\": \"2\",\n" +
                "            \"Physical Zone\": \"1\",\n" +
                "            \"OpsCloud Zone\": \"westus3b\",\n" +
                "            \"Logical Zone\": \"3\",\n" +
                "            \"sdp-spot-demand-price\": \"4.032\",\n" +
                "            \"sdp-spot-bid-price\": \"1.6128\",\n" +
                "            \"VMIndex\": \"415\",\n" +
                "            \"sdp-version\": \"SDP-1.0\",\n" +
                "            \"sdp-group\": \"task-task\",\n" +
                "            \"svcid\": \"1fbaa9cb-8c80-49d1-b7a4-fd6d73e101e2\",\n" +
                "            \"sdp-role\": \"task\",\n" +
                "            \"svc\": \"dataAbcService-uswest5-prod-center\",\n" +
                "            \"for\": \"PatrickSheng\"\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"vm-sdp-mc-spot-300-0424-b-01-tsk-0418\",\n" +
                "          \"hostName\": \"sdp-mc-spot-300-0424-b-01-tsk-0418.sdp.azure.com\",\n" +
                "          \"privateIp\": \"10.209.2.214\",\n" +
                "          \"zone\": \"1\",\n" +
                "          \"tags\": {\n" +
                "            \"sdp-purchasetype\": \"2\",\n" +
                "            \"Physical Zone\": \"1\",\n" +
                "            \"OpsCloud Zone\": \"westus3b\",\n" +
                "            \"Logical Zone\": \"3\",\n" +
                "            \"sdp-spot-demand-price\": \"4.032\",\n" +
                "            \"sdp-spot-bid-price\": \"1.6128\",\n" +
                "            \"VMIndex\": \"418\",\n" +
                "            \"sdp-version\": \"SDP-1.0\",\n" +
                "            \"sdp-group\": \"task-task\",\n" +
                "            \"svcid\": \"1fbaa9cb-8c80-49d1-b7a4-fd6d73e101e2\",\n" +
                "            \"sdp-role\": \"task\",\n" +
                "            \"svc\": \"dataAbcService-uswest5-prod-center\",\n" +
                "            \"for\": \"PatrickSheng\"\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"vm-sdp-mc-spot-300-0424-b-01-tsk-0421\",\n" +
                "          \"hostName\": \"sdp-mc-spot-300-0424-b-01-tsk-0421.sdp.azure.com\",\n" +
                "          \"privateIp\": \"10.209.1.171\",\n" +
                "          \"zone\": \"1\",\n" +
                "          \"tags\": {\n" +
                "            \"sdp-purchasetype\": \"2\",\n" +
                "            \"Physical Zone\": \"1\",\n" +
                "            \"OpsCloud Zone\": \"westus3b\",\n" +
                "            \"Logical Zone\": \"3\",\n" +
                "            \"sdp-spot-demand-price\": \"4.032\",\n" +
                "            \"sdp-spot-bid-price\": \"1.6128\",\n" +
                "            \"VMIndex\": \"421\",\n" +
                "            \"sdp-version\": \"SDP-1.0\",\n" +
                "            \"sdp-group\": \"task-task\",\n" +
                "            \"svcid\": \"1fbaa9cb-8c80-49d1-b7a4-fd6d73e101e2\",\n" +
                "            \"sdp-role\": \"task\",\n" +
                "            \"svc\": \"dataAbcService-uswest5-prod-center\",\n" +
                "            \"for\": \"PatrickSheng\"\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"vm-sdp-mc-spot-300-0424-b-01-tsk-0422\",\n" +
                "          \"hostName\": \"sdp-mc-spot-300-0424-b-01-tsk-0422.sdp.azure.com\",\n" +
                "          \"privateIp\": \"10.209.2.239\",\n" +
                "          \"zone\": \"1\",\n" +
                "          \"tags\": {\n" +
                "            \"sdp-purchasetype\": \"2\",\n" +
                "            \"Physical Zone\": \"1\",\n" +
                "            \"OpsCloud Zone\": \"westus3b\",\n" +
                "            \"Logical Zone\": \"3\",\n" +
                "            \"sdp-spot-demand-price\": \"4.032\",\n" +
                "            \"sdp-spot-bid-price\": \"1.6128\",\n" +
                "            \"VMIndex\": \"422\",\n" +
                "            \"sdp-version\": \"SDP-1.0\",\n" +
                "            \"sdp-group\": \"task-task\",\n" +
                "            \"svcid\": \"1fbaa9cb-8c80-49d1-b7a4-fd6d73e101e2\",\n" +
                "            \"sdp-role\": \"task\",\n" +
                "            \"svc\": \"dataAbcService-uswest5-prod-center\",\n" +
                "            \"for\": \"PatrickSheng\"\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"vm-sdp-mc-spot-300-0424-b-01-tsk-0424\",\n" +
                "          \"hostName\": \"sdp-mc-spot-300-0424-b-01-tsk-0424.sdp.azure.com\",\n" +
                "          \"privateIp\": \"10.209.7.123\",\n" +
                "          \"zone\": \"1\",\n" +
                "          \"tags\": {\n" +
                "            \"sdp-purchasetype\": \"2\",\n" +
                "            \"Physical Zone\": \"1\",\n" +
                "            \"OpsCloud Zone\": \"westus3b\",\n" +
                "            \"Logical Zone\": \"3\",\n" +
                "            \"sdp-spot-demand-price\": \"4.032\",\n" +
                "            \"sdp-spot-bid-price\": \"1.6128\",\n" +
                "            \"VMIndex\": \"424\",\n" +
                "            \"sdp-version\": \"SDP-1.0\",\n" +
                "            \"sdp-group\": \"task-task\",\n" +
                "            \"svcid\": \"1fbaa9cb-8c80-49d1-b7a4-fd6d73e101e2\",\n" +
                "            \"sdp-role\": \"task\",\n" +
                "            \"svc\": \"dataAbcService-uswest5-prod-center\",\n" +
                "            \"for\": \"PatrickSheng\"\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"vm-sdp-mc-spot-300-0424-b-01-tsk-0425\",\n" +
                "          \"hostName\": \"sdp-mc-spot-300-0424-b-01-tsk-0425.sdp.azure.com\",\n" +
                "          \"privateIp\": \"10.209.1.104\",\n" +
                "          \"zone\": \"1\",\n" +
                "          \"tags\": {\n" +
                "            \"sdp-purchasetype\": \"2\",\n" +
                "            \"Physical Zone\": \"1\",\n" +
                "            \"OpsCloud Zone\": \"westus3b\",\n" +
                "            \"Logical Zone\": \"3\",\n" +
                "            \"sdp-spot-demand-price\": \"4.032\",\n" +
                "            \"sdp-spot-bid-price\": \"1.6128\",\n" +
                "            \"VMIndex\": \"425\",\n" +
                "            \"sdp-version\": \"SDP-1.0\",\n" +
                "            \"sdp-group\": \"task-task\",\n" +
                "            \"svcid\": \"1fbaa9cb-8c80-49d1-b7a4-fd6d73e101e2\",\n" +
                "            \"sdp-role\": \"task\",\n" +
                "            \"svc\": \"dataAbcService-uswest5-prod-center\",\n" +
                "            \"for\": \"PatrickSheng\"\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"vm-sdp-mc-spot-300-0424-b-01-tsk-0430\",\n" +
                "          \"hostName\": \"sdp-mc-spot-300-0424-b-01-tsk-0430.sdp.azure.com\",\n" +
                "          \"privateIp\": \"10.209.3.19\",\n" +
                "          \"zone\": \"1\",\n" +
                "          \"tags\": {\n" +
                "            \"sdp-purchasetype\": \"2\",\n" +
                "            \"Physical Zone\": \"1\",\n" +
                "            \"OpsCloud Zone\": \"westus3b\",\n" +
                "            \"Logical Zone\": \"3\",\n" +
                "            \"sdp-spot-demand-price\": \"4.032\",\n" +
                "            \"sdp-spot-bid-price\": \"1.6128\",\n" +
                "            \"VMIndex\": \"430\",\n" +
                "            \"sdp-version\": \"SDP-1.0\",\n" +
                "            \"sdp-group\": \"task-task\",\n" +
                "            \"svcid\": \"1fbaa9cb-8c80-49d1-b7a4-fd6d73e101e2\",\n" +
                "            \"sdp-role\": \"task\",\n" +
                "            \"svc\": \"dataAbcService-uswest5-prod-center\",\n" +
                "            \"for\": \"PatrickSheng\"\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"vm-sdp-mc-spot-300-0424-b-01-tsk-0431\",\n" +
                "          \"hostName\": \"sdp-mc-spot-300-0424-b-01-tsk-0431.sdp.azure.com\",\n" +
                "          \"privateIp\": \"10.209.19.252\",\n" +
                "          \"zone\": \"1\",\n" +
                "          \"tags\": {\n" +
                "            \"sdp-purchasetype\": \"2\",\n" +
                "            \"Physical Zone\": \"1\",\n" +
                "            \"OpsCloud Zone\": \"westus3b\",\n" +
                "            \"Logical Zone\": \"3\",\n" +
                "            \"sdp-spot-demand-price\": \"4.032\",\n" +
                "            \"sdp-spot-bid-price\": \"1.6128\",\n" +
                "            \"VMIndex\": \"431\",\n" +
                "            \"sdp-version\": \"SDP-1.0\",\n" +
                "            \"sdp-group\": \"task-task\",\n" +
                "            \"svcid\": \"1fbaa9cb-8c80-49d1-b7a4-fd6d73e101e2\",\n" +
                "            \"sdp-role\": \"task\",\n" +
                "            \"svc\": \"dataAbcService-uswest5-prod-center\",\n" +
                "            \"for\": \"PatrickSheng\"\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"name\": \"vm-sdp-mc-spot-300-0424-b-01-tsk-0436\",\n" +
                "          \"hostName\": \"sdp-mc-spot-300-0424-b-01-tsk-0436.sdp.azure.com\",\n" +
                "          \"privateIp\": \"10.209.7.83\",\n" +
                "          \"zone\": \"1\",\n" +
                "          \"tags\": {\n" +
                "            \"sdp-purchasetype\": \"2\",\n" +
                "            \"Physical Zone\": \"1\",\n" +
                "            \"OpsCloud Zone\": \"westus3b\",\n" +
                "            \"Logical Zone\": \"3\",\n" +
                "            \"sdp-spot-demand-price\": \"4.032\",\n" +
                "            \"sdp-spot-bid-price\": \"1.6128\",\n" +
                "            \"VMIndex\": \"436\",\n" +
                "            \"sdp-version\": \"SDP-1.0\",\n" +
                "            \"sdp-group\": \"task-task\",\n" +
                "            \"svcid\": \"1fbaa9cb-8c80-49d1-b7a4-fd6d73e101e2\",\n" +
                "            \"sdp-role\": \"task\",\n" +
                "            \"svc\": \"dataAbcService-uswest5-prod-center\",\n" +
                "            \"for\": \"PatrickSheng\"\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"failedVMs\": [\n" +
                "    \"vm-sdp-mc-spot-300-0424-b-01-tsk-0394\",\n" +
                "    \"vm-sdp-mc-spot-300-0424-b-01-tsk-0395\",\n" +
                "    \"vm-sdp-mc-spot-300-0424-b-01-tsk-0397\",\n" +
                "    \"vm-sdp-mc-spot-300-0424-b-01-tsk-0398\",\n" +
                "    \"vm-sdp-mc-spot-300-0424-b-01-tsk-0400\",\n" +
                "    \"vm-sdp-mc-spot-300-0424-b-01-tsk-0402\",\n" +
                "    \"vm-sdp-mc-spot-300-0424-b-01-tsk-0404\",\n" +
                "    \"vm-sdp-mc-spot-300-0424-b-01-tsk-0405\",\n" +
                "    \"vm-sdp-mc-spot-300-0424-b-01-tsk-0406\",\n" +
                "    \"vm-sdp-mc-spot-300-0424-b-01-tsk-0408\",\n" +
                "    \"vm-sdp-mc-spot-300-0424-b-01-tsk-0409\",\n" +
                "    \"vm-sdp-mc-spot-300-0424-b-01-tsk-0412\",\n" +
                "    \"vm-sdp-mc-spot-300-0424-b-01-tsk-0414\",\n" +
                "    \"vm-sdp-mc-spot-300-0424-b-01-tsk-0416\",\n" +
                "    \"vm-sdp-mc-spot-300-0424-b-01-tsk-0417\",\n" +
                "    \"vm-sdp-mc-spot-300-0424-b-01-tsk-0419\",\n" +
                "    \"vm-sdp-mc-spot-300-0424-b-01-tsk-0420\",\n" +
                "    \"vm-sdp-mc-spot-300-0424-b-01-tsk-0423\",\n" +
                "    \"vm-sdp-mc-spot-300-0424-b-01-tsk-0426\",\n" +
                "    \"vm-sdp-mc-spot-300-0424-b-01-tsk-0427\",\n" +
                "    \"vm-sdp-mc-spot-300-0424-b-01-tsk-0428\",\n" +
                "    \"vm-sdp-mc-spot-300-0424-b-01-tsk-0429\",\n" +
                "    \"vm-sdp-mc-spot-300-0424-b-01-tsk-0432\",\n" +
                "    \"vm-sdp-mc-spot-300-0424-b-01-tsk-0433\",\n" +
                "    \"vm-sdp-mc-spot-300-0424-b-01-tsk-0434\",\n" +
                "    \"vm-sdp-mc-spot-300-0424-b-01-tsk-0435\",\n" +
                "    \"vm-sdp-mc-spot-300-0424-b-01-tsk-0437\",\n" +
                "    \"vm-sdp-mc-spot-300-0424-b-01-tsk-0438\",\n" +
                "    \"vm-sdp-mc-spot-300-0424-b-01-tsk-0439\",\n" +
                "    \"vm-sdp-mc-spot-300-0424-b-01-tsk-0440\",\n" +
                "    \"vm-sdp-mc-spot-300-0424-b-01-tsk-0441\",\n" +
                "    \"vm-sdp-mc-spot-300-0424-b-01-tsk-0442\"\n" +
                "  ],\n" +
                "  \"provisionStatus\": \"Failed\",\n" +
                "  \"deployDetailResults\": [\n" +
                "    {\n" +
                "      \"deployName\": \"sdp-mc-spot-300-0424-b-01-tsk-0-vm-deploy-393\",\n" +
                "      \"correlationId\": \"f69870cc-e286-401b-ab34-f1c1ecdaad44\",\n" +
                "      \"provisionState\": \"Failed\",\n" +
                "      \"vMs\": [\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0393\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0394\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0395\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0396\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0397\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0398\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0399\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0400\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0401\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0402\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0403\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0404\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0405\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0406\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0407\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0408\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0409\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0410\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0411\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0412\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0413\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0414\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0415\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0416\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0417\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0418\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0419\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0420\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0421\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0422\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0423\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0424\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0425\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0426\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0427\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0428\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0429\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0430\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0431\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0432\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0433\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0434\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0435\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0436\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0437\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0438\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0439\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0440\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0441\",\n" +
                "        \"vm-sdp-mc-spot-300-0424-b-01-tsk-0442\"\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"deployName\": \"append-cluster-sdp-mc-spot-300-0424-B-01-vms-task-393-50\",\n" +
                "      \"correlationId\": \"f69870cc-e286-401b-ab34-f1c1ecdaad44\",\n" +
                "      \"provisionState\": \"Failed\",\n" +
                "      \"timestamp\": \"2023-04-25T05:28:22.6257527+00:00\",\n" +
                "      \"duration\": \"00:01:07.5056071\",\n" +
                "      \"deployError\": {\n" +
                "        \"code\": \"DeploymentFailed\",\n" +
                "        \"message\": \"At least one resource deployment operation failed. Please list deployment operations for details. Please see https://aka.ms/arm-deployment-operations for usage details.\"\n" +
                "      },\n" +
                "      \"vMs\": [],\n" +
                "      \"operationDetails\": [\n" +
                "        {\n" +
                "          \"operationId\": \"83BE6B6D1F700B4E\",\n" +
                "          \"targetResource\": \"Microsoft.Resources/deployments/sdp-mc-spot-300-0424-b-01-tsk-0-vm-deploy-393\",\n" +
                "          \"provisionState\": \"Failed\",\n" +
                "          \"timestamp\": \"2023-04-25T05:28:22.500587+00:00\",\n" +
                "          \"duration\": \"00:01:07.1278199\",\n" +
                "          \"deployError\": {\n" +
                "            \"code\": \"DeploymentFailed\",\n" +
                "            \"message\": \"At least one resource deployment operation failed. Please list deployment operations for details. Please see https://aka.ms/arm-deployment-operations for usage details.\"\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}");
        return stringBuffer.toString();
    }

    private String getss2(){
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(" {\n" +
                "  \"jobId\": \"append-cluster-prod-dw-qihua-us5-l-vms-task-7187-20-20230510-034958-997\",\n" +
                "  \"clusterName\": \"prod-dw-qihua-us5-l\",\n" +
                "  \"provisionedVmGroups\": [\n" +
                "    {\n" +
                "      \"groupName\": \"task\",\n" +
                "      \"count\": 0,\n" +
                "      \"virtualMachines\": []\n" +
                "    }\n" +
                "  ],\n" +
                "  \"failedVMs\": [\n" +
                "    \"vm-prod-dw-qihua-us5-l-tsk-7187\",\n" +
                "    \"vm-prod-dw-qihua-us5-l-tsk-7188\",\n" +
                "    \"vm-prod-dw-qihua-us5-l-tsk-7189\",\n" +
                "    \"vm-prod-dw-qihua-us5-l-tsk-7190\",\n" +
                "    \"vm-prod-dw-qihua-us5-l-tsk-7191\",\n" +
                "    \"vm-prod-dw-qihua-us5-l-tsk-7192\",\n" +
                "    \"vm-prod-dw-qihua-us5-l-tsk-7193\",\n" +
                "    \"vm-prod-dw-qihua-us5-l-tsk-7194\",\n" +
                "    \"vm-prod-dw-qihua-us5-l-tsk-7195\",\n" +
                "    \"vm-prod-dw-qihua-us5-l-tsk-7196\",\n" +
                "    \"vm-prod-dw-qihua-us5-l-tsk-7197\",\n" +
                "    \"vm-prod-dw-qihua-us5-l-tsk-7198\",\n" +
                "    \"vm-prod-dw-qihua-us5-l-tsk-7199\",\n" +
                "    \"vm-prod-dw-qihua-us5-l-tsk-7200\",\n" +
                "    \"vm-prod-dw-qihua-us5-l-tsk-7201\",\n" +
                "    \"vm-prod-dw-qihua-us5-l-tsk-7202\",\n" +
                "    \"vm-prod-dw-qihua-us5-l-tsk-7203\",\n" +
                "    \"vm-prod-dw-qihua-us5-l-tsk-7204\",\n" +
                "    \"vm-prod-dw-qihua-us5-l-tsk-7205\",\n" +
                "    \"vm-prod-dw-qihua-us5-l-tsk-7206\"\n" +
                "  ],\n" +
                "  \"provisionStatus\": \"Failed\",\n" +
                "  \"deployDetailResults\": [\n" +
                "    {\n" +
                "      \"deployName\": \"prod-dw-qihua-us5-l-tsk-0-vm-deploy-7187\",\n" +
                "      \"correlationId\": \"3e76e397-7917-4ee8-9c45-a0f1f5c7fb91\",\n" +
                "      \"provisionState\": \"Failed\",\n" +
                "      \"timestamp\": \"2023-05-10T03:50:23.2307116+00:00\",\n" +
                "      \"duration\": \"00:00:19.8431226\",\n" +
                "      \"deployError\": {\n" +
                "        \"code\": \"DeploymentFailed\",\n" +
                "        \"message\": \"At least one resource deployment operation failed. Please list deployment operations for details. Please see https://aka.ms/arm-deployment-operations for usage details.\"\n" +
                "      },\n" +
                "      \"vMs\": [\n" +
                "        \"vm-prod-dw-qihua-us5-l-tsk-7187\",\n" +
                "        \"vm-prod-dw-qihua-us5-l-tsk-7188\",\n" +
                "        \"vm-prod-dw-qihua-us5-l-tsk-7189\",\n" +
                "        \"vm-prod-dw-qihua-us5-l-tsk-7190\",\n" +
                "        \"vm-prod-dw-qihua-us5-l-tsk-7191\",\n" +
                "        \"vm-prod-dw-qihua-us5-l-tsk-7192\",\n" +
                "        \"vm-prod-dw-qihua-us5-l-tsk-7193\",\n" +
                "        \"vm-prod-dw-qihua-us5-l-tsk-7194\",\n" +
                "        \"vm-prod-dw-qihua-us5-l-tsk-7195\",\n" +
                "        \"vm-prod-dw-qihua-us5-l-tsk-7196\",\n" +
                "        \"vm-prod-dw-qihua-us5-l-tsk-7197\",\n" +
                "        \"vm-prod-dw-qihua-us5-l-tsk-7198\",\n" +
                "        \"vm-prod-dw-qihua-us5-l-tsk-7199\",\n" +
                "        \"vm-prod-dw-qihua-us5-l-tsk-7200\",\n" +
                "        \"vm-prod-dw-qihua-us5-l-tsk-7201\",\n" +
                "        \"vm-prod-dw-qihua-us5-l-tsk-7202\",\n" +
                "        \"vm-prod-dw-qihua-us5-l-tsk-7203\",\n" +
                "        \"vm-prod-dw-qihua-us5-l-tsk-7204\",\n" +
                "        \"vm-prod-dw-qihua-us5-l-tsk-7205\",\n" +
                "        \"vm-prod-dw-qihua-us5-l-tsk-7206\"\n" +
                "      ],\n" +
                "    \n" +
                "    },\n" +
                "    {\n" +
                "      \"deployName\": \"append-cluster-prod-dw-qihua-us5-l-vms-task-7187-20\",\n" +
                "      \"correlationId\": \"3e76e397-7917-4ee8-9c45-a0f1f5c7fb91\",\n" +
                "      \"provisionState\": \"Failed\",\n" +
                "      \"timestamp\": \"2023-05-10T03:50:38.0617406+00:00\",\n" +
                "      \"duration\": \"00:00:39.2586722\",\n" +
                "      \"deployError\": {\n" +
                "        \"code\": \"DeploymentFailed\",\n" +
                "        \"message\": \"At least one resource deployment operation failed. Please list deployment operations for details. Please see https://aka.ms/arm-deployment-operations for usage details.\"\n" +
                "      },\n" +
                "      \"vMs\": [],\n" +
                "      \"operationDetails\": [\n" +
                "        {\n" +
                "          \"operationId\": \"20B7EE7ADCA0EA1B\",\n" +
                "          \"targetResource\": \"Microsoft.Resources/deployments/prod-dw-qihua-us5-l-tsk-0-vm-deploy-7187\",\n" +
                "          \"provisionState\": \"Failed\",\n" +
                "          \"timestamp\": \"2023-05-10T03:50:37.3471233+00:00\",\n" +
                "          \"duration\": \"00:00:37.3193127\",\n" +
                "          \"deployError\": {\n" +
                "            \"code\": \"DeploymentFailed\",\n" +
                "            \"message\": \"At least one resource deployment operation failed. Please list deployment operations for details. Please see https://aka.ms/arm-deployment-operations for usage details.\"\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}");
        return stringBuffer.toString();
    }
}