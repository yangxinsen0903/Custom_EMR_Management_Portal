# HostsApi

All URIs are relative to *https://localhost/api/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createHost**](HostsApi.md#createHost) | **POST** /hosts/{hostName} | Creates a host
[**createHosts**](HostsApi.md#createHosts) | **POST** /hosts | Creates multiple hosts in a single request
[**deleteHost**](HostsApi.md#deleteHost) | **DELETE** /hosts/{hostName} | Deletes a host
[**deleteHosts**](HostsApi.md#deleteHosts) | **DELETE** /hosts | Deletes multiple hosts in a single request
[**getHost**](HostsApi.md#getHost) | **GET** /hosts/{hostName} | Returns information about a single host
[**getHosts**](HostsApi.md#getHosts) | **GET** /hosts | Returns a collection of all hosts
[**updateHost**](HostsApi.md#updateHost) | **PUT** /hosts/{hostName} | Updates a host
[**updateHosts**](HostsApi.md#updateHosts) | **PUT** /hosts | Updates multiple hosts in a single request


<a name="createHost"></a>
# **createHost**
> createHost(hostName, body)

Creates a host



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.HostsApi;


HostsApi apiInstance = new HostsApi();
String hostName = "hostName_example"; // String | host name
HostRequest body = new HostRequest(); // HostRequest | 
try {
    apiInstance.createHost(hostName, body);
} catch (ApiException e) {
    System.err.println("Exception when calling HostsApi#createHost");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **hostName** | **String**| host name |
 **body** | [**HostRequest**](HostRequest.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="createHosts"></a>
# **createHosts**
> createHosts(body)

Creates multiple hosts in a single request



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.HostsApi;


HostsApi apiInstance = new HostsApi();
HostRequest body = new HostRequest(); // HostRequest | 
try {
    apiInstance.createHosts(body);
} catch (ApiException e) {
    System.err.println("Exception when calling HostsApi#createHosts");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**HostRequest**](HostRequest.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="deleteHost"></a>
# **deleteHost**
> deleteHost(hostName)

Deletes a host



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.HostsApi;


HostsApi apiInstance = new HostsApi();
String hostName = "hostName_example"; // String | host name
try {
    apiInstance.deleteHost(hostName);
} catch (ApiException e) {
    System.err.println("Exception when calling HostsApi#deleteHost");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **hostName** | **String**| host name |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="deleteHosts"></a>
# **deleteHosts**
> deleteHosts(body)

Deletes multiple hosts in a single request



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.HostsApi;


HostsApi apiInstance = new HostsApi();
HostRequest body = new HostRequest(); // HostRequest | 
try {
    apiInstance.deleteHosts(body);
} catch (ApiException e) {
    System.err.println("Exception when calling HostsApi#deleteHosts");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**HostRequest**](HostRequest.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getHost"></a>
# **getHost**
> HostResponseWrapper getHost(hostName, fields)

Returns information about a single host



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.HostsApi;


HostsApi apiInstance = new HostsApi();
String hostName = "hostName_example"; // String | host name
String fields = "fields_example"; // String | Filter fields in the response (identifier fields are mandatory)
try {
    HostResponseWrapper result = apiInstance.getHost(hostName, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling HostsApi#getHost");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **hostName** | **String**| host name |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional]

### Return type

[**HostResponseWrapper**](HostResponseWrapper.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getHosts"></a>
# **getHosts**
> List&lt;HostResponseWrapper&gt; getHosts(fields, sortBy, pageSize, from, to)

Returns a collection of all hosts



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.HostsApi;


HostsApi apiInstance = new HostsApi();
String fields = "Hosts/_*"; // String | Filter fields in the response (identifier fields are mandatory)
String sortBy = "Hosts/host_name.asc"; // String | Sort resources in result by (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
Integer from = 0; // Integer | The starting page resource (inclusive).  \"start\" is also accepted.
Integer to = 56; // Integer | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    List<HostResponseWrapper> result = apiInstance.getHosts(fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling HostsApi#getHosts");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to Hosts/*]
 **sortBy** | **String**| Sort resources in result by (asc | desc) | [optional] [default to Hosts/host_name.asc]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **Integer**| The starting page resource (inclusive).  \&quot;start\&quot; is also accepted. | [optional] [default to 0]
 **to** | **Integer**| The ending page resource (inclusive).  \&quot;end\&quot; is also accepted. | [optional]

### Return type

[**List&lt;HostResponseWrapper&gt;**](HostResponseWrapper.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="updateHost"></a>
# **updateHost**
> updateHost(hostName, body)

Updates a host



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.HostsApi;


HostsApi apiInstance = new HostsApi();
String hostName = "hostName_example"; // String | host name
HostRequest body = new HostRequest(); // HostRequest | 
try {
    apiInstance.updateHost(hostName, body);
} catch (ApiException e) {
    System.err.println("Exception when calling HostsApi#updateHost");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **hostName** | **String**| host name |
 **body** | [**HostRequest**](HostRequest.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="updateHosts"></a>
# **updateHosts**
> updateHosts(body)

Updates multiple hosts in a single request



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.HostsApi;


HostsApi apiInstance = new HostsApi();
HostRequest body = new HostRequest(); // HostRequest | 
try {
    apiInstance.updateHosts(body);
} catch (ApiException e) {
    System.err.println("Exception when calling HostsApi#updateHosts");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**HostRequest**](HostRequest.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

