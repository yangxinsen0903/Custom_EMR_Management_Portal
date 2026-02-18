# GroupsApi

All URIs are relative to *https://localhost/api/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**groupPrivilegeServiceGetPrivilege**](GroupsApi.md#groupPrivilegeServiceGetPrivilege) | **GET** /groups/{groupName}/privileges/{privilegeId} | Get group privilege
[**groupPrivilegeServiceGetPrivileges**](GroupsApi.md#groupPrivilegeServiceGetPrivileges) | **GET** /groups/{groupName}/privileges | Get all privileges
[**groupServiceCreateGroup**](GroupsApi.md#groupServiceCreateGroup) | **POST** /groups | Create new group
[**groupServiceDeleteGroup**](GroupsApi.md#groupServiceDeleteGroup) | **DELETE** /groups/{groupName} | Delete group
[**groupServiceGetGroup**](GroupsApi.md#groupServiceGetGroup) | **GET** /groups/{groupName} | Get group
[**groupServiceGetGroups**](GroupsApi.md#groupServiceGetGroups) | **GET** /groups | Get all groups
[**memberServiceDeleteMember**](GroupsApi.md#memberServiceDeleteMember) | **DELETE** /groups/{groupName}/members/{userName} | Delete group member
[**memberServiceGetMember**](GroupsApi.md#memberServiceGetMember) | **GET** /groups/{groupName}/members/{userName} | Get group member
[**memberServiceGetMembers**](GroupsApi.md#memberServiceGetMembers) | **GET** /groups/{groupName}/members | Get all group members
[**memberServiceUpdateMembers**](GroupsApi.md#memberServiceUpdateMembers) | **PUT** /groups/{groupName}/members | Update group members


<a name="groupPrivilegeServiceGetPrivilege"></a>
# **groupPrivilegeServiceGetPrivilege**
> PrivilegeResponse groupPrivilegeServiceGetPrivilege(groupName, privilegeId, fields)

Get group privilege

Returns group privilege details.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.GroupsApi;


GroupsApi apiInstance = new GroupsApi();
String groupName = "groupName_example"; // String | group name
String privilegeId = "privilegeId_example"; // String | privilege id
String fields = "PrivilegeInfo/_*"; // String | Filter group privilege details
try {
    PrivilegeResponse result = apiInstance.groupPrivilegeServiceGetPrivilege(groupName, privilegeId, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling GroupsApi#groupPrivilegeServiceGetPrivilege");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **groupName** | **String**| group name |
 **privilegeId** | **String**| privilege id |
 **fields** | **String**| Filter group privilege details | [optional] [default to PrivilegeInfo/*]

### Return type

[**PrivilegeResponse**](PrivilegeResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="groupPrivilegeServiceGetPrivileges"></a>
# **groupPrivilegeServiceGetPrivileges**
> List&lt;GroupPrivilegeResponse&gt; groupPrivilegeServiceGetPrivileges(groupName, fields, sortBy, pageSize, from, to)

Get all privileges

Returns all privileges for group.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.GroupsApi;


GroupsApi apiInstance = new GroupsApi();
String groupName = "groupName_example"; // String | group name
String fields = "PrivilegeInfo/_*"; // String | Filter user privileges
String sortBy = "PrivilegeInfo/user_name.asc"; // String | Sort user privileges (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
String from = "0"; // String | The starting page resource (inclusive). Valid values are :offset | \"start\"
String to = "to_example"; // String | The ending page resource (inclusive). Valid values are :offset | \"end\"
try {
    List<GroupPrivilegeResponse> result = apiInstance.groupPrivilegeServiceGetPrivileges(groupName, fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling GroupsApi#groupPrivilegeServiceGetPrivileges");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **groupName** | **String**| group name |
 **fields** | **String**| Filter user privileges | [optional] [default to PrivilegeInfo/*]
 **sortBy** | **String**| Sort user privileges (asc | desc) | [optional] [default to PrivilegeInfo/user_name.asc]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **String**| The starting page resource (inclusive). Valid values are :offset | \&quot;start\&quot; | [optional] [default to 0]
 **to** | **String**| The ending page resource (inclusive). Valid values are :offset | \&quot;end\&quot; | [optional]

### Return type

[**List&lt;GroupPrivilegeResponse&gt;**](GroupPrivilegeResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="groupServiceCreateGroup"></a>
# **groupServiceCreateGroup**
> groupServiceCreateGroup(body)

Create new group

Creates group resource.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.GroupsApi;


GroupsApi apiInstance = new GroupsApi();
GroupRequest body = new GroupRequest(); // GroupRequest | input parameters in json form
try {
    apiInstance.groupServiceCreateGroup(body);
} catch (ApiException e) {
    System.err.println("Exception when calling GroupsApi#groupServiceCreateGroup");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**GroupRequest**](GroupRequest.md)| input parameters in json form |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="groupServiceDeleteGroup"></a>
# **groupServiceDeleteGroup**
> groupServiceDeleteGroup(groupName)

Delete group

Delete group resource.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.GroupsApi;


GroupsApi apiInstance = new GroupsApi();
String groupName = "groupName_example"; // String | group name
try {
    apiInstance.groupServiceDeleteGroup(groupName);
} catch (ApiException e) {
    System.err.println("Exception when calling GroupsApi#groupServiceDeleteGroup");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **groupName** | **String**| group name |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="groupServiceGetGroup"></a>
# **groupServiceGetGroup**
> GroupResponse groupServiceGetGroup(groupName, fields)

Get group

Returns group details.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.GroupsApi;


GroupsApi apiInstance = new GroupsApi();
String groupName = "groupName_example"; // String | group name
String fields = "Groups"; // String | Filter group details
try {
    GroupResponse result = apiInstance.groupServiceGetGroup(groupName, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling GroupsApi#groupServiceGetGroup");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **groupName** | **String**| group name |
 **fields** | **String**| Filter group details | [optional] [default to Groups]

### Return type

[**GroupResponse**](GroupResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="groupServiceGetGroups"></a>
# **groupServiceGetGroups**
> List&lt;GroupResponse&gt; groupServiceGetGroups(fields, sortBy, pageSize, from, to)

Get all groups

Returns details of all groups.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.GroupsApi;


GroupsApi apiInstance = new GroupsApi();
String fields = "Groups/_*"; // String | Filter group details
String sortBy = "Groups/group_name.asc"; // String | Sort groups (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
String from = "0"; // String | The starting page resource (inclusive). Valid values are :offset | \"start\"
String to = "to_example"; // String | The ending page resource (inclusive). Valid values are :offset | \"end\"
try {
    List<GroupResponse> result = apiInstance.groupServiceGetGroups(fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling GroupsApi#groupServiceGetGroups");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **fields** | **String**| Filter group details | [optional] [default to Groups/*]
 **sortBy** | **String**| Sort groups (asc | desc) | [optional] [default to Groups/group_name.asc]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **String**| The starting page resource (inclusive). Valid values are :offset | \&quot;start\&quot; | [optional] [default to 0]
 **to** | **String**| The ending page resource (inclusive). Valid values are :offset | \&quot;end\&quot; | [optional]

### Return type

[**List&lt;GroupResponse&gt;**](GroupResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="memberServiceDeleteMember"></a>
# **memberServiceDeleteMember**
> memberServiceDeleteMember(groupName, userName)

Delete group member

Delete member resource.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.GroupsApi;


GroupsApi apiInstance = new GroupsApi();
String groupName = "groupName_example"; // String | group name
String userName = "userName_example"; // String | user name
try {
    apiInstance.memberServiceDeleteMember(groupName, userName);
} catch (ApiException e) {
    System.err.println("Exception when calling GroupsApi#memberServiceDeleteMember");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **groupName** | **String**| group name |
 **userName** | **String**| user name |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="memberServiceGetMember"></a>
# **memberServiceGetMember**
> MemberResponse memberServiceGetMember(groupName, userName, fields)

Get group member

Returns member details.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.GroupsApi;


GroupsApi apiInstance = new GroupsApi();
String groupName = "groupName_example"; // String | group name
String userName = "userName_example"; // String | user name
String fields = "MemberInfo"; // String | Filter member details
try {
    MemberResponse result = apiInstance.memberServiceGetMember(groupName, userName, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling GroupsApi#memberServiceGetMember");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **groupName** | **String**| group name |
 **userName** | **String**| user name |
 **fields** | **String**| Filter member details | [optional] [default to MemberInfo]

### Return type

[**MemberResponse**](MemberResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="memberServiceGetMembers"></a>
# **memberServiceGetMembers**
> List&lt;MemberResponse&gt; memberServiceGetMembers(groupName, fields, sortBy, pageSize, from, to)

Get all group members

Returns details of all members.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.GroupsApi;


GroupsApi apiInstance = new GroupsApi();
String groupName = "groupName_example"; // String | group name
String fields = "MemberInfo/_*"; // String | Filter member details
String sortBy = "MemberInfo/user_name.asc"; // String | Sort members (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
String from = "0"; // String | The starting page resource (inclusive). Valid values are :offset | \"start\"
String to = "to_example"; // String | The ending page resource (inclusive). Valid values are :offset | \"end\"
try {
    List<MemberResponse> result = apiInstance.memberServiceGetMembers(groupName, fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling GroupsApi#memberServiceGetMembers");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **groupName** | **String**| group name |
 **fields** | **String**| Filter member details | [optional] [default to MemberInfo/*]
 **sortBy** | **String**| Sort members (asc | desc) | [optional] [default to MemberInfo/user_name.asc]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **String**| The starting page resource (inclusive). Valid values are :offset | \&quot;start\&quot; | [optional] [default to 0]
 **to** | **String**| The ending page resource (inclusive). Valid values are :offset | \&quot;end\&quot; | [optional]

### Return type

[**List&lt;MemberResponse&gt;**](MemberResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="memberServiceUpdateMembers"></a>
# **memberServiceUpdateMembers**
> memberServiceUpdateMembers(groupName, body)

Update group members

Updates group member resources.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.GroupsApi;


GroupsApi apiInstance = new GroupsApi();
String groupName = "groupName_example"; // String | group name
MemberRequest body = new MemberRequest(); // MemberRequest | input parameters in json form
try {
    apiInstance.memberServiceUpdateMembers(groupName, body);
} catch (ApiException e) {
    System.err.println("Exception when calling GroupsApi#memberServiceUpdateMembers");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **groupName** | **String**| group name |
 **body** | [**MemberRequest**](MemberRequest.md)| input parameters in json form |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

