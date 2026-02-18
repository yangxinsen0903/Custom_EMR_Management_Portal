# ClustersApi

All URIs are relative to *https://localhost/api/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createCluster**](ClustersApi.md#createCluster) | **POST** /clusters/{clusterName} | Creates a cluster
[**createClusterArtifact**](ClustersApi.md#createClusterArtifact) | **POST** /clusters/{clusterName}/artifacts/{artifactName} | Creates a cluster artifact
[**deleteCluster**](ClustersApi.md#deleteCluster) | **DELETE** /clusters/{clusterName} | Deletes a cluster
[**deleteClusterArtifact**](ClustersApi.md#deleteClusterArtifact) | **DELETE** /clusters/{clusterName}/artifacts/{artifactName} | Deletes a single artifact
[**deleteClusterArtifacts**](ClustersApi.md#deleteClusterArtifacts) | **DELETE** /clusters/{clusterName}/artifacts | Deletes all artifacts of a cluster that match the provided predicate
[**getCluster**](ClustersApi.md#getCluster) | **GET** /clusters/{clusterName} | Returns information about a specific cluster
[**getClusterArtifact**](ClustersApi.md#getClusterArtifact) | **GET** /clusters/{clusterName}/artifacts/{artifactName} | Get the details of a cluster artifact
[**getClusterArtifacts**](ClustersApi.md#getClusterArtifacts) | **GET** /clusters/{clusterName}/artifacts | Returns all artifacts associated with the cluster
[**getClusters**](ClustersApi.md#getClusters) | **GET** /clusters | Returns all clusters
[**updateCluster**](ClustersApi.md#updateCluster) | **PUT** /clusters/{clusterName} | Updates a cluster
[**updateClusterArtifact**](ClustersApi.md#updateClusterArtifact) | **PUT** /clusters/{clusterName}/artifacts/{artifactName} | Updates a single artifact
[**updateClusterArtifacts**](ClustersApi.md#updateClusterArtifacts) | **PUT** /clusters/{clusterName}/artifacts | Updates multiple artifacts


<a name="createCluster"></a>
# **createCluster**
> createCluster(clusterName, body)

Creates a cluster



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ClustersApi;


ClustersApi apiInstance = new ClustersApi();
String clusterName = "clusterName_example"; // String | 
ClusterRequestSwagger body = new ClusterRequestSwagger(); // ClusterRequestSwagger | 
try {
    apiInstance.createCluster(clusterName, body);
} catch (ApiException e) {
    System.err.println("Exception when calling ClustersApi#createCluster");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **clusterName** | **String**|  |
 **body** | [**ClusterRequestSwagger**](ClusterRequestSwagger.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="createClusterArtifact"></a>
# **createClusterArtifact**
> createClusterArtifact(clusterName, artifactName, body)

Creates a cluster artifact



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ClustersApi;


ClustersApi apiInstance = new ClustersApi();
String clusterName = "clusterName_example"; // String | 
String artifactName = "artifactName_example"; // String | 
ClusterArtifactRequest body = new ClusterArtifactRequest(); // ClusterArtifactRequest | 
try {
    apiInstance.createClusterArtifact(clusterName, artifactName, body);
} catch (ApiException e) {
    System.err.println("Exception when calling ClustersApi#createClusterArtifact");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **clusterName** | **String**|  |
 **artifactName** | **String**|  |
 **body** | [**ClusterArtifactRequest**](ClusterArtifactRequest.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="deleteCluster"></a>
# **deleteCluster**
> deleteCluster(clusterName)

Deletes a cluster



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ClustersApi;


ClustersApi apiInstance = new ClustersApi();
String clusterName = "clusterName_example"; // String | 
try {
    apiInstance.deleteCluster(clusterName);
} catch (ApiException e) {
    System.err.println("Exception when calling ClustersApi#deleteCluster");
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

<a name="deleteClusterArtifact"></a>
# **deleteClusterArtifact**
> deleteClusterArtifact(clusterName, artifactName)

Deletes a single artifact



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ClustersApi;


ClustersApi apiInstance = new ClustersApi();
String clusterName = "clusterName_example"; // String | 
String artifactName = "artifactName_example"; // String | 
try {
    apiInstance.deleteClusterArtifact(clusterName, artifactName);
} catch (ApiException e) {
    System.err.println("Exception when calling ClustersApi#deleteClusterArtifact");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **clusterName** | **String**|  |
 **artifactName** | **String**|  |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="deleteClusterArtifacts"></a>
# **deleteClusterArtifacts**
> deleteClusterArtifacts(clusterName)

Deletes all artifacts of a cluster that match the provided predicate



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ClustersApi;


ClustersApi apiInstance = new ClustersApi();
String clusterName = "clusterName_example"; // String | 
try {
    apiInstance.deleteClusterArtifacts(clusterName);
} catch (ApiException e) {
    System.err.println("Exception when calling ClustersApi#deleteClusterArtifacts");
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

<a name="getCluster"></a>
# **getCluster**
> ClusterResponseWrapper getCluster(clusterName, fields)

Returns information about a specific cluster



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ClustersApi;


ClustersApi apiInstance = new ClustersApi();
String clusterName = "clusterName_example"; // String | 
String fields = "Clusters/_*"; // String | Filter fields in the response (identifier fields are mandatory)
try {
    ClusterResponseWrapper result = apiInstance.getCluster(clusterName, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ClustersApi#getCluster");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **clusterName** | **String**|  |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to Clusters/*]

### Return type

[**ClusterResponseWrapper**](ClusterResponseWrapper.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getClusterArtifact"></a>
# **getClusterArtifact**
> ClusterArtifactResponse getClusterArtifact(clusterName, artifactName, fields, sortBy, pageSize, from, to)

Get the details of a cluster artifact



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ClustersApi;


ClustersApi apiInstance = new ClustersApi();
String clusterName = "clusterName_example"; // String | 
String artifactName = "artifactName_example"; // String | 
String fields = "fields_example"; // String | Filter fields in the response (identifier fields are mandatory)
String sortBy = "sortBy_example"; // String | Sort resources in result by (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
String from = "0"; // String | The starting page resource (inclusive).  \"start\" is also accepted.
String to = "to_example"; // String | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    ClusterArtifactResponse result = apiInstance.getClusterArtifact(clusterName, artifactName, fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ClustersApi#getClusterArtifact");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **clusterName** | **String**|  |
 **artifactName** | **String**|  |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional]
 **sortBy** | **String**| Sort resources in result by (asc | desc) | [optional]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **String**| The starting page resource (inclusive).  \&quot;start\&quot; is also accepted. | [optional] [default to 0]
 **to** | **String**| The ending page resource (inclusive).  \&quot;end\&quot; is also accepted. | [optional]

### Return type

[**ClusterArtifactResponse**](ClusterArtifactResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getClusterArtifacts"></a>
# **getClusterArtifacts**
> List&lt;ClusterArtifactResponse&gt; getClusterArtifacts(clusterName, fields, sortBy, pageSize, from, to)

Returns all artifacts associated with the cluster



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ClustersApi;


ClustersApi apiInstance = new ClustersApi();
String clusterName = "clusterName_example"; // String | 
String fields = "fields_example"; // String | Filter fields in the response (identifier fields are mandatory)
String sortBy = "sortBy_example"; // String | Sort resources in result by (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
Integer from = 0; // Integer | The starting page resource (inclusive).  \"start\" is also accepted.
Integer to = 56; // Integer | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    List<ClusterArtifactResponse> result = apiInstance.getClusterArtifacts(clusterName, fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ClustersApi#getClusterArtifacts");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **clusterName** | **String**|  |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional]
 **sortBy** | **String**| Sort resources in result by (asc | desc) | [optional]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **Integer**| The starting page resource (inclusive).  \&quot;start\&quot; is also accepted. | [optional] [default to 0]
 **to** | **Integer**| The ending page resource (inclusive).  \&quot;end\&quot; is also accepted. | [optional]

### Return type

[**List&lt;ClusterArtifactResponse&gt;**](ClusterArtifactResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getClusters"></a>
# **getClusters**
> List&lt;ClusterResponseWrapper&gt; getClusters(fields, sortBy, pageSize, from, to)

Returns all clusters



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ClustersApi;


ClustersApi apiInstance = new ClustersApi();
String fields = "fields_example"; // String | Filter fields in the response (identifier fields are mandatory)
String sortBy = "sortBy_example"; // String | Sort resources in result by (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
Integer from = 0; // Integer | The starting page resource (inclusive).  \"start\" is also accepted.
Integer to = 56; // Integer | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    List<ClusterResponseWrapper> result = apiInstance.getClusters(fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ClustersApi#getClusters");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional]
 **sortBy** | **String**| Sort resources in result by (asc | desc) | [optional]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **Integer**| The starting page resource (inclusive).  \&quot;start\&quot; is also accepted. | [optional] [default to 0]
 **to** | **Integer**| The ending page resource (inclusive).  \&quot;end\&quot; is also accepted. | [optional]

### Return type

[**List&lt;ClusterResponseWrapper&gt;**](ClusterResponseWrapper.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="updateCluster"></a>
# **updateCluster**
> updateCluster(clusterName, body)

Updates a cluster



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ClustersApi;


ClustersApi apiInstance = new ClustersApi();
String clusterName = "clusterName_example"; // String | 
ClusterRequestSwagger body = new ClusterRequestSwagger(); // ClusterRequestSwagger | 
try {
    apiInstance.updateCluster(clusterName, body);
} catch (ApiException e) {
    System.err.println("Exception when calling ClustersApi#updateCluster");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **clusterName** | **String**|  |
 **body** | [**ClusterRequestSwagger**](ClusterRequestSwagger.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="updateClusterArtifact"></a>
# **updateClusterArtifact**
> updateClusterArtifact(clusterName, artifactName, body)

Updates a single artifact



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ClustersApi;


ClustersApi apiInstance = new ClustersApi();
String clusterName = "clusterName_example"; // String | 
String artifactName = "artifactName_example"; // String | 
ClusterArtifactRequest body = new ClusterArtifactRequest(); // ClusterArtifactRequest | 
try {
    apiInstance.updateClusterArtifact(clusterName, artifactName, body);
} catch (ApiException e) {
    System.err.println("Exception when calling ClustersApi#updateClusterArtifact");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **clusterName** | **String**|  |
 **artifactName** | **String**|  |
 **body** | [**ClusterArtifactRequest**](ClusterArtifactRequest.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="updateClusterArtifacts"></a>
# **updateClusterArtifacts**
> updateClusterArtifacts(clusterName, body)

Updates multiple artifacts



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ClustersApi;


ClustersApi apiInstance = new ClustersApi();
String clusterName = "clusterName_example"; // String | 
ClusterArtifactRequest body = new ClusterArtifactRequest(); // ClusterArtifactRequest | 
try {
    apiInstance.updateClusterArtifacts(clusterName, body);
} catch (ApiException e) {
    System.err.println("Exception when calling ClustersApi#updateClusterArtifacts");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **clusterName** | **String**|  |
 **body** | [**ClusterArtifactRequest**](ClusterArtifactRequest.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

