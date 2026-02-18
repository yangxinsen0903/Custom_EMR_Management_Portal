# ConfigurationsApi

All URIs are relative to *https://localhost/api/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createConfigurations**](ConfigurationsApi.md#createConfigurations) | **POST** /clusters/{clusterName}/configurations | Create new configurations
[**getConfigurations**](ConfigurationsApi.md#getConfigurations) | **GET** /clusters/{clusterName}/configurations | Get all configurations
[**getServiceConfigVersions**](ConfigurationsApi.md#getServiceConfigVersions) | **GET** /clusters/{clusterName}/configurations/service_config_versions | Get all service config versions


<a name="createConfigurations"></a>
# **createConfigurations**
> createConfigurations(clusterName, body)

Create new configurations



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ConfigurationsApi;


ConfigurationsApi apiInstance = new ConfigurationsApi();
String clusterName = "clusterName_example"; // String | 
ConfigurationRequest body = new ConfigurationRequest(); // ConfigurationRequest | 
try {
    apiInstance.createConfigurations(clusterName, body);
} catch (ApiException e) {
    System.err.println("Exception when calling ConfigurationsApi#createConfigurations");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **clusterName** | **String**|  |
 **body** | [**ConfigurationRequest**](ConfigurationRequest.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getConfigurations"></a>
# **getConfigurations**
> List&lt;ConfigurationResponse&gt; getConfigurations(clusterName, fields, sortBy, pageSize, from, to)

Get all configurations



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ConfigurationsApi;


ConfigurationsApi apiInstance = new ConfigurationsApi();
String clusterName = "clusterName_example"; // String | 
String fields = "fields_example"; // String | Filter fields in the response (identifier fields are mandatory)
String sortBy = "sortBy_example"; // String | Sort resources in result by (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
Integer from = 0; // Integer | The starting page resource (inclusive).  \"start\" is also accepted.
Integer to = 56; // Integer | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    List<ConfigurationResponse> result = apiInstance.getConfigurations(clusterName, fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ConfigurationsApi#getConfigurations");
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

[**List&lt;ConfigurationResponse&gt;**](ConfigurationResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getServiceConfigVersions"></a>
# **getServiceConfigVersions**
> List&lt;ServiceConfigVersionResponse&gt; getServiceConfigVersions(clusterName, fields, sortBy, pageSize, from, to)

Get all service config versions



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ConfigurationsApi;


ConfigurationsApi apiInstance = new ConfigurationsApi();
String clusterName = "clusterName_example"; // String | 
String fields = "fields_example"; // String | Filter fields in the response (identifier fields are mandatory)
String sortBy = "sortBy_example"; // String | Sort resources in result by (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
Integer from = 0; // Integer | The starting page resource (inclusive).  \"start\" is also accepted.
Integer to = 56; // Integer | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    List<ServiceConfigVersionResponse> result = apiInstance.getServiceConfigVersions(clusterName, fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ConfigurationsApi#getServiceConfigVersions");
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

[**List&lt;ServiceConfigVersionResponse&gt;**](ServiceConfigVersionResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

