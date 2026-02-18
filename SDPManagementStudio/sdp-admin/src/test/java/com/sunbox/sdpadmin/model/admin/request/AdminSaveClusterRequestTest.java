package com.sunbox.sdpadmin.model.admin.request;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

/**
 * @author: wangda
 * @date: 2023/3/5
 */
class AdminSaveClusterRequestTest {

    @Test
    void validate() {
        AdminSaveClusterRequest request = JSON.parseObject(cfg, AdminSaveClusterRequest.class);
        request.validate();
    }


    String cfg = "{\n" +
            "    \"ambariDbCfgs\": {\n" +
            "        \"account\": \"root\",\n" +
            "        \"password\": \"\",\n" +
            "        \"url\": \"localhost\",\n" +
            "        \"port\": \"3306\",\n" +
            "        \"database\": \"ambaridb\"\n" +
            "    },\n" +
            "    \"ambariPassword\": \"\",\n" +
            "    \"ambariUsername\": \"root\",\n" +
            "    \"clusterCfgs\": [],\n" +
            "    \"clusterName\": \"sdp-hz3A8xu2OPz\",\n" +
            "    \"confClusterScript\": [],\n" +
            "    \"deleteProtected\": \"0\",\n" +
            "    \"hiveMetadataDbCfgs\": {\n" +
            "        \"account\": null,\n" +
            "        \"password\": null,\n" +
            "        \"url\": \"\",\n" +
            "        \"port\": \"3306\",\n" +
            "        \"database\": null\n" +
            "    },\n" +
            "    \"instanceGroupSkuCfgs\": [\n" +
            "        {\n" +
            "            \"cnt\": 1,\n" +
            "            \"vmRole\": \"ambari\",\n" +
            "            \"groupName\": \"ambari\",\n" +
            "            \"dataVolumeSize\": 200,\n" +
            "            \"dataVolumeType\": \"Premium_LRS\",\n" +
            "            \"purchaseType\": \"1\",\n" +
            "            \"spotType\": null,\n" +
            "            \"spotPrice\": null,\n" +
            "            \"memoryGB\": 16,\n" +
            "            \"osVolumeSize\": 300,\n" +
            "            \"osVolumeType\": \"Premium_LRS\",\n" +
            "            \"skuName\": \"Standard_D4s_v5\",\n" +
            "            \"vCPUs\": 4,\n" +
            "            \"dataVolumeCount\": \"1\",\n" +
            "            \"groupCfgs\": [],\n" +
            "            \"clusterId\": null,\n" +
            "            \"groupId\": null,\n" +
            "            \"confGroupElasticScalingData\": null,\n" +
            "            \"enableBeforestartScript\": null,\n" +
            "            \"enableAfterstartScript\": null\n" +
            "        },\n" +
            "        {\n" +
            "            \"cnt\": 2,\n" +
            "            \"vmRole\": \"master\",\n" +
            "            \"groupName\": \"master\",\n" +
            "            \"dataVolumeSize\": 200,\n" +
            "            \"dataVolumeType\": \"Premium_LRS\",\n" +
            "            \"purchaseType\": \"1\",\n" +
            "            \"spotType\": null,\n" +
            "            \"spotPrice\": null,\n" +
            "            \"memoryGB\": 16,\n" +
            "            \"osVolumeSize\": 300,\n" +
            "            \"osVolumeType\": \"Premium_LRS\",\n" +
            "            \"skuName\": \"Standard_D4s_v5\",\n" +
            "            \"vCPUs\": 4,\n" +
            "            \"dataVolumeCount\": \"1\",\n" +
            "            \"groupCfgs\": [],\n" +
            "            \"clusterId\": null,\n" +
            "            \"groupId\": null,\n" +
            "            \"confGroupElasticScalingData\": null,\n" +
            "            \"enableBeforestartScript\": null,\n" +
            "            \"enableAfterstartScript\": null\n" +
            "        },\n" +
            "        {\n" +
            "            \"cnt\": 4,\n" +
            "            \"vmRole\": \"core\",\n" +
            "            \"groupName\": \"core\",\n" +
            "            \"dataVolumeSize\": 200,\n" +
            "            \"dataVolumeType\": \"Premium_LRS\",\n" +
            "            \"purchaseType\": \"1\",\n" +
            "            \"spotType\": null,\n" +
            "            \"spotPrice\": null,\n" +
            "            \"memoryGB\": 16,\n" +
            "            \"osVolumeSize\": 300,\n" +
            "            \"osVolumeType\": \"Premium_LRS\",\n" +
            "            \"skuName\": \"Standard_D4s_v5\",\n" +
            "            \"vCPUs\": 4,\n" +
            "            \"dataVolumeCount\": \"1\",\n" +
            "            \"groupCfgs\": [],\n" +
            "            \"clusterId\": null,\n" +
            "            \"groupId\": null,\n" +
            "            \"confGroupElasticScalingData\": null,\n" +
            "            \"enableBeforestartScript\": null,\n" +
            "            \"enableAfterstartScript\": null\n" +
            "        },\n" +
            "        {\n" +
            "            \"cnt\": 4,\n" +
            "            \"vmRole\": \"task\",\n" +
            "            \"groupName\": \"task-2\",\n" +
            "            \"dataVolumeSize\": 200,\n" +
            "            \"dataVolumeType\": \"Premium_LRS\",\n" +
            "            \"purchaseType\": \"1\",\n" +
            "            \"spotType\": null,\n" +
            "            \"spotPrice\": null,\n" +
            "            \"memoryGB\": 16,\n" +
            "            \"osVolumeSize\": 300,\n" +
            "            \"osVolumeType\": \"Premium_LRS\",\n" +
            "            \"skuName\": \"Standard_D4s_v5\",\n" +
            "            \"vCPUs\": 4,\n" +
            "            \"dataVolumeCount\": \"1\",\n" +
            "            \"groupCfgs\": [],\n" +
            "            \"clusterId\": null,\n" +
            "            \"groupId\": null,\n" +
            "            \"confGroupElasticScalingData\": null,\n" +
            "            \"enableBeforestartScript\": null,\n" +
            "            \"enableAfterstartScript\": null\n" +
            "        },\n" +
            "        {\n" +
            "            \"cnt\": 4,\n" +
            "            \"vmRole\": \"task\",\n" +
            "            \"groupName\": \"task-1\",\n" +
            "            \"dataVolumeSize\": 200,\n" +
            "            \"dataVolumeType\": \"Premium_LRS\",\n" +
            "            \"purchaseType\": \"1\",\n" +
            "            \"spotType\": null,\n" +
            "            \"spotPrice\": null,\n" +
            "            \"memoryGB\": 16,\n" +
            "            \"osVolumeSize\": 300,\n" +
            "            \"osVolumeType\": \"Premium_LRS\",\n" +
            "            \"skuName\": \"Standard_D4s_v5\",\n" +
            "            \"vCPUs\": 4,\n" +
            "            \"dataVolumeCount\": \"1\",\n" +
            "            \"groupCfgs\": [],\n" +
            "            \"clusterId\": null,\n" +
            "            \"groupId\": null,\n" +
            "            \"confGroupElasticScalingData\": null,\n" +
            "            \"enableBeforestartScript\": null,\n" +
            "            \"enableAfterstartScript\": null\n" +
            "        }\n" +
            "    ],\n" +
            "    \"instanceGroupVersion\": {\n" +
            "        \"clusterApps\": [\n" +
            "            {\n" +
            "                \"appName\": \"HDFS\",\n" +
            "                \"appVersion\": \"3.3.2\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"appName\": \"MapReduce2\",\n" +
            "                \"appVersion\": \"3.3.2\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"appName\": \"Yarn\",\n" +
            "                \"appVersion\": \"3.3.2\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"appName\": \"ZooKeeper\",\n" +
            "                \"appVersion\": \"3.5.7\"\n" +
            "            }\n" +
            "        ],\n" +
            "        \"clusterReleaseVer\": \"SDP-1.0\"\n" +
            "    },\n" +
            "    \"isHa\": 1,\n" +
            "    \"keypairId\": \"sdp-sit-ssh-public-key\",\n" +
            "    \"logPath\": \"https://sasdpscriptssit.blob.core.windows.net/hadoop-logs-01\",\n" +
            "    \"masterSecurityGroup\": \"/subscriptions/90a53f69-8043-4628-b38a-39ed2604fea4/resourceGroups/rg-sdp-data-dev-test/providers/Microsoft.Network/networkSecurityGroups/nsg-sdp-clusters-sit-master\",\n" +
            "    \"slaveSecurityGroup\": \"/subscriptions/90a53f69-8043-4628-b38a-39ed2604fea4/resourceGroups/rg-sdp-data-dev-test/providers/Microsoft.Network/networkSecurityGroups/nsg-sdp-clusters-sit-master\",\n" +
            "    \"subNet\": \"/subscriptions/90a53f69-8043-4628-b38a-39ed2604fea4/resourceGroups/rg-sdp-data-dev-test/providers/Microsoft.Network/virtualNetworks/sdp-data-dev-vnet/subnets/subnet-sdp-clusters-sit-large\",\n" +
            "    \"tagMap\": {\n" +
            "        \"for\": \"bugfix35\"\n" +
            "    },\n" +
            "    \"zone\": \"1\",\n" +
            "    \"zoneName\": \"westus3b\",\n" +
            "    \"scene\": \"\",\n" +
            "    \"vmMI\": \"/subscriptions/9b88bd64-d315-48de-96bc-83051ed25fdc/resourcegroups/rg-sdp-dev-test/providers/Microsoft.ManagedIdentity/userAssignedIdentities/isd-sdpsit-identity\",\n" +
            "    \"logMI\": \"/subscriptions/9b88bd64-d315-48de-96bc-83051ed25fdc/resourcegroups/rg-sdp-dev-test/providers/Microsoft.ManagedIdentity/userAssignedIdentities/isd-sdpsit-identity\",\n" +
            "    \"state\": 1,\n" +
            "    \"enableGanglia\": 1,\n" +
            "    \"isEmbedAmbariDb\": 1,\n" +
            "    \"region\": \"uswest3\",\n" +
            "    \"regionName\": \"美国P1中心\",\n" +
            "    \"vnet\": \"Default-VPC\",\n" +
            "    \"vNet\": \"Default-VPC\",\n" +
            "    \"osDiskType\": \"Premium_LRS\",\n" +
            "    \"diskSize\": 300,\n" +
            "    \"subNetName\": \"shein-test/subnet-sdp-clusters-sit-large\",\n" +
            "    \"subNetStart\": \"/subscriptions/90a53f69-8043-4628-b38a-39ed2604fea4/\",\n" +
            "    \"slaveSecurityGroupName\": \"shein-test/nsg-sdp-clusters-sit-master\",\n" +
            "    \"vmMIName\": \"isd-sdpsit-identity\",\n" +
            "    \"vmMITenantId\": \"e446233b-677e-4f72-aba3-4ef2858fead2\",\n" +
            "    \"vmMIClientId\": \"4dbe9613-079f-47a8-82c0-13ca60e456f9\",\n" +
            "    \"logMIName\": \"isd-sdpsit-identity\",\n" +
            "    \"masterSecurityGroupName\": \"shein-test/nsg-sdp-clusters-sit-master\",\n" +
            "    \"srcClusterId\": \"fae0afb0-82a8-41bf-b255-1f55a26bf2ba\"\n" +
            "}";
}