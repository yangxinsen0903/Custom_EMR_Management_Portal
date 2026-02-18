# Service Bus 信息

service bus： servicebus-sdp-wu2
topic： vm_info_from_rm_to_sdp
subscription：sdp-consumer

# 报文格式
```json 
{
    "name": "sdp-cluster-3-eastus2-group-1_c2342_18df47d9",
    "hostName": "sdp-cluster-3-eastus2-group-1-6WFW0P", // 
    "uniqueId": "d34e8bdf-bca3-4fe5-8ee2-b025c371d9db", // vmid
    "privateIp": "10.0.0.20",            // ip
    "zone": "1",                         // 可用区
    "tags": {
        "cluster": "cluster-3-eastus2",    // 集群名
        "VirtualMachineProfileTimeCreated": "6/14/2024 12:18:49 AM +00:00",
        "clusterName": "cluster-3-eastus2", // 集群名
        "name": "eric",
        "SYS_CREATE_BATCH": "f47f499d0dc8842c8bf186e26b7032d7",
        "hello": "sdp",
        "SYS_SDP_CLUSTER": "cluster-3-eastus2",
        "group": "group-1",     // 实例组名
        "SYS_SDP_GROUP": "group-1"
    },
    "priority": "Spot",    // 竞价实例
    "vmState": "Creating"
}
```

示例报文:
```json
{
    "name": "sdp-cluster-3-eastus2-group-1_c2342_18df47d9",
    "hostName": "sdp-cluster-3-eastus2-group-1-6WFW0P",
    "uniqueId": "d34e8bdf-bca3-4fe5-8ee2-b025c371d9db",
    "privateIp": "10.0.0.20", 
    "zone": "1",  
    "tags": {
        "cluster": "cluster-3-eastus2",
        "VirtualMachineProfileTimeCreated": "6/14/2024 12:18:49 AM +00:00",
        "clusterName": "cluster-3-eastus2", 
        "name": "eric",
        "SYS_CREATE_BATCH": "f47f499d0dc8842c8bf186e26b7032d7",
        "hello": "sdp",
        "SYS_SDP_CLUSTER": "cluster-3-eastus2",
        "group": "group-1", 
        "SYS_SDP_GROUP": "group-1"
    },
    "priority": "Spot",  
    "vmState": "Creating"
}
```
