
# ClusterRequest

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**clusterId** | **Long** |  |  [optional]
**clusterName** | **String** |  |  [optional]
**version** | **String** |  |  [optional]
**provisioningState** | **String** |  |  [optional]
**securityType** | [**SecurityTypeEnum**](#SecurityTypeEnum) |  |  [optional]
**desiredServiceConfigVersions** | [**ServiceConfigVersionRequest**](ServiceConfigVersionRequest.md) |  |  [optional]
**desiredConfigs** | [**List&lt;ConfigurationRequest&gt;**](ConfigurationRequest.md) |  |  [optional]


<a name="SecurityTypeEnum"></a>
## Enum: SecurityTypeEnum
Name | Value
---- | -----
NONE | &quot;NONE&quot;
KERBEROS | &quot;KERBEROS&quot;



