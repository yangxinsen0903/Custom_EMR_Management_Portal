
# HostResponse

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**hostName** | **String** |  |  [optional]
**clusterName** | **String** |  |  [optional]
**ip** | **String** |  |  [optional]
**cpuCount** | **Long** |  |  [optional]
**phCpuCount** | **Long** |  |  [optional]
**osArch** | **String** |  |  [optional]
**osFamily** | **String** |  |  [optional]
**osType** | **String** |  |  [optional]
**totalMem** | **Long** |  |  [optional]
**diskInfo** | [**List&lt;DiskInfo&gt;**](DiskInfo.md) |  |  [optional]
**lastHeartbeatTime** | **Long** |  |  [optional]
**lastAgentEnv** | [**AgentEnv**](AgentEnv.md) |  |  [optional]
**lastRegistrationTime** | **Long** |  |  [optional]
**rackInfo** | **String** |  |  [optional]
**recoveryReport** | [**RecoveryReport**](RecoveryReport.md) |  |  [optional]
**recoverySummary** | **String** |  |  [optional]
**hostState** | [**HostStateEnum**](#HostStateEnum) |  |  [optional]
**desiredConfigs** | [**Map&lt;String, HostConfig&gt;**](HostConfig.md) |  |  [optional]
**hostStatus** | **String** |  |  [optional]
**maintenanceState** | [**MaintenanceStateEnum**](#MaintenanceStateEnum) |  |  [optional]
**hostHealthReport** | **String** |  |  [optional]
**publicHostName** | **String** |  |  [optional]


<a name="HostStateEnum"></a>
## Enum: HostStateEnum
Name | Value
---- | -----
INIT | &quot;INIT&quot;
WAITING_FOR_HOST_STATUS_UPDATES | &quot;WAITING_FOR_HOST_STATUS_UPDATES&quot;
HEALTHY | &quot;HEALTHY&quot;
HEARTBEAT_LOST | &quot;HEARTBEAT_LOST&quot;
UNHEALTHY | &quot;UNHEALTHY&quot;


<a name="MaintenanceStateEnum"></a>
## Enum: MaintenanceStateEnum
Name | Value
---- | -----
OFF | &quot;OFF&quot;
ON | &quot;ON&quot;
IMPLIED_FROM_SERVICE | &quot;IMPLIED_FROM_SERVICE&quot;
IMPLIED_FROM_HOST | &quot;IMPLIED_FROM_HOST&quot;
IMPLIED_FROM_SERVICE_AND_HOST | &quot;IMPLIED_FROM_SERVICE_AND_HOST&quot;



