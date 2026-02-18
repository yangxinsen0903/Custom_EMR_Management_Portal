
# ViewInstanceResponseInfo

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**viewName** | **String** |  |  [optional]
**version** | **String** |  |  [optional]
**instanceName** | **String** |  |  [optional]
**label** | **String** |  |  [optional]
**description** | **String** |  |  [optional]
**visible** | **Boolean** |  |  [optional]
**iconPath** | **String** |  |  [optional]
**icon64Path** | **String** |  |  [optional]
**properties** | **Map&lt;String, String&gt;** |  |  [optional]
**instanceData** | **Map&lt;String, String&gt;** |  |  [optional]
**clusterHandle** | **Integer** |  |  [optional]
**clusterType** | [**ClusterTypeEnum**](#ClusterTypeEnum) |  |  [optional]
**contextPath** | **String** |  |  [optional]
**_static** | **Boolean** |  |  [optional]
**shortUrl** | **String** |  |  [optional]
**shortUrlName** | **String** |  |  [optional]
**validationResult** | [**ValidationResult**](ValidationResult.md) |  |  [optional]
**propertyValidationResults** | [**Map&lt;String, ValidationResult&gt;**](ValidationResult.md) |  |  [optional]


<a name="ClusterTypeEnum"></a>
## Enum: ClusterTypeEnum
Name | Value
---- | -----
LOCAL_AMBARI | &quot;LOCAL_AMBARI&quot;
REMOTE_AMBARI | &quot;REMOTE_AMBARI&quot;
NONE | &quot;NONE&quot;



