
# UserResponse

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**displayName** | **String** |  |  [optional]
**userType** | [**UserTypeEnum**](#UserTypeEnum) |  |  [optional]
**groups** | **List&lt;String&gt;** |  |  [optional]
**created** | [**OffsetDateTime**](OffsetDateTime.md) |  |  [optional]
**consecutiveFailures** | **Integer** |  |  [optional]
**active** | **Boolean** |  |  [optional]
**userName** | **String** |  |  [optional]
**admin** | **Boolean** |  |  [optional]
**ldapUser** | **Boolean** |  |  [optional]
**localUserName** | **String** |  |  [optional]


<a name="UserTypeEnum"></a>
## Enum: UserTypeEnum
Name | Value
---- | -----
LOCAL | &quot;LOCAL&quot;
LDAP | &quot;LDAP&quot;
JWT | &quot;JWT&quot;
PAM | &quot;PAM&quot;
KERBEROS | &quot;KERBEROS&quot;



