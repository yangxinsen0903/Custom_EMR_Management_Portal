
# UserAuthenticationSourceResponse

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**userName** | **String** |  | 
**sourceId** | **Long** |  | 
**authenticationType** | [**AuthenticationTypeEnum**](#AuthenticationTypeEnum) |  | 
**key** | **String** |  |  [optional]
**created** | [**OffsetDateTime**](OffsetDateTime.md) |  |  [optional]
**updated** | [**OffsetDateTime**](OffsetDateTime.md) |  |  [optional]


<a name="AuthenticationTypeEnum"></a>
## Enum: AuthenticationTypeEnum
Name | Value
---- | -----
LOCAL | &quot;LOCAL&quot;
LDAP | &quot;LDAP&quot;
JWT | &quot;JWT&quot;
PAM | &quot;PAM&quot;
KERBEROS | &quot;KERBEROS&quot;



