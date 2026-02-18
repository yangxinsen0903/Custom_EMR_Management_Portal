# ServicesApi

All URIs are relative to *https://localhost/api/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**getRootHost**](ServicesApi.md#getRootHost) | **GET** /services/{serviceName}/hosts/{hostName} | Returns information about the given host
[**getRootHosts**](ServicesApi.md#getRootHosts) | **GET** /services/{serviceName}/hosts | Returns the list of hosts for the given root-level service
[**getRootService**](ServicesApi.md#getRootService) | **GET** /services/{serviceName} | Returns information about the given root-level service, including a list of its components
[**getRootServiceComponent**](ServicesApi.md#getRootServiceComponent) | **GET** /services/{serviceName}/components/{componentName} | Returns information about the given component for the given root-level service
[**getRootServiceComponentHosts**](ServicesApi.md#getRootServiceComponentHosts) | **GET** /services/{serviceName}/components/{componentName}/hostComponents | Returns the list of hosts for the given root-level service component
[**getRootServiceComponents**](ServicesApi.md#getRootServiceComponents) | **GET** /services/{serviceName}/components | Returns the list of components for the given root-level service
[**getRootServiceHostComponent**](ServicesApi.md#getRootServiceHostComponent) | **GET** /services/{serviceName}/hosts/{hostName}/hostComponents/{hostComponent} | Returns information about the given component for the given root-level service on the given host
[**getRootServiceHostComponents**](ServicesApi.md#getRootServiceHostComponents) | **GET** /services/{serviceName}/hosts/{hostName}/hostComponents | Returns the list of components for the given root-level service on the given host
[**getRootServices**](ServicesApi.md#getRootServices) | **GET** /services | Returns the list of root-level services


<a name="getRootHost"></a>
# **getRootHost**
> HostResponseWrapper getRootHost(hostName, fields)

Returns information about the given host



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ServicesApi;


ServicesApi apiInstance = new ServicesApi();
String hostName = "hostName_example"; // String | host name
String fields = "Hosts/_*"; // String | Filter fields in the response (identifier fields are mandatory)
try {
    HostResponseWrapper result = apiInstance.getRootHost(hostName, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ServicesApi#getRootHost");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **hostName** | **String**| host name |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to Hosts/*]

### Return type

[**HostResponseWrapper**](HostResponseWrapper.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getRootHosts"></a>
# **getRootHosts**
> List&lt;HostResponseWrapper&gt; getRootHosts(fields)

Returns the list of hosts for the given root-level service



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ServicesApi;


ServicesApi apiInstance = new ServicesApi();
String fields = "Hosts/host_name"; // String | Filter fields in the response (identifier fields are mandatory)
try {
    List<HostResponseWrapper> result = apiInstance.getRootHosts(fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ServicesApi#getRootHosts");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to Hosts/host_name]

### Return type

[**List&lt;HostResponseWrapper&gt;**](HostResponseWrapper.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getRootService"></a>
# **getRootService**
> RootServiceResponseWithComponentList getRootService(serviceName, fields)

Returns information about the given root-level service, including a list of its components



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ServicesApi;


ServicesApi apiInstance = new ServicesApi();
String serviceName = "serviceName_example"; // String | service name
String fields = "RootService/service_name, components/RootServiceComponents/component_name, components/RootServiceComponents/service_name"; // String | Filter fields in the response (identifier fields are mandatory)
try {
    RootServiceResponseWithComponentList result = apiInstance.getRootService(serviceName, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ServicesApi#getRootService");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **serviceName** | **String**| service name |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to RootService/service_name, components/RootServiceComponents/component_name, components/RootServiceComponents/service_name]

### Return type

[**RootServiceResponseWithComponentList**](RootServiceResponseWithComponentList.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getRootServiceComponent"></a>
# **getRootServiceComponent**
> RootServiceComponentWithHostComponentList getRootServiceComponent(serviceName, componentName, fields)

Returns information about the given component for the given root-level service



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ServicesApi;


ServicesApi apiInstance = new ServicesApi();
String serviceName = "serviceName_example"; // String | service name
String componentName = "componentName_example"; // String | component name
String fields = "RootServiceComponents/_*, hostComponents/RootServiceHostComponents/component_name, hostComponents/RootServiceHostComponents/host_name, hostComponents/RootServiceHostComponents/service_name"; // String | Filter fields in the response (identifier fields are mandatory)
try {
    RootServiceComponentWithHostComponentList result = apiInstance.getRootServiceComponent(serviceName, componentName, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ServicesApi#getRootServiceComponent");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **serviceName** | **String**| service name |
 **componentName** | **String**| component name |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to RootServiceComponents/*, hostComponents/RootServiceHostComponents/component_name, hostComponents/RootServiceHostComponents/host_name, hostComponents/RootServiceHostComponents/service_name]

### Return type

[**RootServiceComponentWithHostComponentList**](RootServiceComponentWithHostComponentList.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getRootServiceComponentHosts"></a>
# **getRootServiceComponentHosts**
> List&lt;RootServiceHostComponentResponseWrapper&gt; getRootServiceComponentHosts(serviceName, componentName, fields)

Returns the list of hosts for the given root-level service component



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ServicesApi;


ServicesApi apiInstance = new ServicesApi();
String serviceName = "serviceName_example"; // String | service name
String componentName = "componentName_example"; // String | component name
String fields = "RootServiceHostComponents/component_name, RootServiceHostComponents/host_name, RootServiceHostComponents/service_name"; // String | Filter fields in the response (identifier fields are mandatory)
try {
    List<RootServiceHostComponentResponseWrapper> result = apiInstance.getRootServiceComponentHosts(serviceName, componentName, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ServicesApi#getRootServiceComponentHosts");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **serviceName** | **String**| service name |
 **componentName** | **String**| component name |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to RootServiceHostComponents/component_name, RootServiceHostComponents/host_name, RootServiceHostComponents/service_name]

### Return type

[**List&lt;RootServiceHostComponentResponseWrapper&gt;**](RootServiceHostComponentResponseWrapper.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getRootServiceComponents"></a>
# **getRootServiceComponents**
> List&lt;RootServiceComponentResponseWrapper&gt; getRootServiceComponents(serviceName, fields)

Returns the list of components for the given root-level service



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ServicesApi;


ServicesApi apiInstance = new ServicesApi();
String serviceName = "serviceName_example"; // String | service name
String fields = "RootServiceComponents/component_name, RootServiceComponents/service_name"; // String | Filter fields in the response (identifier fields are mandatory)
try {
    List<RootServiceComponentResponseWrapper> result = apiInstance.getRootServiceComponents(serviceName, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ServicesApi#getRootServiceComponents");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **serviceName** | **String**| service name |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to RootServiceComponents/component_name, RootServiceComponents/service_name]

### Return type

[**List&lt;RootServiceComponentResponseWrapper&gt;**](RootServiceComponentResponseWrapper.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getRootServiceHostComponent"></a>
# **getRootServiceHostComponent**
> RootServiceHostComponentResponseWrapper getRootServiceHostComponent(serviceName, hostName, hostComponent, fields)

Returns information about the given component for the given root-level service on the given host



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ServicesApi;


ServicesApi apiInstance = new ServicesApi();
String serviceName = "serviceName_example"; // String | service name
String hostName = "hostName_example"; // String | host name
String hostComponent = "hostComponent_example"; // String | component name
String fields = "RootServiceHostComponents/component_name, RootServiceHostComponents/host_name, RootServiceHostComponents/service_name"; // String | Filter fields in the response (identifier fields are mandatory)
try {
    RootServiceHostComponentResponseWrapper result = apiInstance.getRootServiceHostComponent(serviceName, hostName, hostComponent, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ServicesApi#getRootServiceHostComponent");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **serviceName** | **String**| service name |
 **hostName** | **String**| host name |
 **hostComponent** | **String**| component name |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to RootServiceHostComponents/component_name, RootServiceHostComponents/host_name, RootServiceHostComponents/service_name]

### Return type

[**RootServiceHostComponentResponseWrapper**](RootServiceHostComponentResponseWrapper.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getRootServiceHostComponents"></a>
# **getRootServiceHostComponents**
> List&lt;RootServiceHostComponentResponseWrapper&gt; getRootServiceHostComponents(serviceName, hostName, fields)

Returns the list of components for the given root-level service on the given host



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ServicesApi;


ServicesApi apiInstance = new ServicesApi();
String serviceName = "serviceName_example"; // String | service name
String hostName = "hostName_example"; // String | host name
String fields = "RootServiceHostComponents/component_name, RootServiceHostComponents/host_name, RootServiceHostComponents/service_name"; // String | Filter fields in the response (identifier fields are mandatory)
try {
    List<RootServiceHostComponentResponseWrapper> result = apiInstance.getRootServiceHostComponents(serviceName, hostName, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ServicesApi#getRootServiceHostComponents");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **serviceName** | **String**| service name |
 **hostName** | **String**| host name |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to RootServiceHostComponents/component_name, RootServiceHostComponents/host_name, RootServiceHostComponents/service_name]

### Return type

[**List&lt;RootServiceHostComponentResponseWrapper&gt;**](RootServiceHostComponentResponseWrapper.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getRootServices"></a>
# **getRootServices**
> List&lt;RootServiceResponseWrapper&gt; getRootServices(fields)

Returns the list of root-level services



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ServicesApi;


ServicesApi apiInstance = new ServicesApi();
String fields = "RootService/service_name"; // String | Filter fields in the response (identifier fields are mandatory)
try {
    List<RootServiceResponseWrapper> result = apiInstance.getRootServices(fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ServicesApi#getRootServices");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to RootService/service_name]

### Return type

[**List&lt;RootServiceResponseWrapper&gt;**](RootServiceResponseWrapper.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

