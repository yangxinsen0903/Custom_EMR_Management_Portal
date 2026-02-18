[[_TOC_]]

# Blob
## POST /api/v1/blobs/{fileName}/{region}
### Parameters
fileName - String
region - String <font color=red>新增字段</font>
### Request body
file - binary
### Responses
```
Code: 200
{
  "result": "Unknown",
  "blobUrl": "string",
  "code": "string",
  "message": "string"
}
```
```
Code: 400
{
  "type": "string",
  "title": "string",
  "status": 0,
  "detail": "string",
  "instance": "string"
}
```
## POST /api/v1/blobs/logs/{serviceName}/{logDate}/{fileName}/{region}
### Parameters
serviceName  - String
logDate - String
fileName - String
region - String <font color=red>新增字段</font>
### Request body
file - binary
### Responses
```
Code: 200
{
  "result": "Unknown",
  "blobUrl": "string",
  "code": "string",
  "message": "string"
}
```
```
Code: 400
{
  "type": "string",
  "title": "string",
  "status": 0,
  "detail": "string",
  "instance": "string"
}
```
# Job
## GET /api/v1/jobs/{id}
### Parameters
id - String //job id, 异步创建资源的时候会返回给客户端job id
### Responses
```
Code: 200
{
  "id": "string",
  "name": "string",
  "type": "Unknown",
  "status": "Unknown"
}
```
```
Code: 400
{
  "type": "string",
  "title": "string",
  "status": 0,
  "detail": "string",
  "instance": "string"
}
```
```
Code: 404
{
  "type": "string",
  "title": "string",
  "status": 0,
  "detail": "string",
  "instance": "string"
}
```
## GET /api/v1/jobs/{id}/provisionDetail
### Parameters
id - String //job id, 异步创建资源的时候会返回给客户端job id
### Responses
<font color=red>创建失败的vm能否获取到</font>
<font color=red>如何获取详细的deployment信息，SDP侧是否真的需要；deployment所有信息能否给出需再确认</font>
```
Code: 200
{
  "jobId": "string",
  "clusterName": "string",
  "provisionedVmGroups": [
    {
      "groupName": "string",
      "count": 0,
      "virtualMachines": [
        {
          "name": "string",
          "hostName": "string",
          "privateIp": "string",
          "zone": "string",
          "tags": {
            "additionalProp1": "string",
            "additionalProp2": "string",
            "additionalProp3": "string"
          }
        }
      ]
    }
  ],
  "failedVMs": [
    "string"
  ],
  "provisionStatus": "Succeed",
  "message": "string",
  "deployDetailResults": [
    {
      "deployName": "string",
      "correlationId": "string",
      "provisionState": "string",
      "timestamp": "2024-05-14T02:50:25.777Z",
      "duration": {
        "ticks": 0,
        "days": 0,
        "hours": 0,
        "milliseconds": 0,
        "minutes": 0,
        "seconds": 0,
        "totalDays": 0,
        "totalHours": 0,
        "totalMilliseconds": 0,
        "totalMinutes": 0,
        "totalSeconds": 0
      },
      "deployError": {
        "code": "string",
        "message": "string"
      },
      "vMs": [
        "string"
      ],
      "operationDetails": [
        {
          "operationId": "string",
          "targetResource": "string",
          "provisionState": "string",
          "timestamp": "2024-05-14T02:50:25.777Z",
          "duration": {
            "ticks": 0,
            "days": 0,
            "hours": 0,
            "milliseconds": 0,
            "minutes": 0,
            "seconds": 0,
            "totalDays": 0,
            "totalHours": 0,
            "totalMilliseconds": 0,
            "totalMinutes": 0,
            "totalSeconds": 0
          },
          "deployError": {
            "code": "string",
            "message": "string"
          }
        }
      ]
    }
  ]
}
```
```
Code: 400
{
  "type": "string",
  "title": "string",
  "status": 0,
  "detail": "string",
  "instance": "string"
}
```
```
Code: 404
{
  "type": "string",
  "title": "string",
  "status": 0,
  "detail": "string",
  "instance": "string"
}
```
# Metadata
## POST /api/v1/metas/{region}
### Parameters
region: string <font color=red>新增字段</font>
### Request body
```
{
  "metadataType": "SupportedRegionList" // SupportedRegionList, SupportedVMSkuList, SupportedDiskSkuList, SupportedSubnetList, SupportedNSGSkuList, SupportedSSHKeyPairList, SupportedManagedIdentityList, SupportedLogsBlobContainerList, SupportedAvailabilityZoneList
  // data的参数类型参考不同metadataType的查询接口，如果是更新必须有id字段
  "data": [
     {  //更新元数据
        "id": 123,
        "region": "westus3",
        "name": "美西数据中心3"，
     }，
     {  //增加元数据
        "region": "eastus",
        "name": "美东数据中心1"，
     }
  ]
}
```
### Responses
```
Code: 200
{
  "code": 200,
  "status": "SUCCESS"，
  "message": "Unknown"
}
```
```
Code: 400
{
  "code": 400,
  "status": "ERROR"，
  "message": "string"
}
```
# Metadata
## DELETE /api/v1/metas/{region}
### Parameters
region: string <font color=red>新增字段</font>
### Request body
```
{
  "metadataType": "SupportedRegionList" // SupportedRegionList, SupportedVMSkuList, SupportedDiskSkuList, SupportedSubnetList, SupportedNSGSkuList, SupportedSSHKeyPairList, SupportedManagedIdentityList, SupportedLogsBlobContainerList, SupportedAvailabilityZoneList
  // 元数据id
  "id": [123,345,555]
}
```
### Responses
```
Code: 200
{
  "code": 200,
  "status": "SUCCESS"，
  "message": "Unknown"
}
```
```
Code: 400
{
  "code": 400,
  "status": "ERROR"，
  "message": "string"
}
```

## GET /api/v1/metas/supportedRegionList
### Parameters
None
### Responses
```
Code: 200
{
  "data": [
    {
      "id": 123,
      "region": "string"   // azure的region，例如"westus3"
      "name": "string"  // sdp中的命名
    }
  ],
  "metadataType": "SupportedRegionList",
}

```
## GET /api/v1/metas/supportedVMSkuList/{region}
### Parameters
region - String <font color=red>新增字段</font>
### Responses
```
Code: 200
{
  "data": [
    {
      "id": 123,
      "name": "string",
      "vCoreCount": 0,
      "memoryGB": 0,
      "tempSSDStorageGB": 0,
      "tempNVMeStorageGB": 0,
      "tempNVMeDisksCount": 0,
      "tempNVMeDiskSizeGB": 0,
      "maxDataDisksCount": 0,
      "referenceLink": "string"
    }
  ],
  "metadataType": "SupportedVMSkuList",
  "region": "west us 3"
}
```
```
Code: 400
{
  "type": "string",
  "title": "string",
  "status": 0,
  "detail": "string",
  "instance": "string"
}
```
## GET /api/v1/metas/supportedDiskSkuList/{region}
### Parameters
region - String <font color=red>新增字段</font>
### Responses
```
Code: 200
{
  "data": [
    {
      "id": 123,
      "name": "string"
    }
  ],
  "metadataType": "SupportedDiskSkuList",
  "region": "west us 3"
}
```
```
Code: 400
{
  "type": "string",
  "title": "string",
  "status": 0,
  "detail": "string",
  "instance": "string"
}
```
## GET /api/v1/metas/supportedSubnetList/{region}
### Parameters
region - String <font color=red>新增字段</font>
### Responses
```
Code: 200
{
  "data": [
    {
      "id":123,
      "name": "string",
      "resourceId": "string"
    }
  ],
  "metadataType": "SupportedSubnetList",
  "region": "west us 3"
}
```
```
Code: 400
{
  "type": "string",
  "title": "string",
  "status": 0,
  "detail": "string",
  "instance": "string"
}
```
## GET /api/v1/metas/supportedNSGSkuList/{region}
### Parameters
region - String <font color=red>新增字段</font>
### Responses
```
Code: 200
{
  "data": [
    {
      "id":123,
      "name": "string",
      "resourceId": "string"
    }
  ],
  "metadataType": "SupportedNSGSkuList",
  "region": "west us 3"
}
```
```
Code: 400
{
  "type": "string",
  "title": "string",
  "status": 0,
  "detail": "string",
  "instance": "string"
}
```
## GET /api/v1/metas/supportedSSHKeyPairList/{region}
### Parameters
region - String <font color=red>新增字段</font>
### Responses
```
Code: 200
{
  "data": [
    {
      "id":123,
      "name": "string",
      "keyVaultResourceId": "string",
      "privateKeySecretName": "string",
      "publicKeySecretName": "string"
    }
  ],
  "metadataType": "SupportedSSHKeyPairList",
  "region": "west us 3"
}
```
```
Code: 400
{
  "type": "string",
  "title": "string",
  "status": 0,
  "detail": "string",
  "instance": "string"
}
```
## GET /api/v1/metas/supportedManagedIdentityList/{region}
### Parameters
region - String <font color=red>新增字段</font>
### Responses
```
Code: 200
{
  "data": [
    {
      "id":123,
      "name": "string",
      "tenantId": "string",
      "clientId": "string",
      "resourceId": "string"
    }
  ],
  "metadataType": "SupportedManagedIdentityList",
  "region": "west us 3"
}
```
```
Code: 400
{
  "type": "string",
  "title": "string",
  "status": 0,
  "detail": "string",
  "instance": "string"
}
```
## GET /api/v1/metas/supportedLogsBlobContainerList/{region}
### Parameters
region - String <font color=red>新增字段</font>
### Responses
```
Code: 200
{
  "data": [
    {
      "id":123,
      "name": "string",
      "blobContainerUrl": "string"
    }
  ],
  "metadataType": "SupportedLogsBlobContainerList",
  "region": "west us 3"
}
```
```
Code: 400
{
  "type": "string",
  "title": "string",
  "status": 0,
  "detail": "string",
  "instance": "string"
}
```
## GET /api/v1/metas/supportedAvailabilityZoneList/{region}
### Parameters
region - String <font color=red>新增字段</font>
### Responses
```
Code: 200
{
  "data": [
    {
      "id":123,
      "name": "string",
      "physicalZone": "1",
      "logicalZone": "3"
    }
  ],
  "metadataType": "SupportedAvailabilityZoneList",
  "region": "west us 3"
}
```
```
Code: 400
{
  "type": "string",
  "title": "string",
  "status": 0,
  "detail": "string",
  "instance": "string"
}
```
# Price
## GET /api/v1/price/spotInstance/{region}
### Parameters
region - String <font color=red>新增字段</font>
### Request body
```
[
  "skuName1",
  "skuName2"
]
```
### Responses
```
Code: 200
{
  "spotInstanceList": [
    {
      "vmSkuName": "string",
      "spotUnitPricePerHourUSD": 0,
      "onDemandUnitPricePerHourUSD": 0,
    }
  ],
  "region": "west us 3"
}
```
```
Code: 400
{
  "type": "string",
  "title": "string",
  "status": 0,
  "detail": "string",
  "instance": "string"
}
```
## POST /api/v1/price/spotEvictionRate/{region}
### Parameters
region: string <font color=red>新增字段</font>
### Request body
```
[
  "skuName1",
  "skuName2"
]
```
### Responses
```
Code: 200
{
  "spotEvictionRateList": [
    {
      "skuName": "string",
      "evictionRateLowerPercentage": 0,
      "evictionRateUpperPercentage": 0
    }
  ],
  "region": "west us 3"
}
```
```
Code: 400
{
  "type": "string",
  "title": "string",
  "status": 0,
  "detail": "string",
  "instance": "string"
}
```
## POST /api/v1/price/spotPriceHistory/{region}
### Parameters
region: string <font color=red>新增字段</font>
### Request body
```
[
  "skuName1",
  "skuName2"
]
```
### Responses
```
Code: 200
{
  "spotPriceHistoryList": [
    {
      "skuName": "string",
      "priceItems": [
        {
          "effectiveDate": "2024-05-14T03:30:57.174Z",
          "unitPricePerHourUSD": 0
        }
      ]
    }
  ],
  "region": "west us 3"
}
```
```
Code: 400
{
  "type": "string",
  "title": "string",
  "status": 0,
  "detail": "string",
  "instance": "string"
}
```
# VirtualMachine
## POST /api/v1/vms
### Parameters
None
### Request body
<font color=red>region为新增字段</font>
```
{
  "apiVersion": "string",
  "transactionId": "string",
  "requestTimestamp": "string",
  "region": "string",  // 增加region字段
  "clusterName": "string",
  "virtualMachineGroups": [
    {
      "groupName": "string",  // 对应Azure fleet的name
      "count": 0,
      "beginIndex": 0,
      "provisionType": "Azure_Fleet", // 该字段可删除，或固定为Azure_fleet
      "virtualMachineSpec": {
        // Azure fleet支持多sku
        "skuName": [
           "standard_d2s_v5",
           "standard_e2s_v5"
        ],
        "baseProfile": {
            "osImageType": "MarketplaceImage",
            "customOSImageId": "string",  // azure fleet是否支持customOSImageId参数，支持
            "marketplaceOSImageName": "string", 
            "hostNameSuffix": "string", // azure fleet是否支持hostNameSuffix
            "userName": "string",
            "sshPublicKeyType": "PlainText",  // azure fleet中ssh key的参数格式是什么，支持
            "sshPublicKeySecretName": "string",
            "sshPublicKey": "string",
            "subnetResourceId": "string",
            "nsgResourceId": "string",  // azure fleet如何配置nsg，支持
            "osDiskSku": "string",
            "osDiskSizeGB": 0,
            "dataDiskSku": "string", // azure fleet中data disk的参数格式是什么，支持
            "dataDiskSizeGB": 0,
            "dataDiskCount": 0,
            "startupScriptBlobUrl": "string", // azure fleet是否支持启动脚本，参数格式是什么，支持
            "zone": "string",
            "secondaryZone": "string",
            "userAssignedIdentityResourceIds": [    // azure fleet是否支持userAssignedIdentity的绑定，支持
               "string"
            ]
        },
        "spotProfile": {
           "capacity": 50,
           "minCapacity": 5,
           "maxPricePerVM": "0.05",
           "allocationStrategy": "CapacityOptimized",
           "evictionPolicy": "Delete"
           // "evictionType": "None",
           // "evictionPolicy": "None",
           // "maxPricePerHour": 0
        },
        "regularPriorityProfile": {
            "capacity": 5,
            "minCapacity": 2,
            "allocationStrategy": "LowestPrice"
        },
        "virtualMachineTags": {  // azure fleet是否支持给vm打tag，支持
          "additionalProp1": "string",
          "additionalProp2": "string",
          "additionalProp3": "string"
        },
        "request": {
            "type": "Restore"   // azure fleet一共支持几种type；字段格式会变更，具体等PG给出具体例子
        }
      }
    }
  ],
  "clusterTags": {
    "additionalProp1": "string",
    "additionalProp2": "string",
    "additionalProp3": "string"
  }
}
```
### Responses
```
Code: 200
{
  "id": "string",
  "name": "string",
  "type": "Unknown",
  "status": "Unknown"
}
```
```
Code: 400
{
  "type": "string",
  "title": "string",
  "status": 0,
  "detail": "string",
  "instance": "string"
}
```
## PUT /api/v1/vms/updateVMsDiskSize
<font color=red>azure fleet如何对已存在vm更新disk</font>
<font color=red>当前的更新功能只对后续scale的vm生效</font>
### Parameters
None
### Request body
<font color=red>region为新增字段</font>
<font color=red>groupName为新增字段</font>
```
{
  "apiVersion": "string",
  "transactionId": "string",
  "requestTimestamp": "string",
  "region": "west us 3", // 如果clusterName可以唯一指定一个集群，则该字段可以删除
  "clusterName": "string",
  "groupName": "string",
  // 如果不传vmNames参数，则表示整个实例组vm的磁盘都更新，包括后续扩容vm的磁盘；如果传vmNames参数，则表示只更新所选vm的磁盘大小，其他vm包括后续扩容的不受影响
  "vmNames": [
    "string"
  ],
  "newDataDiskSizeGB": 0  // azure fleet如何支持update disk，测试一下
}
```
### Responses
```
Code: 200
{
  "id": "string",
  "name": "string",
  "type": "Unknown",
  "status": "Unknown"
}
```
```
Code: 400
{
  "type": "string",
  "title": "string",
  "status": 0,
  "detail": "string",
  "instance": "string"
}
```
## PUT /api/v1/vms/appendVirtualMachines
### Parameters
None
### Request body
<font color=red>region为新增字段</font>
```
{
  "apiVersion": "string",
  "transactionId": "string",
  "requestTimestamp": "string",
  "region": "string",  // 如果clusterName可以唯一指定一个集群，则该字段可以删除
  "clusterName": "string",
  "virtualMachineGroups": [
    {
      "groupName": "string",  // 对应Azure fleet的name
      "count": 0,
      "beginIndex": 0,
      "provisionType": "Azure_Fleet", // 该字段可删除，或固定为Azure_fleet
      "virtualMachineSpec": {
        // Azure fleet支持多sku
        "skuName": [
           "standard_d2s_v5",
           "standard_e2s_v5"
        ],
        "baseProfile": {
            "osImageType": "MarketplaceImage",
            "customOSImageId": "string",  // azure fleet是否支持customOSImageId参数，支持
            "marketplaceOSImageName": "string", 
            "hostNameSuffix": "string",
            "userName": "string",
            "sshPublicKeyType": "PlainText",  // azure fleet中ssh key的参数是什么， 支持
            "sshPublicKeySecretName": "string",
            "sshPublicKey": "string",
            "subnetResourceId": "string",
            "nsgResourceId": "string",  // azure fleet如何配置nsg， 支持
            "osDiskSku": "string",
            "osDiskSizeGB": 0,
            "dataDiskSku": "string", // azure fleet中data disk的参数格式是什么， 支持
            "dataDiskSizeGB": 0,
            "dataDiskCount": 0,
            "startupScriptBlobUrl": "string", // azure fleet是否支持启动脚本，支持
            "zone": "string",
            "secondaryZone": "string",
            "userAssignedIdentityResourceIds": [    // azure fleet是否支持userAssignedIdentity的绑定，支持
               "string"
            ]
        },
        "spotProfile": {
           "capacity": 50,
           "minCapacity": 5,
           "maxPricePerVM": "0.05", 
           "allocationStrategy": "CapacityOptimized",
           "evictionPolicy": "Delete"
           // "evictionType": "None",
           // "evictionPolicy": "None",
           // "maxPricePerHour": 0
        },
        "regularPriorityProfile": {
            "capacity": 5,
            "minCapacity": 2,
            "allocationStrategy": "LowestPrice"
        },
        "virtualMachineTags": {  
          "additionalProp1": "string",
          "additionalProp2": "string",
          "additionalProp3": "string"
        },
        "request": {
            "type": "Restore"   // azure fleet一共支持几种type；字段格式可能变更，具体等PG确认
        }
      }
    }
  ]
}
```
### Responses
```
Code: 200
{
  "id": "string",
  "name": "string",
  "type": "Unknown",
  "status": "Unknown"
}
```
```
Code: 400
{
  "type": "string",
  "title": "string",
  "status": 0,
  "detail": "string",
  "instance": "string"
}
```
## DELETE /api/v1/vms/{region}/{vmName}
### Parameters
region: string  <font color=red>新增字段</font>
vmName: string
### Responses
```
Code: 200
{
  "id": "string",
  "name": "string",
  "type": "Unknown",
  "status": "Unknown"
}
```
```
Code: 400
{
  "type": "string",
  "title": "string",
  "status": 0,
  "detail": "string",
  "instance": "string"
}
```


## PUT /api/v1/vms/deleteVirtualMachines
### Parameters
None
### Request body
<font color=red>region为新增字段</font>
```
{
  "apiVersion": "string",
  "transactionId": "string",
  "requestTimestamp": "string",
  "region": "westus3", // 如果clusterName可以唯一指定一个集群，则该字段可以删除
  "clusterName": "string",
  "vmNames": [
    "string"
  ]
}
```
### Responses
```
Code: 200
{
  "id": "string",
  "name": "string",
  "type": "Unknown",
  "status": "Unknown"
}
```
```
Code: 400
{
  "type": "string",
  "title": "string",
  "status": 0,
  "detail": "string",
  "instance": "string"
}
```

## GET /api/v1/vms/listAll/{region}
<font color=red>查询量较大，是否存在性能问题，实现时再分析，当前保持现状</font>
### Parameters
region: string <font color=red>新增字段</font>
### Responses
```
Code: 200
{
  "code": "string",
  "message": "string",
  "data": [
    {
      "groupName": "string",
      "count": 0,
      "virtualMachines": [
        {
          "name": "string",
          "uniqueId": "string",
          "hostName": "string",
          "privateIp": "string",
          "zone": "string",
          "tags": {
            "additionalProp1": "string",
            "additionalProp2": "string",
            "additionalProp3": "string"
          }
        }
      ]
    }
  ]
}
```
```
Code: 400
{
  "type": "string",
  "title": "string",
  "status": 0,
  "detail": "string",
  "instance": "string"
}
```

# ResourceGroup
## POST /api/v1/rgs/{region}     <font color=red>Deprecated</font>
### Parameters
region: string
### Request body
```
{
  "apiVersion": "string",
  "transactionId": "string",
  "requestTimestamp": "string",
  "name": "string",
  "tags": {
    "additionalProp1": "string",
    "additionalProp2": "string",
    "additionalProp3": "string"
  }
}
```
### Responses
```
Code: 200
{
  "name": "string",
  "location": "string",
  "tags": {
    "additionalProp1": "string",
    "additionalProp2": "string",
    "additionalProp3": "string"
  }
}
```
```
Code: 400
{
  "type": "string",
  "title": "string",
  "status": 0,
  "detail": "string",
  "instance": "string"
}
```
## GET /api/v1/rgs/{region}/{name}
### Parameters
region: string <font color=red>新增字段</font>
name: string
### Responses
```
Code: 200
{
  "name": "string",
  "location": "string",
  "tags": {
    "additionalProp1": "string",
    "additionalProp2": "string",
    "additionalProp3": "string"
  }
}
```
```
Code: 400
{
  "type": "string",
  "title": "string",
  "status": 0,
  "detail": "string",
  "instance": "string"
}
```
```
Code: 404
{
  "type": "string",
  "title": "string",
  "status": 0,
  "detail": "string",
  "instance": "string"
}
```
## DELETE /api/v1/rgs/{region}/{name}
### Parameters
region: string <font color=red>新增字段</font>
name: string
### Responses
```
Code: 200
{
  "id": "string",
  "name": "string",
  "type": "Unknown",
  "status": "Unknown"
}
```
```
Code: 400
{
  "type": "string",
  "title": "string",
  "status": 0,
  "detail": "string",
  "instance": "string"
}
```
```
Code: 404
{
  "type": "string",
  "title": "string",
  "status": 0,
  "detail": "string",
  "instance": "string"
}
```
## PUT /api/v1/rgs/{region}/{name}/updateTags
### Parameters
region: string <font color=red>新增字段</font>
name: string
### Request body
```
{
  "apiVersion": "string",
  "transactionId": "string",
  "requestTimestamp": "string",
  "tags": {
    "additionalProp1": "string",
    "additionalProp2": "string",
    "additionalProp3": "string"
  }
}
```
### Responses
```
Code: 200
{
  "name": "string",
  "location": "string",
  "tags": {
    "additionalProp1": "string",
    "additionalProp2": "string",
    "additionalProp3": "string"
  }
}
```
```
Code: 400
{
  "type": "string",
  "title": "string",
  "status": 0,
  "detail": "string",
  "instance": "string"
}
```
```
Code: 404
{
  "type": "string",
  "title": "string",
  "status": 0,
  "detail": "string",
  "instance": "string"
}
```
## PUT /api/v1/rgs/{region}/{name}/addTags
### Parameters
region: string <font color=red>新增字段</font>
name: string
### Request body
```
{
  "apiVersion": "string",
  "transactionId": "string",
  "requestTimestamp": "string",
  "tags": [
    {
      "tagName": "string",
      "tagValue": "string"
    }
  ]
}
```
### Responses
```
Code: 200
{
  "name": "string",
  "location": "string",
  "tags": {
    "additionalProp1": "string",
    "additionalProp2": "string",
    "additionalProp3": "string"
  }
}
```
```
Code: 400
{
  "type": "string",
  "title": "string",
  "status": 0,
  "detail": "string",
  "instance": "string"
}
```
```
Code: 404
{
  "type": "string",
  "title": "string",
  "status": 0,
  "detail": "string",
  "instance": "string"
}
```
## PUT /api/v1/rgs/{region}/{name}/deleteTags
### Parameters
region: string <font color=red>新增字段</font>
name: string
### Request body
```
{
  "apiVersion": "string",
  "transactionId": "string",
  "requestTimestamp": "string",
  "tagNames": [
    "string"
  ]
}
```
### Responses
```
Code: 200
{
  "name": "string",
  "location": "string",
  "tags": {
    "additionalProp1": "string",
    "additionalProp2": "string",
    "additionalProp3": "string"
  }
}
```
```
Code: 400
{
  "type": "string",
  "title": "string",
  "status": 0,
  "detail": "string",
  "instance": "string"
}
```
```
Code: 404
{
  "type": "string",
  "title": "string",
  "status": 0,
  "detail": "string",
  "instance": "string"
}
```
















