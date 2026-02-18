
# StackConfigurationResponse

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**stackName** | **String** |  |  [optional]
**stackVersion** | **String** |  |  [optional]
**serviceName** | **String** |  |  [optional]
**propertyName** | **String** |  |  [optional]
**propertyValue** | **String** |  |  [optional]
**propertyDescription** | **String** |  |  [optional]
**propertyDisplayName** | **String** |  |  [optional]
**type** | **String** |  |  [optional]
**propertyValueAttributes** | [**ValueAttributesInfo**](ValueAttributesInfo.md) |  |  [optional]
**dependencies** | [**List&lt;PropertyDependencyInfo&gt;**](PropertyDependencyInfo.md) |  |  [optional]
**propertyType** | [**List&lt;PropertyTypeEnum&gt;**](#List&lt;PropertyTypeEnum&gt;) |  |  [optional]


<a name="List<PropertyTypeEnum>"></a>
## Enum: List&lt;PropertyTypeEnum&gt;
Name | Value
---- | -----
PASSWORD | &quot;PASSWORD&quot;
USER | &quot;USER&quot;
UID | &quot;UID&quot;
GROUP | &quot;GROUP&quot;
GID | &quot;GID&quot;
TEXT | &quot;TEXT&quot;
ADDITIONAL_USER_PROPERTY | &quot;ADDITIONAL_USER_PROPERTY&quot;
NOT_MANAGED_HDFS_PATH | &quot;NOT_MANAGED_HDFS_PATH&quot;
VALUE_FROM_PROPERTY_FILE | &quot;VALUE_FROM_PROPERTY_FILE&quot;
KERBEROS_PRINCIPAL | &quot;KERBEROS_PRINCIPAL&quot;



