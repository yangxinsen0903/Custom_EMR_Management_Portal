# UserAuthenticationSourcesApi

All URIs are relative to *https://localhost/api/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createAuthenticationSources**](UserAuthenticationSourcesApi.md#createAuthenticationSources) | **POST** /users/{userName}/sources | Create one or more new authentication sources for a user
[**deleteAuthenticationSource**](UserAuthenticationSourcesApi.md#deleteAuthenticationSource) | **DELETE** /users/{userName}/sources/{sourceId} | Deletes an existing authentication source
[**getAuthenticationSource**](UserAuthenticationSourcesApi.md#getAuthenticationSource) | **GET** /users/{userName}/sources/{sourceId} | Get user authentication source
[**getAuthenticationSources**](UserAuthenticationSourcesApi.md#getAuthenticationSources) | **GET** /users/{userName}/sources | Get all authentication sources
[**updateAuthenticationSource**](UserAuthenticationSourcesApi.md#updateAuthenticationSource) | **PUT** /users/{userName}/sources/{sourceId} | Updates an existing authentication source


<a name="createAuthenticationSources"></a>
# **createAuthenticationSources**
> createAuthenticationSources(userName, body)

Create one or more new authentication sources for a user



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.UserAuthenticationSourcesApi;


UserAuthenticationSourcesApi apiInstance = new UserAuthenticationSourcesApi();
String userName = "userName_example"; // String | user name
UserAuthenticationSourceRequestCreateSwagger body = new UserAuthenticationSourceRequestCreateSwagger(); // UserAuthenticationSourceRequestCreateSwagger | 
try {
    apiInstance.createAuthenticationSources(userName, body);
} catch (ApiException e) {
    System.err.println("Exception when calling UserAuthenticationSourcesApi#createAuthenticationSources");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **userName** | **String**| user name |
 **body** | [**UserAuthenticationSourceRequestCreateSwagger**](UserAuthenticationSourceRequestCreateSwagger.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="deleteAuthenticationSource"></a>
# **deleteAuthenticationSource**
> deleteAuthenticationSource(userName, sourceId)

Deletes an existing authentication source



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.UserAuthenticationSourcesApi;


UserAuthenticationSourcesApi apiInstance = new UserAuthenticationSourcesApi();
String userName = "userName_example"; // String | user name
String sourceId = "sourceId_example"; // String | source id
try {
    apiInstance.deleteAuthenticationSource(userName, sourceId);
} catch (ApiException e) {
    System.err.println("Exception when calling UserAuthenticationSourcesApi#deleteAuthenticationSource");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **userName** | **String**| user name |
 **sourceId** | **String**| source id |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getAuthenticationSource"></a>
# **getAuthenticationSource**
> UserAuthenticationSourceResponseSwagger getAuthenticationSource(userName, sourceId, fields)

Get user authentication source



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.UserAuthenticationSourcesApi;


UserAuthenticationSourcesApi apiInstance = new UserAuthenticationSourcesApi();
String userName = "userName_example"; // String | user name
String sourceId = "sourceId_example"; // String | source id
String fields = "AuthenticationSourceInfo/_*"; // String | Filter fields in the response (identifier fields are mandatory)
try {
    UserAuthenticationSourceResponseSwagger result = apiInstance.getAuthenticationSource(userName, sourceId, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling UserAuthenticationSourcesApi#getAuthenticationSource");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **userName** | **String**| user name |
 **sourceId** | **String**| source id |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to AuthenticationSourceInfo/*]

### Return type

[**UserAuthenticationSourceResponseSwagger**](UserAuthenticationSourceResponseSwagger.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getAuthenticationSources"></a>
# **getAuthenticationSources**
> List&lt;UserAuthenticationSourceResponseSwagger&gt; getAuthenticationSources(userName, fields, sortBy, pageSize, from, to)

Get all authentication sources



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.UserAuthenticationSourcesApi;


UserAuthenticationSourcesApi apiInstance = new UserAuthenticationSourcesApi();
String userName = "userName_example"; // String | user name
String fields = "AuthenticationSourceInfo/source_id,AuthenticationSourceInfo/user_name"; // String | Filter fields in the response (identifier fields are mandatory)
String sortBy = "AuthenticationSourceInfo/source_id.asc"; // String | Sort resources in result by (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
Integer from = 0; // Integer | The starting page resource (inclusive).  \"start\" is also accepted.
Integer to = 56; // Integer | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    List<UserAuthenticationSourceResponseSwagger> result = apiInstance.getAuthenticationSources(userName, fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling UserAuthenticationSourcesApi#getAuthenticationSources");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **userName** | **String**| user name |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to AuthenticationSourceInfo/source_id,AuthenticationSourceInfo/user_name]
 **sortBy** | **String**| Sort resources in result by (asc | desc) | [optional] [default to AuthenticationSourceInfo/source_id.asc]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **Integer**| The starting page resource (inclusive).  \&quot;start\&quot; is also accepted. | [optional] [default to 0]
 **to** | **Integer**| The ending page resource (inclusive).  \&quot;end\&quot; is also accepted. | [optional]

### Return type

[**List&lt;UserAuthenticationSourceResponseSwagger&gt;**](UserAuthenticationSourceResponseSwagger.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="updateAuthenticationSource"></a>
# **updateAuthenticationSource**
> updateAuthenticationSource(userName, sourceId, body)

Updates an existing authentication source



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.UserAuthenticationSourcesApi;


UserAuthenticationSourcesApi apiInstance = new UserAuthenticationSourcesApi();
String userName = "userName_example"; // String | user name
String sourceId = "sourceId_example"; // String | source id
UserAuthenticationSourceRequestUpdateSwagger body = new UserAuthenticationSourceRequestUpdateSwagger(); // UserAuthenticationSourceRequestUpdateSwagger | 
try {
    apiInstance.updateAuthenticationSource(userName, sourceId, body);
} catch (ApiException e) {
    System.err.println("Exception when calling UserAuthenticationSourcesApi#updateAuthenticationSource");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **userName** | **String**| user name |
 **sourceId** | **String**| source id |
 **body** | [**UserAuthenticationSourceRequestUpdateSwagger**](UserAuthenticationSourceRequestUpdateSwagger.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

