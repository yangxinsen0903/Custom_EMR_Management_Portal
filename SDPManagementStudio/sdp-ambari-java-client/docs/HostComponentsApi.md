# HostComponentsApi

All URIs are relative to *https://localhost/api/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createHostComponent**](HostComponentsApi.md#createHostComponent) | **POST** /clusters/{clusterName}/host_components/{hostComponentName} | Create new host component
[**deleteHostComponent**](HostComponentsApi.md#deleteHostComponent) | **DELETE** /clusters/{clusterName}/host_components/{hostComponentName} | Delete host component
[**deleteHostComponents**](HostComponentsApi.md#deleteHostComponents) | **DELETE** /clusters/{clusterName}/host_components | Delete host components
[**getHostComponent**](HostComponentsApi.md#getHostComponent) | **GET** /clusters/{clusterName}/host_components/{hostComponentName} | Get single host component for a host
[**getHostComponents**](HostComponentsApi.md#getHostComponents) | **GET** /clusters/{clusterName}/host_components | Get all host components for a host
[**getProcesses**](HostComponentsApi.md#getProcesses) | **GET** /clusters/{clusterName}/host_components/{hostComponentName}/processes | Get processes of a specific host component
[**updateHostComponent**](HostComponentsApi.md#updateHostComponent) | **PUT** /clusters/{clusterName}/host_components/{hostComponentName} | Update host component detail


<a name="createHostComponent"></a>
# **createHostComponent**
> createHostComponent(hostComponentName, clusterName, body)

Create new host component



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.HostComponentsApi;


HostComponentsApi apiInstance = new HostComponentsApi();
String hostComponentName = "hostComponentName_example"; // String | 
String clusterName = "clusterName_example"; // String | 
ServiceComponentHostResponse body = new ServiceComponentHostResponse(); // ServiceComponentHostResponse | 
try {
    apiInstance.createHostComponent(hostComponentName, clusterName, body);
} catch (ApiException e) {
    System.err.println("Exception when calling HostComponentsApi#createHostComponent");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **hostComponentName** | **String**|  |
 **clusterName** | **String**|  |
 **body** | [**ServiceComponentHostResponse**](ServiceComponentHostResponse.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="deleteHostComponent"></a>
# **deleteHostComponent**
> deleteHostComponent(hostComponentName, clusterName)

Delete host component



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.HostComponentsApi;


HostComponentsApi apiInstance = new HostComponentsApi();
String hostComponentName = "hostComponentName_example"; // String | 
String clusterName = "clusterName_example"; // String | 
try {
    apiInstance.deleteHostComponent(hostComponentName, clusterName);
} catch (ApiException e) {
    System.err.println("Exception when calling HostComponentsApi#deleteHostComponent");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **hostComponentName** | **String**|  |
 **clusterName** | **String**|  |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="deleteHostComponents"></a>
# **deleteHostComponents**
> deleteHostComponents(clusterName)

Delete host components



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.HostComponentsApi;


HostComponentsApi apiInstance = new HostComponentsApi();
String clusterName = "clusterName_example"; // String | 
try {
    apiInstance.deleteHostComponents(clusterName);
} catch (ApiException e) {
    System.err.println("Exception when calling HostComponentsApi#deleteHostComponents");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **clusterName** | **String**|  |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getHostComponent"></a>
# **getHostComponent**
> HostComponentSwagger getHostComponent(hostComponentName, clusterName, format, fields)

Get single host component for a host



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.HostComponentsApi;


HostComponentsApi apiInstance = new HostComponentsApi();
String hostComponentName = "hostComponentName_example"; // String | 
String clusterName = "clusterName_example"; // String | 
String format = "format_example"; // String | 
String fields = "fields_example"; // String | Filter fields in the response (identifier fields are mandatory)
try {
    HostComponentSwagger result = apiInstance.getHostComponent(hostComponentName, clusterName, format, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling HostComponentsApi#getHostComponent");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **hostComponentName** | **String**|  |
 **clusterName** | **String**|  |
 **format** | **String**|  | [optional]
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional]

### Return type

[**HostComponentSwagger**](HostComponentSwagger.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getHostComponents"></a>
# **getHostComponents**
> List&lt;HostComponentSwagger&gt; getHostComponents(clusterName, format, fields)

Get all host components for a host



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.HostComponentsApi;


HostComponentsApi apiInstance = new HostComponentsApi();
String clusterName = "clusterName_example"; // String | 
String format = "format_example"; // String | 
String fields = "fields_example"; // String | Filter fields in the response (identifier fields are mandatory)
try {
    List<HostComponentSwagger> result = apiInstance.getHostComponents(clusterName, format, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling HostComponentsApi#getHostComponents");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **clusterName** | **String**|  |
 **format** | **String**|  | [optional]
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional]

### Return type

[**List&lt;HostComponentSwagger&gt;**](HostComponentSwagger.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getProcesses"></a>
# **getProcesses**
> HostComponentProcessResponse getProcesses(hostComponentName, clusterName, fields)

Get processes of a specific host component



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.HostComponentsApi;


HostComponentsApi apiInstance = new HostComponentsApi();
String hostComponentName = "hostComponentName_example"; // String | 
String clusterName = "clusterName_example"; // String | 
String fields = "fields_example"; // String | Filter fields in the response (identifier fields are mandatory)
try {
    HostComponentProcessResponse result = apiInstance.getProcesses(hostComponentName, clusterName, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling HostComponentsApi#getProcesses");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **hostComponentName** | **String**|  |
 **clusterName** | **String**|  |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional]

### Return type

[**HostComponentProcessResponse**](HostComponentProcessResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="updateHostComponent"></a>
# **updateHostComponent**
> updateHostComponent(hostComponentName, clusterName, body)

Update host component detail



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.HostComponentsApi;


HostComponentsApi apiInstance = new HostComponentsApi();
String hostComponentName = "hostComponentName_example"; // String | 
String clusterName = "clusterName_example"; // String | 
ServiceComponentHostResponse body = new ServiceComponentHostResponse(); // ServiceComponentHostResponse | 
try {
    apiInstance.updateHostComponent(hostComponentName, clusterName, body);
} catch (ApiException e) {
    System.err.println("Exception when calling HostComponentsApi#updateHostComponent");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **hostComponentName** | **String**|  |
 **clusterName** | **String**|  |
 **body** | [**ServiceComponentHostResponse**](ServiceComponentHostResponse.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

