# ConfigGroupsApi

All URIs are relative to *https://localhost/api/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createConfigGroup**](ConfigGroupsApi.md#createConfigGroup) | **POST** /clusters/{clusterName}/config_groups | Creates a config group
[**deleteConfigGroup**](ConfigGroupsApi.md#deleteConfigGroup) | **DELETE** /clusters/{clusterName}/config_groups/{groupId} | Deletes a config group
[**getConfigGroup**](ConfigGroupsApi.md#getConfigGroup) | **GET** /clusters/{clusterName}/config_groups/{groupId} | Returns a single config group
[**getConfigGroups**](ConfigGroupsApi.md#getConfigGroups) | **GET** /clusters/{clusterName}/config_groups | Returns all config groups
[**updateConfigGroup**](ConfigGroupsApi.md#updateConfigGroup) | **PUT** /clusters/{clusterName}/config_groups/{groupId} | Updates a config group


<a name="createConfigGroup"></a>
# **createConfigGroup**
> createConfigGroup(clusterName, body)

Creates a config group



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ConfigGroupsApi;


ConfigGroupsApi apiInstance = new ConfigGroupsApi();
String clusterName = "clusterName_example"; // String | 
ConfigGroupRequest body = new ConfigGroupRequest(); // ConfigGroupRequest | 
try {
    apiInstance.createConfigGroup(clusterName, body);
} catch (ApiException e) {
    System.err.println("Exception when calling ConfigGroupsApi#createConfigGroup");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **clusterName** | **String**|  |
 **body** | [**ConfigGroupRequest**](ConfigGroupRequest.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="deleteConfigGroup"></a>
# **deleteConfigGroup**
> deleteConfigGroup(groupId, clusterName)

Deletes a config group



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ConfigGroupsApi;


ConfigGroupsApi apiInstance = new ConfigGroupsApi();
String groupId = "groupId_example"; // String | 
String clusterName = "clusterName_example"; // String | 
try {
    apiInstance.deleteConfigGroup(groupId, clusterName);
} catch (ApiException e) {
    System.err.println("Exception when calling ConfigGroupsApi#deleteConfigGroup");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **groupId** | **String**|  |
 **clusterName** | **String**|  |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getConfigGroup"></a>
# **getConfigGroup**
> ConfigGroupWrapper getConfigGroup(groupId, clusterName, fields)

Returns a single config group



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ConfigGroupsApi;


ConfigGroupsApi apiInstance = new ConfigGroupsApi();
String groupId = "groupId_example"; // String | 
String clusterName = "clusterName_example"; // String | 
String fields = "ConfigGroup/_*"; // String | Filter fields in the response (identifier fields are mandatory)
try {
    ConfigGroupWrapper result = apiInstance.getConfigGroup(groupId, clusterName, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ConfigGroupsApi#getConfigGroup");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **groupId** | **String**|  |
 **clusterName** | **String**|  |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to ConfigGroup/*]

### Return type

[**ConfigGroupWrapper**](ConfigGroupWrapper.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getConfigGroups"></a>
# **getConfigGroups**
> List&lt;ConfigGroupWrapper&gt; getConfigGroups(clusterName, fields, sortBy, pageSize, from, to)

Returns all config groups



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ConfigGroupsApi;


ConfigGroupsApi apiInstance = new ConfigGroupsApi();
String clusterName = "clusterName_example"; // String | 
String fields = "ConfigGroup/_*"; // String | Filter fields in the response (identifier fields are mandatory)
String sortBy = "sortBy_example"; // String | Sort resources in result by (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
Integer from = 0; // Integer | The starting page resource (inclusive).  \"start\" is also accepted.
Integer to = 56; // Integer | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    List<ConfigGroupWrapper> result = apiInstance.getConfigGroups(clusterName, fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ConfigGroupsApi#getConfigGroups");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **clusterName** | **String**|  |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to ConfigGroup/*]
 **sortBy** | **String**| Sort resources in result by (asc | desc) | [optional]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **Integer**| The starting page resource (inclusive).  \&quot;start\&quot; is also accepted. | [optional] [default to 0]
 **to** | **Integer**| The ending page resource (inclusive).  \&quot;end\&quot; is also accepted. | [optional]

### Return type

[**List&lt;ConfigGroupWrapper&gt;**](ConfigGroupWrapper.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="updateConfigGroup"></a>
# **updateConfigGroup**
> updateConfigGroup(groupId, clusterName, body)

Updates a config group



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ConfigGroupsApi;


ConfigGroupsApi apiInstance = new ConfigGroupsApi();
String groupId = "groupId_example"; // String | 
String clusterName = "clusterName_example"; // String | 
ConfigGroupRequest body = new ConfigGroupRequest(); // ConfigGroupRequest | 
try {
    apiInstance.updateConfigGroup(groupId, clusterName, body);
} catch (ApiException e) {
    System.err.println("Exception when calling ConfigGroupsApi#updateConfigGroup");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **groupId** | **String**|  |
 **clusterName** | **String**|  |
 **body** | [**ConfigGroupRequest**](ConfigGroupRequest.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

