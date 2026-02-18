
# ServiceResponse

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**clusterName** | **String** |  |  [optional]
**serviceName** | **String** |  |  [optional]
**desiredRepositoryVersion** | **String** |  |  [optional]
**desiredRepositoryVersionId** | **Long** |  |  [optional]
**repositoryVersionState** | [**RepositoryVersionStateEnum**](#RepositoryVersionStateEnum) |  |  [optional]
**state** | **String** |  |  [optional]
**maintenanceState** | **String** |  |  [optional]
**credentialStoreSupported** | **Boolean** |  |  [optional]
**credentialStoreEnabled** | **Boolean** |  |  [optional]
**ssoIntegrationSupported** | **Boolean** |  |  [optional]
**ssoIntegrationDesired** | **Boolean** |  |  [optional]
**ssoIntegrationEnabled** | **Boolean** |  |  [optional]
**ssoIntegrationRequiresKerberos** | **Boolean** |  |  [optional]
**kerberosEnabled** | **Boolean** |  |  [optional]


<a name="RepositoryVersionStateEnum"></a>
## Enum: RepositoryVersionStateEnum
Name | Value
---- | -----
NOT_REQUIRED | &quot;NOT_REQUIRED&quot;
INSTALLING | &quot;INSTALLING&quot;
INSTALLED | &quot;INSTALLED&quot;
INSTALL_FAILED | &quot;INSTALL_FAILED&quot;
OUT_OF_SYNC | &quot;OUT_OF_SYNC&quot;
CURRENT | &quot;CURRENT&quot;



