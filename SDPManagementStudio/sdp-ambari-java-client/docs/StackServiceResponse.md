
# StackServiceResponse

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**stackName** | **String** |  |  [optional]
**stackVersion** | **String** |  |  [optional]
**serviceName** | **String** |  |  [optional]
**serviceType** | **String** |  |  [optional]
**displayName** | **String** |  |  [optional]
**userName** | **String** |  |  [optional]
**comments** | **String** |  |  [optional]
**serviceVersion** | **String** |  |  [optional]
**selection** | [**SelectionEnum**](#SelectionEnum) |  |  [optional]
**serviceCheckSupported** | **Boolean** |  |  [optional]
**customCommands** | **List&lt;String&gt;** |  |  [optional]
**configTypes** | [**Map&lt;String, Map&lt;String, Map&lt;String, String&gt;&gt;&gt;**](Map.md) |  |  [optional]
**requiredServices** | **List&lt;String&gt;** |  |  [optional]
**properties** | **Map&lt;String, String&gt;** |  |  [optional]
**credentialStoreSupported** | **Boolean** |  |  [optional]
**credentialStoreEnabled** | **Boolean** |  |  [optional]
**credentialStoreRequired** | **Boolean** |  |  [optional]
**ssoIntegrationSupported** | **Boolean** |  |  [optional]
**ssoIntegrationRequiresKerberos** | **Boolean** |  |  [optional]


<a name="SelectionEnum"></a>
## Enum: SelectionEnum
Name | Value
---- | -----
DEFAULT | &quot;DEFAULT&quot;
TECH_PREVIEW | &quot;TECH_PREVIEW&quot;
MANDATORY | &quot;MANDATORY&quot;
DEPRECATED | &quot;DEPRECATED&quot;



