
# ClusterResponse

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**clusterId** | **Long** |  |  [optional]
**clusterName** | **String** |  |  [optional]
**version** | **String** |  |  [optional]
**provisioningState** | [**ProvisioningStateEnum**](#ProvisioningStateEnum) |  |  [optional]
**securityType** | [**SecurityTypeEnum**](#SecurityTypeEnum) |  |  [optional]
**totalHosts** | **Integer** |  |  [optional]
**desiredConfigs** | [**Map&lt;String, DesiredConfig&gt;**](DesiredConfig.md) |  |  [optional]
**desiredServiceConfigVersions** | [**Map&lt;String, List&lt;ServiceConfigVersionResponse&gt;&gt;**](List.md) |  |  [optional]
**healthReport** | [**ClusterHealthReport**](ClusterHealthReport.md) |  |  [optional]
**credentialStoreProperties** | **Map&lt;String, String&gt;** |  |  [optional]


<a name="ProvisioningStateEnum"></a>
## Enum: ProvisioningStateEnum
Name | Value
---- | -----
INIT | &quot;INIT&quot;
INSTALLING | &quot;INSTALLING&quot;
INSTALL_FAILED | &quot;INSTALL_FAILED&quot;
INSTALLED | &quot;INSTALLED&quot;
STARTING | &quot;STARTING&quot;
STARTED | &quot;STARTED&quot;
STOPPING | &quot;STOPPING&quot;
UNINSTALLING | &quot;UNINSTALLING&quot;
UNINSTALLED | &quot;UNINSTALLED&quot;
WIPING_OUT | &quot;WIPING_OUT&quot;
UPGRADING | &quot;UPGRADING&quot;
DISABLED | &quot;DISABLED&quot;
UNKNOWN | &quot;UNKNOWN&quot;


<a name="SecurityTypeEnum"></a>
## Enum: SecurityTypeEnum
Name | Value
---- | -----
NONE | &quot;NONE&quot;
KERBEROS | &quot;KERBEROS&quot;



