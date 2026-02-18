# ClusterServicesApi

All URIs are relative to *https://localhost/api/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**serviceServiceCreateArtifact**](ClusterServicesApi.md#serviceServiceCreateArtifact) | **POST** /clusters/{clusterName}/services/{serviceName}/artifacts/{artifactName} | Creates a service artifact
[**serviceServiceCreateServices**](ClusterServicesApi.md#serviceServiceCreateServices) | **POST** /clusters/{clusterName}/services/{serviceName} | Creates a service
[**serviceServiceDeleteArtifact**](ClusterServicesApi.md#serviceServiceDeleteArtifact) | **DELETE** /clusters/{clusterName}/services/{serviceName}/artifacts/{artifactName} | Deletes a single service artifact
[**serviceServiceDeleteArtifacts**](ClusterServicesApi.md#serviceServiceDeleteArtifacts) | **DELETE** /clusters/{clusterName}/services/{serviceName}/artifacts | Deletes all artifacts of a service that match the provided predicate
[**serviceServiceDeleteService**](ClusterServicesApi.md#serviceServiceDeleteService) | **DELETE** /clusters/{clusterName}/services/{serviceName} | Deletes a service
[**serviceServiceGetArtifact**](ClusterServicesApi.md#serviceServiceGetArtifact) | **GET** /clusters/{clusterName}/services/{serviceName}/artifacts/{artifactName} | Get the details of a service artifact
[**serviceServiceGetArtifacts**](ClusterServicesApi.md#serviceServiceGetArtifacts) | **GET** /clusters/{clusterName}/services/{serviceName}/artifacts | Get all service artifacts
[**serviceServiceGetService**](ClusterServicesApi.md#serviceServiceGetService) | **GET** /clusters/{clusterName}/services/{serviceName} | Get the details of a service
[**serviceServiceGetServices**](ClusterServicesApi.md#serviceServiceGetServices) | **GET** /clusters/{clusterName}/services | Get all services
[**serviceServiceUpdateArtifact**](ClusterServicesApi.md#serviceServiceUpdateArtifact) | **PUT** /clusters/{clusterName}/services/{serviceName}/artifacts/{artifactName} | Updates a single artifact
[**serviceServiceUpdateArtifacts**](ClusterServicesApi.md#serviceServiceUpdateArtifacts) | **PUT** /clusters/{clusterName}/services/{serviceName}/artifacts | Updates multiple artifacts
[**serviceServiceUpdateService**](ClusterServicesApi.md#serviceServiceUpdateService) | **PUT** /clusters/{clusterName}/services/{serviceName} | Updates a service


<a name="serviceServiceCreateArtifact"></a>
# **serviceServiceCreateArtifact**
> serviceServiceCreateArtifact(serviceName, artifactName, clusterName, body)

Creates a service artifact



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ClusterServicesApi;


ClusterServicesApi apiInstance = new ClusterServicesApi();
String serviceName = "serviceName_example"; // String | 
String artifactName = "artifactName_example"; // String | 
String clusterName = "clusterName_example"; // String | 
ClusterServiceArtifactRequest body = new ClusterServiceArtifactRequest(); // ClusterServiceArtifactRequest | 
try {
    apiInstance.serviceServiceCreateArtifact(serviceName, artifactName, clusterName, body);
} catch (ApiException e) {
    System.err.println("Exception when calling ClusterServicesApi#serviceServiceCreateArtifact");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **serviceName** | **String**|  |
 **artifactName** | **String**|  |
 **clusterName** | **String**|  |
 **body** | [**ClusterServiceArtifactRequest**](ClusterServiceArtifactRequest.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="serviceServiceCreateServices"></a>
# **serviceServiceCreateServices**
> serviceServiceCreateServices(serviceName, clusterName, body)

Creates a service



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ClusterServicesApi;


ClusterServicesApi apiInstance = new ClusterServicesApi();
String serviceName = "serviceName_example"; // String | 
String clusterName = "clusterName_example"; // String | 
ServiceRequestSwagger body = new ServiceRequestSwagger(); // ServiceRequestSwagger | 
try {
    apiInstance.serviceServiceCreateServices(serviceName, clusterName, body);
} catch (ApiException e) {
    System.err.println("Exception when calling ClusterServicesApi#serviceServiceCreateServices");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **serviceName** | **String**|  |
 **clusterName** | **String**|  |
 **body** | [**ServiceRequestSwagger**](ServiceRequestSwagger.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="serviceServiceDeleteArtifact"></a>
# **serviceServiceDeleteArtifact**
> serviceServiceDeleteArtifact(serviceName, artifactName, clusterName)

Deletes a single service artifact



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ClusterServicesApi;


ClusterServicesApi apiInstance = new ClusterServicesApi();
String serviceName = "serviceName_example"; // String | 
String artifactName = "artifactName_example"; // String | 
String clusterName = "clusterName_example"; // String | 
try {
    apiInstance.serviceServiceDeleteArtifact(serviceName, artifactName, clusterName);
} catch (ApiException e) {
    System.err.println("Exception when calling ClusterServicesApi#serviceServiceDeleteArtifact");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **serviceName** | **String**|  |
 **artifactName** | **String**|  |
 **clusterName** | **String**|  |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="serviceServiceDeleteArtifacts"></a>
# **serviceServiceDeleteArtifacts**
> serviceServiceDeleteArtifacts(serviceName, clusterName)

Deletes all artifacts of a service that match the provided predicate



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ClusterServicesApi;


ClusterServicesApi apiInstance = new ClusterServicesApi();
String serviceName = "serviceName_example"; // String | 
String clusterName = "clusterName_example"; // String | 
try {
    apiInstance.serviceServiceDeleteArtifacts(serviceName, clusterName);
} catch (ApiException e) {
    System.err.println("Exception when calling ClusterServicesApi#serviceServiceDeleteArtifacts");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **serviceName** | **String**|  |
 **clusterName** | **String**|  |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="serviceServiceDeleteService"></a>
# **serviceServiceDeleteService**
> serviceServiceDeleteService(serviceName, clusterName)

Deletes a service



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ClusterServicesApi;


ClusterServicesApi apiInstance = new ClusterServicesApi();
String serviceName = "serviceName_example"; // String | 
String clusterName = "clusterName_example"; // String | 
try {
    apiInstance.serviceServiceDeleteService(serviceName, clusterName);
} catch (ApiException e) {
    System.err.println("Exception when calling ClusterServicesApi#serviceServiceDeleteService");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **serviceName** | **String**|  |
 **clusterName** | **String**|  |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="serviceServiceGetArtifact"></a>
# **serviceServiceGetArtifact**
> List&lt;ClusterServiceArtifactResponse&gt; serviceServiceGetArtifact(serviceName, artifactName, clusterName, fields, sortBy, pageSize, from, to)

Get the details of a service artifact



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ClusterServicesApi;


ClusterServicesApi apiInstance = new ClusterServicesApi();
String serviceName = "serviceName_example"; // String | 
String artifactName = "artifactName_example"; // String | 
String clusterName = "clusterName_example"; // String | 
String fields = "Artifacts/artifact_name"; // String | Filter fields in the response (identifier fields are mandatory)
String sortBy = "Artifacts/artifact_name"; // String | Sort resources in result by (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
String from = "0"; // String | The starting page resource (inclusive).  \"start\" is also accepted.
String to = "to_example"; // String | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    List<ClusterServiceArtifactResponse> result = apiInstance.serviceServiceGetArtifact(serviceName, artifactName, clusterName, fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ClusterServicesApi#serviceServiceGetArtifact");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **serviceName** | **String**|  |
 **artifactName** | **String**|  |
 **clusterName** | **String**|  |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to Artifacts/artifact_name]
 **sortBy** | **String**| Sort resources in result by (asc | desc) | [optional] [default to Artifacts/artifact_name]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **String**| The starting page resource (inclusive).  \&quot;start\&quot; is also accepted. | [optional] [default to 0]
 **to** | **String**| The ending page resource (inclusive).  \&quot;end\&quot; is also accepted. | [optional]

### Return type

[**List&lt;ClusterServiceArtifactResponse&gt;**](ClusterServiceArtifactResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="serviceServiceGetArtifacts"></a>
# **serviceServiceGetArtifacts**
> List&lt;ClusterServiceArtifactResponse&gt; serviceServiceGetArtifacts(serviceName, clusterName, fields, sortBy, pageSize, from, to)

Get all service artifacts



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ClusterServicesApi;


ClusterServicesApi apiInstance = new ClusterServicesApi();
String serviceName = "serviceName_example"; // String | 
String clusterName = "clusterName_example"; // String | 
String fields = "Artifacts/artifact_name"; // String | Filter fields in the response (identifier fields are mandatory)
String sortBy = "Artifacts/artifact_name"; // String | Sort resources in result by (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
String from = "0"; // String | The starting page resource (inclusive).  \"start\" is also accepted.
String to = "to_example"; // String | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    List<ClusterServiceArtifactResponse> result = apiInstance.serviceServiceGetArtifacts(serviceName, clusterName, fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ClusterServicesApi#serviceServiceGetArtifacts");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **serviceName** | **String**|  |
 **clusterName** | **String**|  |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to Artifacts/artifact_name]
 **sortBy** | **String**| Sort resources in result by (asc | desc) | [optional] [default to Artifacts/artifact_name]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **String**| The starting page resource (inclusive).  \&quot;start\&quot; is also accepted. | [optional] [default to 0]
 **to** | **String**| The ending page resource (inclusive).  \&quot;end\&quot; is also accepted. | [optional]

### Return type

[**List&lt;ClusterServiceArtifactResponse&gt;**](ClusterServiceArtifactResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="serviceServiceGetService"></a>
# **serviceServiceGetService**
> List&lt;ServiceResponseSwagger&gt; serviceServiceGetService(serviceName, clusterName, fields)

Get the details of a service

Returns the details of a service.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ClusterServicesApi;


ClusterServicesApi apiInstance = new ClusterServicesApi();
String serviceName = "serviceName_example"; // String | 
String clusterName = "clusterName_example"; // String | 
String fields = "ServiceInfo/_*"; // String | Filter fields in the response (identifier fields are mandatory)
try {
    List<ServiceResponseSwagger> result = apiInstance.serviceServiceGetService(serviceName, clusterName, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ClusterServicesApi#serviceServiceGetService");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **serviceName** | **String**|  |
 **clusterName** | **String**|  |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to ServiceInfo/*]

### Return type

[**List&lt;ServiceResponseSwagger&gt;**](ServiceResponseSwagger.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="serviceServiceGetServices"></a>
# **serviceServiceGetServices**
> List&lt;ServiceResponseSwagger&gt; serviceServiceGetServices(clusterName, fields, sortBy, pageSize, from, to)

Get all services

Returns all services.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ClusterServicesApi;


ClusterServicesApi apiInstance = new ClusterServicesApi();
String clusterName = "clusterName_example"; // String | 
String fields = "ServiceInfo/service_name,ServiceInfo/cluster_name"; // String | Filter fields in the response (identifier fields are mandatory)
String sortBy = "ServiceInfo/service_name.asc,ServiceInfo/cluster_name.asc"; // String | Sort resources in result by (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
String from = "0"; // String | The starting page resource (inclusive).  \"start\" is also accepted.
String to = "to_example"; // String | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    List<ServiceResponseSwagger> result = apiInstance.serviceServiceGetServices(clusterName, fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ClusterServicesApi#serviceServiceGetServices");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **clusterName** | **String**|  |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to ServiceInfo/service_name,ServiceInfo/cluster_name]
 **sortBy** | **String**| Sort resources in result by (asc | desc) | [optional] [default to ServiceInfo/service_name.asc,ServiceInfo/cluster_name.asc]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **String**| The starting page resource (inclusive).  \&quot;start\&quot; is also accepted. | [optional] [default to 0]
 **to** | **String**| The ending page resource (inclusive).  \&quot;end\&quot; is also accepted. | [optional]

### Return type

[**List&lt;ServiceResponseSwagger&gt;**](ServiceResponseSwagger.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="serviceServiceUpdateArtifact"></a>
# **serviceServiceUpdateArtifact**
> serviceServiceUpdateArtifact(serviceName, artifactName, clusterName, body)

Updates a single artifact



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ClusterServicesApi;


ClusterServicesApi apiInstance = new ClusterServicesApi();
String serviceName = "serviceName_example"; // String | 
String artifactName = "artifactName_example"; // String | 
String clusterName = "clusterName_example"; // String | 
ClusterServiceArtifactRequest body = new ClusterServiceArtifactRequest(); // ClusterServiceArtifactRequest | 
try {
    apiInstance.serviceServiceUpdateArtifact(serviceName, artifactName, clusterName, body);
} catch (ApiException e) {
    System.err.println("Exception when calling ClusterServicesApi#serviceServiceUpdateArtifact");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **serviceName** | **String**|  |
 **artifactName** | **String**|  |
 **clusterName** | **String**|  |
 **body** | [**ClusterServiceArtifactRequest**](ClusterServiceArtifactRequest.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="serviceServiceUpdateArtifacts"></a>
# **serviceServiceUpdateArtifacts**
> serviceServiceUpdateArtifacts(serviceName, clusterName, body)

Updates multiple artifacts



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ClusterServicesApi;


ClusterServicesApi apiInstance = new ClusterServicesApi();
String serviceName = "serviceName_example"; // String | 
String clusterName = "clusterName_example"; // String | 
ClusterServiceArtifactRequest body = new ClusterServiceArtifactRequest(); // ClusterServiceArtifactRequest | 
try {
    apiInstance.serviceServiceUpdateArtifacts(serviceName, clusterName, body);
} catch (ApiException e) {
    System.err.println("Exception when calling ClusterServicesApi#serviceServiceUpdateArtifacts");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **serviceName** | **String**|  |
 **clusterName** | **String**|  |
 **body** | [**ClusterServiceArtifactRequest**](ClusterServiceArtifactRequest.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="serviceServiceUpdateService"></a>
# **serviceServiceUpdateService**
> serviceServiceUpdateService(serviceName, clusterName, body)

Updates a service



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ClusterServicesApi;


ClusterServicesApi apiInstance = new ClusterServicesApi();
String serviceName = "serviceName_example"; // String | 
String clusterName = "clusterName_example"; // String | 
ServiceRequestSwagger body = new ServiceRequestSwagger(); // ServiceRequestSwagger | 
try {
    apiInstance.serviceServiceUpdateService(serviceName, clusterName, body);
} catch (ApiException e) {
    System.err.println("Exception when calling ClusterServicesApi#serviceServiceUpdateService");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **serviceName** | **String**|  |
 **clusterName** | **String**|  |
 **body** | [**ServiceRequestSwagger**](ServiceRequestSwagger.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

