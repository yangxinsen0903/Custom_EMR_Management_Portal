# StacksApi

All URIs are relative to *https://localhost/api/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**stacksServiceGetServiceComponent**](StacksApi.md#stacksServiceGetServiceComponent) | **GET** /stacks/{stackName}/versions/{stackVersion}/services/{serviceName}/components/{componentName} | Get details for a stack service component
[**stacksServiceGetServiceComponentDependencies**](StacksApi.md#stacksServiceGetServiceComponentDependencies) | **GET** /stacks/{stackName}/versions/{stackVersion}/services/{serviceName}/components/{componentName}/dependencies | Get all dependencies for a stack service component
[**stacksServiceGetServiceComponentDependency**](StacksApi.md#stacksServiceGetServiceComponentDependency) | **GET** /stacks/{stackName}/versions/{stackVersion}/services/{serviceName}/components/{componentName}/dependencies/{dependencyName} | Get a stack service component dependency
[**stacksServiceGetServiceComponents**](StacksApi.md#stacksServiceGetServiceComponents) | **GET** /stacks/{stackName}/versions/{stackVersion}/services/{serviceName}/components | Get all components for a stack service
[**stacksServiceGetStack**](StacksApi.md#stacksServiceGetStack) | **GET** /stacks/{stackName} | Get a stack
[**stacksServiceGetStackArtifact**](StacksApi.md#stacksServiceGetStackArtifact) | **GET** /stacks/{stackName}/versions/{stackVersion}/artifacts/{artifactName} | Get stack artifact details
[**stacksServiceGetStackArtifacts**](StacksApi.md#stacksServiceGetStackArtifacts) | **GET** /stacks/{stackName}/versions/{stackVersion}/artifacts | Get all stack artifacts
[**stacksServiceGetStackConfiguration**](StacksApi.md#stacksServiceGetStackConfiguration) | **GET** /stacks/{stackName}/versions/{stackVersion}/services/{serviceName}/configurations/{propertyName} | Get stack service configuration details
[**stacksServiceGetStackConfigurationDependencies**](StacksApi.md#stacksServiceGetStackConfigurationDependencies) | **GET** /stacks/{stackName}/versions/{stackVersion}/services/{serviceName}/configurations/{propertyName}/dependencies | Get all dependencies for a stack service configuration
[**stacksServiceGetStackConfigurations**](StacksApi.md#stacksServiceGetStackConfigurations) | **GET** /stacks/{stackName}/versions/{stackVersion}/services/{serviceName}/configurations | Get all configurations for a stack service
[**stacksServiceGetStackLevelConfiguration**](StacksApi.md#stacksServiceGetStackLevelConfiguration) | **GET** /stacks/{stackName}/versions/{stackVersion}/configurations/{propertyName} | Get configuration details for a given property
[**stacksServiceGetStackLevelConfigurations**](StacksApi.md#stacksServiceGetStackLevelConfigurations) | **GET** /stacks/{stackName}/versions/{stackVersion}/configurations | Get all configurations for a stack version
[**stacksServiceGetStackService**](StacksApi.md#stacksServiceGetStackService) | **GET** /stacks/{stackName}/versions/{stackVersion}/services/{serviceName} | Get stack service details
[**stacksServiceGetStackServiceArtifact**](StacksApi.md#stacksServiceGetStackServiceArtifact) | **GET** /stacks/{stackName}/versions/{stackVersion}/services/{serviceName}/artifacts/{artifactName} | Get stack service artifact details
[**stacksServiceGetStackServiceArtifacts**](StacksApi.md#stacksServiceGetStackServiceArtifacts) | **GET** /stacks/{stackName}/versions/{stackVersion}/services/{serviceName}/artifacts | Get all artifacts for a stack service
[**stacksServiceGetStackServiceQuickLinksConfiguration**](StacksApi.md#stacksServiceGetStackServiceQuickLinksConfiguration) | **GET** /stacks/{stackName}/versions/{stackVersion}/services/{serviceName}/quicklinks/{quickLinksConfigurationName} | Get quicklinks configuration details
[**stacksServiceGetStackServiceQuickLinksConfigurations**](StacksApi.md#stacksServiceGetStackServiceQuickLinksConfigurations) | **GET** /stacks/{stackName}/versions/{stackVersion}/services/{serviceName}/quicklinks | Get all quicklinks configurations for a stack service
[**stacksServiceGetStackServiceTheme**](StacksApi.md#stacksServiceGetStackServiceTheme) | **GET** /stacks/{stackName}/versions/{stackVersion}/services/{serviceName}/themes/{themeName} | Get theme details for a stack service
[**stacksServiceGetStackServiceThemes**](StacksApi.md#stacksServiceGetStackServiceThemes) | **GET** /stacks/{stackName}/versions/{stackVersion}/services/{serviceName}/themes | Get all themes for a stack service
[**stacksServiceGetStackServices**](StacksApi.md#stacksServiceGetStackServices) | **GET** /stacks/{stackName}/versions/{stackVersion}/services | Get all services for a stack version
[**stacksServiceGetStackVersion**](StacksApi.md#stacksServiceGetStackVersion) | **GET** /stacks/{stackName}/versions/{stackVersion} | Get details for a stack version
[**stacksServiceGetStackVersionLinks**](StacksApi.md#stacksServiceGetStackVersionLinks) | **GET** /stacks/{stackName}/versions/{stackVersion}/links | Get extension links for a stack version
[**stacksServiceGetStackVersions**](StacksApi.md#stacksServiceGetStackVersions) | **GET** /stacks/{stackName}/versions | Get all versions for a stacks
[**stacksServiceGetStacks**](StacksApi.md#stacksServiceGetStacks) | **GET** /stacks | Get all stacks


<a name="stacksServiceGetServiceComponent"></a>
# **stacksServiceGetServiceComponent**
> StackServiceComponentResponseSwagger stacksServiceGetServiceComponent(stackName, stackVersion, serviceName, componentName, fields)

Get details for a stack service component

Returns details for a stack service component.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.StacksApi;


StacksApi apiInstance = new StacksApi();
String stackName = "stackName_example"; // String | 
String stackVersion = "stackVersion_example"; // String | 
String serviceName = "serviceName_example"; // String | 
String componentName = "componentName_example"; // String | 
String fields = "StackServiceComponents/_*"; // String | Filter returned attributes
try {
    StackServiceComponentResponseSwagger result = apiInstance.stacksServiceGetServiceComponent(stackName, stackVersion, serviceName, componentName, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling StacksApi#stacksServiceGetServiceComponent");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **stackName** | **String**|  |
 **stackVersion** | **String**|  |
 **serviceName** | **String**|  |
 **componentName** | **String**|  |
 **fields** | **String**| Filter returned attributes | [optional] [default to StackServiceComponents/*]

### Return type

[**StackServiceComponentResponseSwagger**](StackServiceComponentResponseSwagger.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="stacksServiceGetServiceComponentDependencies"></a>
# **stacksServiceGetServiceComponentDependencies**
> List&lt;ComponentDependencyResponse&gt; stacksServiceGetServiceComponentDependencies(stackName, stackVersion, serviceName, componentName, fields, sortBy, pageSize, from, to)

Get all dependencies for a stack service component

Returns all dependencies for a stack service component.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.StacksApi;


StacksApi apiInstance = new StacksApi();
String stackName = "stackName_example"; // String | 
String stackVersion = "stackVersion_example"; // String | 
String serviceName = "serviceName_example"; // String | 
String componentName = "componentName_example"; // String | 
String fields = "Dependencies/stack_name,Dependencies/stack_version,Dependencies/dependent_service_name,Dependencies/dependent_component_name,Dependencies/component_name"; // String | Filter returned attributes
String sortBy = "Dependencies/stack_name.asc,Dependencies/stack_version.asc,Dependencies/dependent_service_name.asc,Dependencies/dependent_component_name.asc,Dependencies/component_name.asc"; // String | Sort component dependencies (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
String from = "0"; // String | The starting page resource (inclusive).  \"start\" is also accepted.
String to = "to_example"; // String | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    List<ComponentDependencyResponse> result = apiInstance.stacksServiceGetServiceComponentDependencies(stackName, stackVersion, serviceName, componentName, fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling StacksApi#stacksServiceGetServiceComponentDependencies");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **stackName** | **String**|  |
 **stackVersion** | **String**|  |
 **serviceName** | **String**|  |
 **componentName** | **String**|  |
 **fields** | **String**| Filter returned attributes | [optional] [default to Dependencies/stack_name,Dependencies/stack_version,Dependencies/dependent_service_name,Dependencies/dependent_component_name,Dependencies/component_name]
 **sortBy** | **String**| Sort component dependencies (asc | desc) | [optional] [default to Dependencies/stack_name.asc,Dependencies/stack_version.asc,Dependencies/dependent_service_name.asc,Dependencies/dependent_component_name.asc,Dependencies/component_name.asc]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **String**| The starting page resource (inclusive).  \&quot;start\&quot; is also accepted. | [optional] [default to 0]
 **to** | **String**| The ending page resource (inclusive).  \&quot;end\&quot; is also accepted. | [optional]

### Return type

[**List&lt;ComponentDependencyResponse&gt;**](ComponentDependencyResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="stacksServiceGetServiceComponentDependency"></a>
# **stacksServiceGetServiceComponentDependency**
> ComponentDependencyResponse stacksServiceGetServiceComponentDependency(stackName, stackVersion, serviceName, componentName, dependencyName, fields)

Get a stack service component dependency

Returns a stack service component dependency.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.StacksApi;


StacksApi apiInstance = new StacksApi();
String stackName = "stackName_example"; // String | 
String stackVersion = "stackVersion_example"; // String | 
String serviceName = "serviceName_example"; // String | 
String componentName = "componentName_example"; // String | 
String dependencyName = "dependencyName_example"; // String | 
String fields = "Dependencies/_*"; // String | Filter returned attributes
try {
    ComponentDependencyResponse result = apiInstance.stacksServiceGetServiceComponentDependency(stackName, stackVersion, serviceName, componentName, dependencyName, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling StacksApi#stacksServiceGetServiceComponentDependency");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **stackName** | **String**|  |
 **stackVersion** | **String**|  |
 **serviceName** | **String**|  |
 **componentName** | **String**|  |
 **dependencyName** | **String**|  |
 **fields** | **String**| Filter returned attributes | [optional] [default to Dependencies/*]

### Return type

[**ComponentDependencyResponse**](ComponentDependencyResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="stacksServiceGetServiceComponents"></a>
# **stacksServiceGetServiceComponents**
> List&lt;StackServiceComponentResponseSwagger&gt; stacksServiceGetServiceComponents(stackName, stackVersion, serviceName, fields, sortBy, pageSize, from, to)

Get all components for a stack service

Returns all components for a stack service.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.StacksApi;


StacksApi apiInstance = new StacksApi();
String stackName = "stackName_example"; // String | 
String stackVersion = "stackVersion_example"; // String | 
String serviceName = "serviceName_example"; // String | 
String fields = "StackServiceComponents/component_name,StackServiceComponents/service_name,StackServiceComponents/stack_name,StackServiceComponents/stack_version"; // String | Filter returned attributes
String sortBy = "StackServiceComponents/component_name.asc,StackServiceComponents/service_name.asc,StackServiceComponents/stack_name.asc,StackServiceComponents/stack_version.asc"; // String | Sort service components (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
String from = "0"; // String | The starting page resource (inclusive).  \"start\" is also accepted.
String to = "to_example"; // String | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    List<StackServiceComponentResponseSwagger> result = apiInstance.stacksServiceGetServiceComponents(stackName, stackVersion, serviceName, fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling StacksApi#stacksServiceGetServiceComponents");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **stackName** | **String**|  |
 **stackVersion** | **String**|  |
 **serviceName** | **String**|  |
 **fields** | **String**| Filter returned attributes | [optional] [default to StackServiceComponents/component_name,StackServiceComponents/service_name,StackServiceComponents/stack_name,StackServiceComponents/stack_version]
 **sortBy** | **String**| Sort service components (asc | desc) | [optional] [default to StackServiceComponents/component_name.asc,StackServiceComponents/service_name.asc,StackServiceComponents/stack_name.asc,StackServiceComponents/stack_version.asc]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **String**| The starting page resource (inclusive).  \&quot;start\&quot; is also accepted. | [optional] [default to 0]
 **to** | **String**| The ending page resource (inclusive).  \&quot;end\&quot; is also accepted. | [optional]

### Return type

[**List&lt;StackServiceComponentResponseSwagger&gt;**](StackServiceComponentResponseSwagger.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="stacksServiceGetStack"></a>
# **stacksServiceGetStack**
> List&lt;StackResponseSwagger&gt; stacksServiceGetStack(stackName, fields)

Get a stack

Returns stack details.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.StacksApi;


StacksApi apiInstance = new StacksApi();
String stackName = "stackName_example"; // String | 
String fields = "Stacks/_*"; // String | Filter stack details
try {
    List<StackResponseSwagger> result = apiInstance.stacksServiceGetStack(stackName, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling StacksApi#stacksServiceGetStack");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **stackName** | **String**|  |
 **fields** | **String**| Filter stack details | [optional] [default to Stacks/*]

### Return type

[**List&lt;StackResponseSwagger&gt;**](StackResponseSwagger.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="stacksServiceGetStackArtifact"></a>
# **stacksServiceGetStackArtifact**
> StackArtifactResponse stacksServiceGetStackArtifact(stackName, stackVersion, artifactName, fields)

Get stack artifact details

Returns the details of a stack artifact

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.StacksApi;


StacksApi apiInstance = new StacksApi();
String stackName = "stackName_example"; // String | 
String stackVersion = "stackVersion_example"; // String | 
String artifactName = "artifactName_example"; // String | 
String fields = "Artifacts/_*"; // String | Filter returned attributes
try {
    StackArtifactResponse result = apiInstance.stacksServiceGetStackArtifact(stackName, stackVersion, artifactName, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling StacksApi#stacksServiceGetStackArtifact");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **stackName** | **String**|  |
 **stackVersion** | **String**|  |
 **artifactName** | **String**|  |
 **fields** | **String**| Filter returned attributes | [optional] [default to Artifacts/*]

### Return type

[**StackArtifactResponse**](StackArtifactResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="stacksServiceGetStackArtifacts"></a>
# **stacksServiceGetStackArtifacts**
> List&lt;StackArtifactResponse&gt; stacksServiceGetStackArtifacts(stackName, stackVersion, fields)

Get all stack artifacts

Returns all stack artifacts (e.g: kerberos descriptor, metrics descriptor)

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.StacksApi;


StacksApi apiInstance = new StacksApi();
String stackName = "stackName_example"; // String | 
String stackVersion = "stackVersion_example"; // String | 
String fields = "Artifacts/artifact_name,Artifacts/stack_name,Artifacts/stack_version"; // String | Filter returned attributes
try {
    List<StackArtifactResponse> result = apiInstance.stacksServiceGetStackArtifacts(stackName, stackVersion, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling StacksApi#stacksServiceGetStackArtifacts");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **stackName** | **String**|  |
 **stackVersion** | **String**|  |
 **fields** | **String**| Filter returned attributes | [optional] [default to Artifacts/artifact_name,Artifacts/stack_name,Artifacts/stack_version]

### Return type

[**List&lt;StackArtifactResponse&gt;**](StackArtifactResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="stacksServiceGetStackConfiguration"></a>
# **stacksServiceGetStackConfiguration**
> StackConfigurationResponseSwagger stacksServiceGetStackConfiguration(stackName, stackVersion, serviceName, propertyName, fields)

Get stack service configuration details

Returns the details of a stack service configuration.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.StacksApi;


StacksApi apiInstance = new StacksApi();
String stackName = "stackName_example"; // String | 
String stackVersion = "stackVersion_example"; // String | 
String serviceName = "serviceName_example"; // String | 
String propertyName = "propertyName_example"; // String | 
String fields = "StackConfigurations/_*"; // String | Filter returned attributes
try {
    StackConfigurationResponseSwagger result = apiInstance.stacksServiceGetStackConfiguration(stackName, stackVersion, serviceName, propertyName, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling StacksApi#stacksServiceGetStackConfiguration");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **stackName** | **String**|  |
 **stackVersion** | **String**|  |
 **serviceName** | **String**|  |
 **propertyName** | **String**|  |
 **fields** | **String**| Filter returned attributes | [optional] [default to StackConfigurations/*]

### Return type

[**StackConfigurationResponseSwagger**](StackConfigurationResponseSwagger.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="stacksServiceGetStackConfigurationDependencies"></a>
# **stacksServiceGetStackConfigurationDependencies**
> List&lt;StackConfigurationDependencyResponseSwagger&gt; stacksServiceGetStackConfigurationDependencies(stackName, stackVersion, serviceName, propertyName, fields, sortBy, pageSize, from, to)

Get all dependencies for a stack service configuration

Returns all dependencies for a stack service configuration.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.StacksApi;


StacksApi apiInstance = new StacksApi();
String stackName = "stackName_example"; // String | 
String stackVersion = "stackVersion_example"; // String | 
String serviceName = "serviceName_example"; // String | 
String propertyName = "propertyName_example"; // String | 
String fields = "StackConfigurationDependency/stack_name,StackConfigurationDependency/stack_version,StackConfigurationDependency/service_name,StackConfigurationDependency/property_name,StackConfigurationDependency/dependency_name"; // String | Filter returned attributes
String sortBy = "StackConfigurationDependency/stack_name.asc,StackConfigurationDependency/stack_version.asc,StackConfigurationDependency/service_name.asc,StackConfigurationDependency/property_name.asc,StackConfigurationDependency/dependency_name.asc"; // String | Sort configuration dependencies (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
String from = "0"; // String | The starting page resource (inclusive).  \"start\" is also accepted.
String to = "to_example"; // String | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    List<StackConfigurationDependencyResponseSwagger> result = apiInstance.stacksServiceGetStackConfigurationDependencies(stackName, stackVersion, serviceName, propertyName, fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling StacksApi#stacksServiceGetStackConfigurationDependencies");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **stackName** | **String**|  |
 **stackVersion** | **String**|  |
 **serviceName** | **String**|  |
 **propertyName** | **String**|  |
 **fields** | **String**| Filter returned attributes | [optional] [default to StackConfigurationDependency/stack_name,StackConfigurationDependency/stack_version,StackConfigurationDependency/service_name,StackConfigurationDependency/property_name,StackConfigurationDependency/dependency_name]
 **sortBy** | **String**| Sort configuration dependencies (asc | desc) | [optional] [default to StackConfigurationDependency/stack_name.asc,StackConfigurationDependency/stack_version.asc,StackConfigurationDependency/service_name.asc,StackConfigurationDependency/property_name.asc,StackConfigurationDependency/dependency_name.asc]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **String**| The starting page resource (inclusive).  \&quot;start\&quot; is also accepted. | [optional] [default to 0]
 **to** | **String**| The ending page resource (inclusive).  \&quot;end\&quot; is also accepted. | [optional]

### Return type

[**List&lt;StackConfigurationDependencyResponseSwagger&gt;**](StackConfigurationDependencyResponseSwagger.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="stacksServiceGetStackConfigurations"></a>
# **stacksServiceGetStackConfigurations**
> List&lt;StackConfigurationResponseSwagger&gt; stacksServiceGetStackConfigurations(stackName, stackVersion, serviceName, fields, sortBy, pageSize, from, to)

Get all configurations for a stack service

Returns all configurations for a stack service.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.StacksApi;


StacksApi apiInstance = new StacksApi();
String stackName = "stackName_example"; // String | 
String stackVersion = "stackVersion_example"; // String | 
String serviceName = "serviceName_example"; // String | 
String fields = "StackConfigurations/property_name,StackConfigurations/service_name,StackConfigurations/stack_nameStackConfigurations/stack_version"; // String | Filter returned attributes
String sortBy = "StackConfigurations/property_name.asc,StackConfigurations/service_name.asc,StackConfigurations/stack_name.ascStackConfigurations/stack_version.asc"; // String | Sort service configurations (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
String from = "0"; // String | The starting page resource (inclusive).  \"start\" is also accepted.
String to = "to_example"; // String | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    List<StackConfigurationResponseSwagger> result = apiInstance.stacksServiceGetStackConfigurations(stackName, stackVersion, serviceName, fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling StacksApi#stacksServiceGetStackConfigurations");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **stackName** | **String**|  |
 **stackVersion** | **String**|  |
 **serviceName** | **String**|  |
 **fields** | **String**| Filter returned attributes | [optional] [default to StackConfigurations/property_name,StackConfigurations/service_name,StackConfigurations/stack_nameStackConfigurations/stack_version]
 **sortBy** | **String**| Sort service configurations (asc | desc) | [optional] [default to StackConfigurations/property_name.asc,StackConfigurations/service_name.asc,StackConfigurations/stack_name.ascStackConfigurations/stack_version.asc]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **String**| The starting page resource (inclusive).  \&quot;start\&quot; is also accepted. | [optional] [default to 0]
 **to** | **String**| The ending page resource (inclusive).  \&quot;end\&quot; is also accepted. | [optional]

### Return type

[**List&lt;StackConfigurationResponseSwagger&gt;**](StackConfigurationResponseSwagger.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="stacksServiceGetStackLevelConfiguration"></a>
# **stacksServiceGetStackLevelConfiguration**
> StackConfigurationResponseSwagger stacksServiceGetStackLevelConfiguration(stackName, stackVersion, serviceName, propertyName, fields)

Get configuration details for a given property

Returns the configuration details for a given property.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.StacksApi;


StacksApi apiInstance = new StacksApi();
String stackName = "stackName_example"; // String | 
String stackVersion = "stackVersion_example"; // String | 
String serviceName = "serviceName_example"; // String | 
String propertyName = "propertyName_example"; // String | 
String fields = "StackLevelConfigurations/_*"; // String | Filter returned attributes
try {
    StackConfigurationResponseSwagger result = apiInstance.stacksServiceGetStackLevelConfiguration(stackName, stackVersion, serviceName, propertyName, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling StacksApi#stacksServiceGetStackLevelConfiguration");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **stackName** | **String**|  |
 **stackVersion** | **String**|  |
 **serviceName** | **String**|  |
 **propertyName** | **String**|  |
 **fields** | **String**| Filter returned attributes | [optional] [default to StackLevelConfigurations/*]

### Return type

[**StackConfigurationResponseSwagger**](StackConfigurationResponseSwagger.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="stacksServiceGetStackLevelConfigurations"></a>
# **stacksServiceGetStackLevelConfigurations**
> List&lt;StackConfigurationResponseSwagger&gt; stacksServiceGetStackLevelConfigurations(stackName, stackVersion, fields, sortBy, pageSize, from, to)

Get all configurations for a stack version

Returns all configurations for a stack version.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.StacksApi;


StacksApi apiInstance = new StacksApi();
String stackName = "stackName_example"; // String | 
String stackVersion = "stackVersion_example"; // String | 
String fields = "StackLevelConfigurations/stack_name,StackLevelConfigurations/stack_version,StackLevelConfigurations/property_name"; // String | Filter returned attributes
String sortBy = "StackLevelConfigurations/stack_name.asc,StackLevelConfigurations/stack_version.asc,StackLevelConfigurations/property_name.asc "; // String | Sort configuration (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
String from = "0"; // String | The starting page resource (inclusive).  \"start\" is also accepted.
String to = "to_example"; // String | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    List<StackConfigurationResponseSwagger> result = apiInstance.stacksServiceGetStackLevelConfigurations(stackName, stackVersion, fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling StacksApi#stacksServiceGetStackLevelConfigurations");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **stackName** | **String**|  |
 **stackVersion** | **String**|  |
 **fields** | **String**| Filter returned attributes | [optional] [default to StackLevelConfigurations/stack_name,StackLevelConfigurations/stack_version,StackLevelConfigurations/property_name]
 **sortBy** | **String**| Sort configuration (asc | desc) | [optional] [default to StackLevelConfigurations/stack_name.asc,StackLevelConfigurations/stack_version.asc,StackLevelConfigurations/property_name.asc ]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **String**| The starting page resource (inclusive).  \&quot;start\&quot; is also accepted. | [optional] [default to 0]
 **to** | **String**| The ending page resource (inclusive).  \&quot;end\&quot; is also accepted. | [optional]

### Return type

[**List&lt;StackConfigurationResponseSwagger&gt;**](StackConfigurationResponseSwagger.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="stacksServiceGetStackService"></a>
# **stacksServiceGetStackService**
> StackServiceResponseSwagger stacksServiceGetStackService(stackName, stackVersion, serviceName, fields)

Get stack service details

Returns the details of a stack service.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.StacksApi;


StacksApi apiInstance = new StacksApi();
String stackName = "stackName_example"; // String | 
String stackVersion = "stackVersion_example"; // String | 
String serviceName = "serviceName_example"; // String | 
String fields = "StackServices/_*"; // String | Filter returned attributes
try {
    StackServiceResponseSwagger result = apiInstance.stacksServiceGetStackService(stackName, stackVersion, serviceName, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling StacksApi#stacksServiceGetStackService");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **stackName** | **String**|  |
 **stackVersion** | **String**|  |
 **serviceName** | **String**|  |
 **fields** | **String**| Filter returned attributes | [optional] [default to StackServices/*]

### Return type

[**StackServiceResponseSwagger**](StackServiceResponseSwagger.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="stacksServiceGetStackServiceArtifact"></a>
# **stacksServiceGetStackServiceArtifact**
> StackArtifactResponse stacksServiceGetStackServiceArtifact(stackName, stackVersion, serviceName, artifactName, fields)

Get stack service artifact details

Returns the details of a stack service artifact.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.StacksApi;


StacksApi apiInstance = new StacksApi();
String stackName = "stackName_example"; // String | 
String stackVersion = "stackVersion_example"; // String | 
String serviceName = "serviceName_example"; // String | 
String artifactName = "artifactName_example"; // String | 
String fields = "Artifacts/_*"; // String | Filter returned attributes
try {
    StackArtifactResponse result = apiInstance.stacksServiceGetStackServiceArtifact(stackName, stackVersion, serviceName, artifactName, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling StacksApi#stacksServiceGetStackServiceArtifact");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **stackName** | **String**|  |
 **stackVersion** | **String**|  |
 **serviceName** | **String**|  |
 **artifactName** | **String**|  |
 **fields** | **String**| Filter returned attributes | [optional] [default to Artifacts/*]

### Return type

[**StackArtifactResponse**](StackArtifactResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="stacksServiceGetStackServiceArtifacts"></a>
# **stacksServiceGetStackServiceArtifacts**
> List&lt;StackServiceArtifactResponse&gt; stacksServiceGetStackServiceArtifacts(stackName, stackVersion, serviceName, fields, sortBy, pageSize, from, to)

Get all artifacts for a stack service

Returns all stack service artifacts

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.StacksApi;


StacksApi apiInstance = new StacksApi();
String stackName = "stackName_example"; // String | 
String stackVersion = "stackVersion_example"; // String | 
String serviceName = "serviceName_example"; // String | 
String fields = "Artifacts/artifact_name,Artifacts/stack_name,Artifacts/stack_version"; // String | Filter returned attributes
String sortBy = "Artifacts/artifact_name.asc,Artifacts/stack_name.asc,Artifacts/stack_version.asc"; // String | Sort service artifacts (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
String from = "0"; // String | The starting page resource (inclusive).  \"start\" is also accepted.
String to = "to_example"; // String | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    List<StackServiceArtifactResponse> result = apiInstance.stacksServiceGetStackServiceArtifacts(stackName, stackVersion, serviceName, fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling StacksApi#stacksServiceGetStackServiceArtifacts");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **stackName** | **String**|  |
 **stackVersion** | **String**|  |
 **serviceName** | **String**|  |
 **fields** | **String**| Filter returned attributes | [optional] [default to Artifacts/artifact_name,Artifacts/stack_name,Artifacts/stack_version]
 **sortBy** | **String**| Sort service artifacts (asc | desc) | [optional] [default to Artifacts/artifact_name.asc,Artifacts/stack_name.asc,Artifacts/stack_version.asc]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **String**| The starting page resource (inclusive).  \&quot;start\&quot; is also accepted. | [optional] [default to 0]
 **to** | **String**| The ending page resource (inclusive).  \&quot;end\&quot; is also accepted. | [optional]

### Return type

[**List&lt;StackServiceArtifactResponse&gt;**](StackServiceArtifactResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="stacksServiceGetStackServiceQuickLinksConfiguration"></a>
# **stacksServiceGetStackServiceQuickLinksConfiguration**
> List&lt;QuickLinksResponse&gt; stacksServiceGetStackServiceQuickLinksConfiguration(stackName, stackVersion, serviceName, quickLinksConfigurationName, fields)

Get quicklinks configuration details

Returns the details of a quicklinks configuration.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.StacksApi;


StacksApi apiInstance = new StacksApi();
String stackName = "stackName_example"; // String | 
String stackVersion = "stackVersion_example"; // String | 
String serviceName = "serviceName_example"; // String | 
String quickLinksConfigurationName = "quickLinksConfigurationName_example"; // String | 
String fields = "QuickLinkInfo/_*"; // String | Filter returned attributes
try {
    List<QuickLinksResponse> result = apiInstance.stacksServiceGetStackServiceQuickLinksConfiguration(stackName, stackVersion, serviceName, quickLinksConfigurationName, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling StacksApi#stacksServiceGetStackServiceQuickLinksConfiguration");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **stackName** | **String**|  |
 **stackVersion** | **String**|  |
 **serviceName** | **String**|  |
 **quickLinksConfigurationName** | **String**|  |
 **fields** | **String**| Filter returned attributes | [optional] [default to QuickLinkInfo/*]

### Return type

[**List&lt;QuickLinksResponse&gt;**](QuickLinksResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="stacksServiceGetStackServiceQuickLinksConfigurations"></a>
# **stacksServiceGetStackServiceQuickLinksConfigurations**
> List&lt;QuickLinksResponse&gt; stacksServiceGetStackServiceQuickLinksConfigurations(stackName, stackVersion, serviceName, fields, sortBy, pageSize, from, to)

Get all quicklinks configurations for a stack service

Returns all quicklinks configurations for a stack service.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.StacksApi;


StacksApi apiInstance = new StacksApi();
String stackName = "stackName_example"; // String | 
String stackVersion = "stackVersion_example"; // String | 
String serviceName = "serviceName_example"; // String | 
String fields = "QuickLinkInfo/file_name,QuickLinkInfo/service_name,QuickLinkInfo/stack_name,QuickLinkInfo/stack_version"; // String | Filter returned attributes
String sortBy = "QuickLinkInfo/file_name.asc,QuickLinkInfo/service_name.asc,QuickLinkInfo/stack_name.asc,QuickLinkInfo/stack_version.asc"; // String | Sort quick links (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
String from = "0"; // String | The starting page resource (inclusive).  \"start\" is also accepted.
String to = "to_example"; // String | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    List<QuickLinksResponse> result = apiInstance.stacksServiceGetStackServiceQuickLinksConfigurations(stackName, stackVersion, serviceName, fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling StacksApi#stacksServiceGetStackServiceQuickLinksConfigurations");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **stackName** | **String**|  |
 **stackVersion** | **String**|  |
 **serviceName** | **String**|  |
 **fields** | **String**| Filter returned attributes | [optional] [default to QuickLinkInfo/file_name,QuickLinkInfo/service_name,QuickLinkInfo/stack_name,QuickLinkInfo/stack_version]
 **sortBy** | **String**| Sort quick links (asc | desc) | [optional] [default to QuickLinkInfo/file_name.asc,QuickLinkInfo/service_name.asc,QuickLinkInfo/stack_name.asc,QuickLinkInfo/stack_version.asc]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **String**| The starting page resource (inclusive).  \&quot;start\&quot; is also accepted. | [optional] [default to 0]
 **to** | **String**| The ending page resource (inclusive).  \&quot;end\&quot; is also accepted. | [optional]

### Return type

[**List&lt;QuickLinksResponse&gt;**](QuickLinksResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="stacksServiceGetStackServiceTheme"></a>
# **stacksServiceGetStackServiceTheme**
> ThemeResponse stacksServiceGetStackServiceTheme(stackName, stackVersion, serviceName, themeName, fields)

Get theme details for a stack service

Returns stack service theme details.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.StacksApi;


StacksApi apiInstance = new StacksApi();
String stackName = "stackName_example"; // String | 
String stackVersion = "stackVersion_example"; // String | 
String serviceName = "serviceName_example"; // String | 
String themeName = "themeName_example"; // String | 
String fields = "ThemeInfo/_*"; // String | Filter returned attributes
try {
    ThemeResponse result = apiInstance.stacksServiceGetStackServiceTheme(stackName, stackVersion, serviceName, themeName, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling StacksApi#stacksServiceGetStackServiceTheme");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **stackName** | **String**|  |
 **stackVersion** | **String**|  |
 **serviceName** | **String**|  |
 **themeName** | **String**|  |
 **fields** | **String**| Filter returned attributes | [optional] [default to ThemeInfo/*]

### Return type

[**ThemeResponse**](ThemeResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="stacksServiceGetStackServiceThemes"></a>
# **stacksServiceGetStackServiceThemes**
> List&lt;ThemeResponse&gt; stacksServiceGetStackServiceThemes(stackName, stackVersion, serviceName, fields, sortBy, pageSize, from, to)

Get all themes for a stack service

Returns all stack themes

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.StacksApi;


StacksApi apiInstance = new StacksApi();
String stackName = "stackName_example"; // String | 
String stackVersion = "stackVersion_example"; // String | 
String serviceName = "serviceName_example"; // String | 
String fields = "ThemeInfo/file_name,ThemeInfo/service_name,ThemeInfo/stack_name,ThemeInfo/stack_version"; // String | Filter returned attributes
String sortBy = "ThemeInfo/file_name.asc,ThemeInfo/service_name.asc,ThemeInfo/stack_name.asc,ThemeInfo/stack_version.asc"; // String | Sort service artifacts (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
String from = "0"; // String | The starting page resource (inclusive).  \"start\" is also accepted.
String to = "to_example"; // String | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    List<ThemeResponse> result = apiInstance.stacksServiceGetStackServiceThemes(stackName, stackVersion, serviceName, fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling StacksApi#stacksServiceGetStackServiceThemes");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **stackName** | **String**|  |
 **stackVersion** | **String**|  |
 **serviceName** | **String**|  |
 **fields** | **String**| Filter returned attributes | [optional] [default to ThemeInfo/file_name,ThemeInfo/service_name,ThemeInfo/stack_name,ThemeInfo/stack_version]
 **sortBy** | **String**| Sort service artifacts (asc | desc) | [optional] [default to ThemeInfo/file_name.asc,ThemeInfo/service_name.asc,ThemeInfo/stack_name.asc,ThemeInfo/stack_version.asc]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **String**| The starting page resource (inclusive).  \&quot;start\&quot; is also accepted. | [optional] [default to 0]
 **to** | **String**| The ending page resource (inclusive).  \&quot;end\&quot; is also accepted. | [optional]

### Return type

[**List&lt;ThemeResponse&gt;**](ThemeResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="stacksServiceGetStackServices"></a>
# **stacksServiceGetStackServices**
> List&lt;StackServiceResponseSwagger&gt; stacksServiceGetStackServices(stackName, stackVersion, fields, sortBy, pageSize, from, to)

Get all services for a stack version

Returns all services for a stack version.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.StacksApi;


StacksApi apiInstance = new StacksApi();
String stackName = "stackName_example"; // String | 
String stackVersion = "stackVersion_example"; // String | 
String fields = "StackServices/stack_name,StackServices/stack_version,StackServices/service_name"; // String | Filter returned attributes
String sortBy = "StackServices/stack_name.asc,StackServices/stack_version.asc,StackServices/service_name.asc"; // String | Sort stack services (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
String from = "0"; // String | The starting page resource (inclusive).  \"start\" is also accepted.
String to = "to_example"; // String | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    List<StackServiceResponseSwagger> result = apiInstance.stacksServiceGetStackServices(stackName, stackVersion, fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling StacksApi#stacksServiceGetStackServices");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **stackName** | **String**|  |
 **stackVersion** | **String**|  |
 **fields** | **String**| Filter returned attributes | [optional] [default to StackServices/stack_name,StackServices/stack_version,StackServices/service_name]
 **sortBy** | **String**| Sort stack services (asc | desc) | [optional] [default to StackServices/stack_name.asc,StackServices/stack_version.asc,StackServices/service_name.asc]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **String**| The starting page resource (inclusive).  \&quot;start\&quot; is also accepted. | [optional] [default to 0]
 **to** | **String**| The ending page resource (inclusive).  \&quot;end\&quot; is also accepted. | [optional]

### Return type

[**List&lt;StackServiceResponseSwagger&gt;**](StackServiceResponseSwagger.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="stacksServiceGetStackVersion"></a>
# **stacksServiceGetStackVersion**
> StackVersionResponseSwagger stacksServiceGetStackVersion(stackName, stackVersion, fields)

Get details for a stack version

Returns the details for a stack version.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.StacksApi;


StacksApi apiInstance = new StacksApi();
String stackName = "stackName_example"; // String | 
String stackVersion = "stackVersion_example"; // String | 
String fields = "Versions/_*"; // String | Filter stack version details
try {
    StackVersionResponseSwagger result = apiInstance.stacksServiceGetStackVersion(stackName, stackVersion, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling StacksApi#stacksServiceGetStackVersion");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **stackName** | **String**|  |
 **stackVersion** | **String**|  |
 **fields** | **String**| Filter stack version details | [optional] [default to Versions/*]

### Return type

[**StackVersionResponseSwagger**](StackVersionResponseSwagger.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="stacksServiceGetStackVersionLinks"></a>
# **stacksServiceGetStackVersionLinks**
> List&lt;ExtensionLinkResponse&gt; stacksServiceGetStackVersionLinks(stackName, stackVersion, fields, sortBy, pageSize, from, to)

Get extension links for a stack version

Returns the extension links for a stack version.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.StacksApi;


StacksApi apiInstance = new StacksApi();
String stackName = "stackName_example"; // String | 
String stackVersion = "stackVersion_example"; // String | 
String fields = "ExtensionLink/link_id,ExtensionLink/stack_name,ExtensionLink/stack_version,ExtensionLink/extension_name,ExtensionLink/extension_version"; // String | Filter extension link attributes
String sortBy = "ExtensionLink/link_id.asc,ExtensionLink/stack_name.asc,ExtensionLink/stack_version.asc,ExtensionLink/extension_name.asc,ExtensionLink/extension_version.asc"; // String | Sort extension links (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
String from = "0"; // String | The starting page resource (inclusive).  \"start\" is also accepted.
String to = "to_example"; // String | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    List<ExtensionLinkResponse> result = apiInstance.stacksServiceGetStackVersionLinks(stackName, stackVersion, fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling StacksApi#stacksServiceGetStackVersionLinks");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **stackName** | **String**|  |
 **stackVersion** | **String**|  |
 **fields** | **String**| Filter extension link attributes | [optional] [default to ExtensionLink/link_id,ExtensionLink/stack_name,ExtensionLink/stack_version,ExtensionLink/extension_name,ExtensionLink/extension_version]
 **sortBy** | **String**| Sort extension links (asc | desc) | [optional] [default to ExtensionLink/link_id.asc,ExtensionLink/stack_name.asc,ExtensionLink/stack_version.asc,ExtensionLink/extension_name.asc,ExtensionLink/extension_version.asc]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **String**| The starting page resource (inclusive).  \&quot;start\&quot; is also accepted. | [optional] [default to 0]
 **to** | **String**| The ending page resource (inclusive).  \&quot;end\&quot; is also accepted. | [optional]

### Return type

[**List&lt;ExtensionLinkResponse&gt;**](ExtensionLinkResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="stacksServiceGetStackVersions"></a>
# **stacksServiceGetStackVersions**
> List&lt;StackVersionResponseSwagger&gt; stacksServiceGetStackVersions(stackName, fields, sortBy, pageSize, from, to)

Get all versions for a stacks

Returns all versions for a stack.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.StacksApi;


StacksApi apiInstance = new StacksApi();
String stackName = "stackName_example"; // String | 
String fields = "Versions/stack_name,Versions/stack_version"; // String | Filter stack version details
String sortBy = "Versions/stack_name.asc,Versions/stack_version.asc"; // String | Sort stack privileges (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
String from = "0"; // String | The starting page resource (inclusive).  \"start\" is also accepted.
String to = "to_example"; // String | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    List<StackVersionResponseSwagger> result = apiInstance.stacksServiceGetStackVersions(stackName, fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling StacksApi#stacksServiceGetStackVersions");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **stackName** | **String**|  |
 **fields** | **String**| Filter stack version details | [optional] [default to Versions/stack_name,Versions/stack_version]
 **sortBy** | **String**| Sort stack privileges (asc | desc) | [optional] [default to Versions/stack_name.asc,Versions/stack_version.asc]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **String**| The starting page resource (inclusive).  \&quot;start\&quot; is also accepted. | [optional] [default to 0]
 **to** | **String**| The ending page resource (inclusive).  \&quot;end\&quot; is also accepted. | [optional]

### Return type

[**List&lt;StackVersionResponseSwagger&gt;**](StackVersionResponseSwagger.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="stacksServiceGetStacks"></a>
# **stacksServiceGetStacks**
> List&lt;StackResponseSwagger&gt; stacksServiceGetStacks(fields, sortBy, pageSize, from, to)

Get all stacks

Returns all stacks.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.StacksApi;


StacksApi apiInstance = new StacksApi();
String fields = "Stacks/stack_name"; // String | Filter stack details
String sortBy = "Stacks/stack_name.asc"; // String | Sort stack privileges (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
String from = "0"; // String | The starting page resource (inclusive).  \"start\" is also accepted.
String to = "to_example"; // String | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    List<StackResponseSwagger> result = apiInstance.stacksServiceGetStacks(fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling StacksApi#stacksServiceGetStacks");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **fields** | **String**| Filter stack details | [optional] [default to Stacks/stack_name]
 **sortBy** | **String**| Sort stack privileges (asc | desc) | [optional] [default to Stacks/stack_name.asc]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **String**| The starting page resource (inclusive).  \&quot;start\&quot; is also accepted. | [optional] [default to 0]
 **to** | **String**| The ending page resource (inclusive).  \&quot;end\&quot; is also accepted. | [optional]

### Return type

[**List&lt;StackResponseSwagger&gt;**](StackResponseSwagger.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

