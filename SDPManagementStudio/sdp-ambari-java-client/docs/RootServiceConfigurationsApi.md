# RootServiceConfigurationsApi

All URIs are relative to *https://localhost/api/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**rootServiceComponentConfigurationServiceDeleteConfiguration**](RootServiceConfigurationsApi.md#rootServiceComponentConfigurationServiceDeleteConfiguration) | **DELETE** /services/{serviceName}/components/{componentName}/configurations/{category} | Deletes a root service component configuration resource
[**rootServiceComponentConfigurationServiceGetConfiguration**](RootServiceConfigurationsApi.md#rootServiceComponentConfigurationServiceGetConfiguration) | **GET** /services/{serviceName}/components/{componentName}/configurations/{category} | Retrieve the details of a root service component configuration resource
[**rootServiceComponentConfigurationServiceUpdateConfiguration**](RootServiceConfigurationsApi.md#rootServiceComponentConfigurationServiceUpdateConfiguration) | **PUT** /services/{serviceName}/components/{componentName}/configurations/{category} | Updates root service component configuration resources 


<a name="rootServiceComponentConfigurationServiceDeleteConfiguration"></a>
# **rootServiceComponentConfigurationServiceDeleteConfiguration**
> rootServiceComponentConfigurationServiceDeleteConfiguration(category, serviceName, componentName)

Deletes a root service component configuration resource



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.RootServiceConfigurationsApi;


RootServiceConfigurationsApi apiInstance = new RootServiceConfigurationsApi();
String category = "category_example"; // String | 
String serviceName = "serviceName_example"; // String | 
String componentName = "componentName_example"; // String | 
try {
    apiInstance.rootServiceComponentConfigurationServiceDeleteConfiguration(category, serviceName, componentName);
} catch (ApiException e) {
    System.err.println("Exception when calling RootServiceConfigurationsApi#rootServiceComponentConfigurationServiceDeleteConfiguration");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **category** | **String**|  |
 **serviceName** | **String**|  |
 **componentName** | **String**|  |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="rootServiceComponentConfigurationServiceGetConfiguration"></a>
# **rootServiceComponentConfigurationServiceGetConfiguration**
> RootServiceComponentConfigurationResponseSwagger rootServiceComponentConfigurationServiceGetConfiguration(category, serviceName, componentName, fields)

Retrieve the details of a root service component configuration resource



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.RootServiceConfigurationsApi;


RootServiceConfigurationsApi apiInstance = new RootServiceConfigurationsApi();
String category = "category_example"; // String | 
String serviceName = "serviceName_example"; // String | 
String componentName = "componentName_example"; // String | 
String fields = "Configuration/_*"; // String | Filter fields in the response (identifier fields are mandatory)
try {
    RootServiceComponentConfigurationResponseSwagger result = apiInstance.rootServiceComponentConfigurationServiceGetConfiguration(category, serviceName, componentName, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling RootServiceConfigurationsApi#rootServiceComponentConfigurationServiceGetConfiguration");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **category** | **String**|  |
 **serviceName** | **String**|  |
 **componentName** | **String**|  |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to Configuration/*]

### Return type

[**RootServiceComponentConfigurationResponseSwagger**](RootServiceComponentConfigurationResponseSwagger.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="rootServiceComponentConfigurationServiceUpdateConfiguration"></a>
# **rootServiceComponentConfigurationServiceUpdateConfiguration**
> rootServiceComponentConfigurationServiceUpdateConfiguration(category, serviceName, componentName, body, fields)

Updates root service component configuration resources 



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.RootServiceConfigurationsApi;


RootServiceConfigurationsApi apiInstance = new RootServiceConfigurationsApi();
String category = "category_example"; // String | 
String serviceName = "serviceName_example"; // String | 
String componentName = "componentName_example"; // String | 
RootServiceComponentConfigurationRequestSwagger body = new RootServiceComponentConfigurationRequestSwagger(); // RootServiceComponentConfigurationRequestSwagger | 
String fields = "Configuration/_*"; // String | Filter fields in the response (identifier fields are mandatory)
try {
    apiInstance.rootServiceComponentConfigurationServiceUpdateConfiguration(category, serviceName, componentName, body, fields);
} catch (ApiException e) {
    System.err.println("Exception when calling RootServiceConfigurationsApi#rootServiceComponentConfigurationServiceUpdateConfiguration");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **category** | **String**|  |
 **serviceName** | **String**|  |
 **componentName** | **String**|  |
 **body** | [**RootServiceComponentConfigurationRequestSwagger**](RootServiceComponentConfigurationRequestSwagger.md)|  | [optional]
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to Configuration/*]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

