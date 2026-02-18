# UsersApi

All URIs are relative to *https://localhost/api/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**activeWidgetLayoutServiceGetServices**](UsersApi.md#activeWidgetLayoutServiceGetServices) | **GET** /users/{userName}/activeWidgetLayouts | Get user widget layouts
[**activeWidgetLayoutServiceUpdateServices**](UsersApi.md#activeWidgetLayoutServiceUpdateServices) | **PUT** /users/{userName}/activeWidgetLayouts | Update user widget layouts
[**createUser**](UsersApi.md#createUser) | **POST** /users/{userName} | Create new user
[**createUsers**](UsersApi.md#createUsers) | **POST** /users | Creates one or more users in a single request
[**deleteUser**](UsersApi.md#deleteUser) | **DELETE** /users/{userName} | Delete single user
[**getUser**](UsersApi.md#getUser) | **GET** /users/{userName} | Get single user
[**getUsers**](UsersApi.md#getUsers) | **GET** /users | Get all users
[**updateUser**](UsersApi.md#updateUser) | **PUT** /users/{userName} | Update user details
[**userAuthorizationServiceGetAuthorization**](UsersApi.md#userAuthorizationServiceGetAuthorization) | **GET** /users/{userName}/authorizations/{authorization_id} | Get user authorization
[**userAuthorizationServiceGetAuthorizations**](UsersApi.md#userAuthorizationServiceGetAuthorizations) | **GET** /users/{userName}/authorizations | Get all authorizations
[**userPrivilegeServiceGetPrivilege**](UsersApi.md#userPrivilegeServiceGetPrivilege) | **GET** /users/{userName}/privileges/{privilegeId} | Get user privilege
[**userPrivilegeServiceGetPrivileges**](UsersApi.md#userPrivilegeServiceGetPrivileges) | **GET** /users/{userName}/privileges | Get all privileges


<a name="activeWidgetLayoutServiceGetServices"></a>
# **activeWidgetLayoutServiceGetServices**
> List&lt;ActiveWidgetLayoutResponse&gt; activeWidgetLayoutServiceGetServices(userName, fields, sortBy, pageSize, from, to)

Get user widget layouts

Returns all active widget layouts for user.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.UsersApi;


UsersApi apiInstance = new UsersApi();
String userName = "userName_example"; // String | user name
String fields = "WidgetLayoutInfo/_*"; // String | Filter user layout details
String sortBy = "WidgetLayoutInfo/user_name.asc"; // String | Sort layouts (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
String from = "0"; // String | The starting page resource (inclusive). Valid values are :offset | \"start\"
String to = "to_example"; // String | The ending page resource (inclusive). Valid values are :offset | \"end\"
try {
    List<ActiveWidgetLayoutResponse> result = apiInstance.activeWidgetLayoutServiceGetServices(userName, fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling UsersApi#activeWidgetLayoutServiceGetServices");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **userName** | **String**| user name |
 **fields** | **String**| Filter user layout details | [optional] [default to WidgetLayoutInfo/*]
 **sortBy** | **String**| Sort layouts (asc | desc) | [optional] [default to WidgetLayoutInfo/user_name.asc]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **String**| The starting page resource (inclusive). Valid values are :offset | \&quot;start\&quot; | [optional] [default to 0]
 **to** | **String**| The ending page resource (inclusive). Valid values are :offset | \&quot;end\&quot; | [optional]

### Return type

[**List&lt;ActiveWidgetLayoutResponse&gt;**](ActiveWidgetLayoutResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="activeWidgetLayoutServiceUpdateServices"></a>
# **activeWidgetLayoutServiceUpdateServices**
> activeWidgetLayoutServiceUpdateServices(userName, body)

Update user widget layouts

Updates user widget layout.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.UsersApi;


UsersApi apiInstance = new UsersApi();
String userName = "userName_example"; // String | user name
ActiveWidgetLayoutRequest body = new ActiveWidgetLayoutRequest(); // ActiveWidgetLayoutRequest | input parameters in json form
try {
    apiInstance.activeWidgetLayoutServiceUpdateServices(userName, body);
} catch (ApiException e) {
    System.err.println("Exception when calling UsersApi#activeWidgetLayoutServiceUpdateServices");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **userName** | **String**| user name |
 **body** | [**ActiveWidgetLayoutRequest**](ActiveWidgetLayoutRequest.md)| input parameters in json form |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="createUser"></a>
# **createUser**
> createUser(userName, body)

Create new user



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.UsersApi;


UsersApi apiInstance = new UsersApi();
String userName = "userName_example"; // String | user name
UserRequestCreateUserSwagger body = new UserRequestCreateUserSwagger(); // UserRequestCreateUserSwagger | 
try {
    apiInstance.createUser(userName, body);
} catch (ApiException e) {
    System.err.println("Exception when calling UsersApi#createUser");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **userName** | **String**| user name |
 **body** | [**UserRequestCreateUserSwagger**](UserRequestCreateUserSwagger.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="createUsers"></a>
# **createUsers**
> createUsers(body)

Creates one or more users in a single request



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.UsersApi;


UsersApi apiInstance = new UsersApi();
UserRequestCreateUsersSwagger body = new UserRequestCreateUsersSwagger(); // UserRequestCreateUsersSwagger | 
try {
    apiInstance.createUsers(body);
} catch (ApiException e) {
    System.err.println("Exception when calling UsersApi#createUsers");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**UserRequestCreateUsersSwagger**](UserRequestCreateUsersSwagger.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="deleteUser"></a>
# **deleteUser**
> deleteUser(userName)

Delete single user



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.UsersApi;


UsersApi apiInstance = new UsersApi();
String userName = "userName_example"; // String | user name
try {
    apiInstance.deleteUser(userName);
} catch (ApiException e) {
    System.err.println("Exception when calling UsersApi#deleteUser");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **userName** | **String**| user name |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getUser"></a>
# **getUser**
> UserResponseSwagger getUser(userName, fields)

Get single user



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.UsersApi;


UsersApi apiInstance = new UsersApi();
String userName = "userName_example"; // String | user name
String fields = "Users/_*"; // String | Filter fields in the response (identifier fields are mandatory)
try {
    UserResponseSwagger result = apiInstance.getUser(userName, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling UsersApi#getUser");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **userName** | **String**| user name |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to Users/*]

### Return type

[**UserResponseSwagger**](UserResponseSwagger.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getUsers"></a>
# **getUsers**
> List&lt;UserResponseSwagger&gt; getUsers(fields, sortBy, pageSize, from, to)

Get all users



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.UsersApi;


UsersApi apiInstance = new UsersApi();
String fields = "Users/user_name"; // String | Filter fields in the response (identifier fields are mandatory)
String sortBy = "Users/user_name.asc"; // String | Sort resources in result by (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
Integer from = 0; // Integer | The starting page resource (inclusive).  \"start\" is also accepted.
Integer to = 56; // Integer | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    List<UserResponseSwagger> result = apiInstance.getUsers(fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling UsersApi#getUsers");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to Users/user_name]
 **sortBy** | **String**| Sort resources in result by (asc | desc) | [optional] [default to Users/user_name.asc]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **Integer**| The starting page resource (inclusive).  \&quot;start\&quot; is also accepted. | [optional] [default to 0]
 **to** | **Integer**| The ending page resource (inclusive).  \&quot;end\&quot; is also accepted. | [optional]

### Return type

[**List&lt;UserResponseSwagger&gt;**](UserResponseSwagger.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="updateUser"></a>
# **updateUser**
> updateUser(userName, body)

Update user details



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.UsersApi;


UsersApi apiInstance = new UsersApi();
String userName = "userName_example"; // String | user name
UserRequestUpdateUserSwagger body = new UserRequestUpdateUserSwagger(); // UserRequestUpdateUserSwagger | 
try {
    apiInstance.updateUser(userName, body);
} catch (ApiException e) {
    System.err.println("Exception when calling UsersApi#updateUser");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **userName** | **String**| user name |
 **body** | [**UserRequestUpdateUserSwagger**](UserRequestUpdateUserSwagger.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="userAuthorizationServiceGetAuthorization"></a>
# **userAuthorizationServiceGetAuthorization**
> UserAuthorizationResponse userAuthorizationServiceGetAuthorization(userName, authorizationId, fields)

Get user authorization

Returns user authorization details.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.UsersApi;


UsersApi apiInstance = new UsersApi();
String userName = "userName_example"; // String | user name
String authorizationId = "authorizationId_example"; // String | Authorization Id
String fields = "AuthorizationInfo/_*"; // String | Filter user authorization details
try {
    UserAuthorizationResponse result = apiInstance.userAuthorizationServiceGetAuthorization(userName, authorizationId, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling UsersApi#userAuthorizationServiceGetAuthorization");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **userName** | **String**| user name |
 **authorizationId** | **String**| Authorization Id |
 **fields** | **String**| Filter user authorization details | [optional] [default to AuthorizationInfo/*]

### Return type

[**UserAuthorizationResponse**](UserAuthorizationResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="userAuthorizationServiceGetAuthorizations"></a>
# **userAuthorizationServiceGetAuthorizations**
> List&lt;UserAuthorizationResponse&gt; userAuthorizationServiceGetAuthorizations(userName, fields, sortBy, pageSize, from, to)

Get all authorizations

Returns all authorization for user.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.UsersApi;


UsersApi apiInstance = new UsersApi();
String userName = "userName_example"; // String | user name
String fields = "AuthorizationInfo/_*"; // String | Filter user authorization details
String sortBy = "AuthorizationInfo/user_name.asc"; // String | Sort user authorizations (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
String from = "0"; // String | The starting page resource (inclusive). Valid values are :offset | \"start\"
String to = "to_example"; // String | The ending page resource (inclusive). Valid values are :offset | \"end\"
try {
    List<UserAuthorizationResponse> result = apiInstance.userAuthorizationServiceGetAuthorizations(userName, fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling UsersApi#userAuthorizationServiceGetAuthorizations");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **userName** | **String**| user name |
 **fields** | **String**| Filter user authorization details | [optional] [default to AuthorizationInfo/*]
 **sortBy** | **String**| Sort user authorizations (asc | desc) | [optional] [default to AuthorizationInfo/user_name.asc]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **String**| The starting page resource (inclusive). Valid values are :offset | \&quot;start\&quot; | [optional] [default to 0]
 **to** | **String**| The ending page resource (inclusive). Valid values are :offset | \&quot;end\&quot; | [optional]

### Return type

[**List&lt;UserAuthorizationResponse&gt;**](UserAuthorizationResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="userPrivilegeServiceGetPrivilege"></a>
# **userPrivilegeServiceGetPrivilege**
> UserPrivilegeResponse userPrivilegeServiceGetPrivilege(userName, privilegeId, fields)

Get user privilege

Returns user privilege details.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.UsersApi;


UsersApi apiInstance = new UsersApi();
String userName = "userName_example"; // String | user name
String privilegeId = "privilegeId_example"; // String | privilege id
String fields = "PrivilegeInfo/_*"; // String | Filter user privilege details
try {
    UserPrivilegeResponse result = apiInstance.userPrivilegeServiceGetPrivilege(userName, privilegeId, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling UsersApi#userPrivilegeServiceGetPrivilege");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **userName** | **String**| user name |
 **privilegeId** | **String**| privilege id |
 **fields** | **String**| Filter user privilege details | [optional] [default to PrivilegeInfo/*]

### Return type

[**UserPrivilegeResponse**](UserPrivilegeResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="userPrivilegeServiceGetPrivileges"></a>
# **userPrivilegeServiceGetPrivileges**
> List&lt;UserPrivilegeResponse&gt; userPrivilegeServiceGetPrivileges(userName, fields, sortBy, pageSize, from, to)

Get all privileges

Returns all privileges for user.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.UsersApi;


UsersApi apiInstance = new UsersApi();
String userName = "userName_example"; // String | user name
String fields = "PrivilegeInfo/_*"; // String | Filter user privileges
String sortBy = "PrivilegeInfo/user_name.asc"; // String | Sort user privileges (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
String from = "0"; // String | The starting page resource (inclusive). Valid values are :offset | \"start\"
String to = "to_example"; // String | The ending page resource (inclusive). Valid values are :offset | \"end\"
try {
    List<UserPrivilegeResponse> result = apiInstance.userPrivilegeServiceGetPrivileges(userName, fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling UsersApi#userPrivilegeServiceGetPrivileges");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **userName** | **String**| user name |
 **fields** | **String**| Filter user privileges | [optional] [default to PrivilegeInfo/*]
 **sortBy** | **String**| Sort user privileges (asc | desc) | [optional] [default to PrivilegeInfo/user_name.asc]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **String**| The starting page resource (inclusive). Valid values are :offset | \&quot;start\&quot; | [optional] [default to 0]
 **to** | **String**| The ending page resource (inclusive). Valid values are :offset | \&quot;end\&quot; | [optional]

### Return type

[**List&lt;UserPrivilegeResponse&gt;**](UserPrivilegeResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

