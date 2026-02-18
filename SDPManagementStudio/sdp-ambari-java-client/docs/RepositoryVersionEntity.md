
# RepositoryVersionEntity

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **Long** |  |  [optional]
**stack** | [**StackEntity**](StackEntity.md) |  |  [optional]
**version** | **String** |  |  [optional]
**displayName** | **String** |  |  [optional]
**repoOsEntities** | [**List&lt;RepoOsEntity&gt;**](RepoOsEntity.md) |  |  [optional]
**type** | [**TypeEnum**](#TypeEnum) |  |  [optional]
**versionXml** | **String** |  |  [optional]
**versionUrl** | **String** |  |  [optional]
**versionXsd** | **String** |  |  [optional]
**resolved** | **Boolean** |  |  [optional]
**children** | [**List&lt;RepositoryVersionEntity&gt;**](RepositoryVersionEntity.md) |  |  [optional]
**hidden** | **Boolean** |  |  [optional]
**stackId** | [**StackId**](StackId.md) |  |  [optional]
**repositoryXml** | [**VersionDefinitionXml**](VersionDefinitionXml.md) |  |  [optional]
**stackVersion** | **String** |  |  [optional]
**stackName** | **String** |  |  [optional]
**legacy** | **Boolean** |  |  [optional]
**parentId** | **Long** |  |  [optional]


<a name="TypeEnum"></a>
## Enum: TypeEnum
Name | Value
---- | -----
STANDARD | &quot;STANDARD&quot;
PATCH | &quot;PATCH&quot;
MAINT | &quot;MAINT&quot;
SERVICE | &quot;SERVICE&quot;



