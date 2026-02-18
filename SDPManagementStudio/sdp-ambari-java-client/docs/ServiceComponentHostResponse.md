
# ServiceComponentHostResponse

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**clusterName** | **String** |  |  [optional]
**serviceName** | **String** |  |  [optional]
**componentName** | **String** |  |  [optional]
**displayName** | **String** |  |  [optional]
**publicHostName** | **String** |  |  [optional]
**hostName** | **String** |  |  [optional]
**actualConfigs** | [**Map&lt;String, HostConfig&gt;**](HostConfig.md) |  |  [optional]
**state** | **String** |  |  [optional]
**version** | **String** |  |  [optional]
**desiredStackId** | **String** |  |  [optional]
**desiredRepositoryVersion** | **String** |  |  [optional]
**desiredState** | **String** |  |  [optional]
**staleConfigs** | **Boolean** |  |  [optional]
**reloadConfigs** | **Boolean** |  |  [optional]
**maintenanceState** | **String** |  |  [optional]
**upgradeState** | [**UpgradeStateEnum**](#UpgradeStateEnum) |  |  [optional]


<a name="UpgradeStateEnum"></a>
## Enum: UpgradeStateEnum
Name | Value
---- | -----
NONE | &quot;NONE&quot;
COMPLETE | &quot;COMPLETE&quot;
IN_PROGRESS | &quot;IN_PROGRESS&quot;
FAILED | &quot;FAILED&quot;
VERSION_MISMATCH | &quot;VERSION_MISMATCH&quot;



