# ViewsApi

All URIs are relative to *https://localhost/api/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createPrivilege**](ViewsApi.md#createPrivilege) | **POST** /views/{viewName}/versions/{version}/instances/{instanceName}/privileges | Create view instance privilege
[**createService**](ViewsApi.md#createService) | **POST** /views/{viewName}/versions/{version}/instances/{instanceName} | Create view instance
[**createServices**](ViewsApi.md#createServices) | **POST** /views/{viewName}/versions/{version}/instances | Create view instances
[**createUrl**](ViewsApi.md#createUrl) | **POST** /view/urls/{urlName} | Create view URL
[**deletePrivilege**](ViewsApi.md#deletePrivilege) | **DELETE** /views/{viewName}/versions/{version}/instances/{instanceName}/privileges/{privilegeId} | Delete privileges
[**deletePrivileges**](ViewsApi.md#deletePrivileges) | **DELETE** /views/{viewName}/versions/{version}/instances/{instanceName}/privileges | Delete view instance privileges
[**deleteService**](ViewsApi.md#deleteService) | **DELETE** /views/{viewName}/versions/{version}/instances/{instanceName} | Delete view instance
[**deleteUrl**](ViewsApi.md#deleteUrl) | **DELETE** /view/urls/{urlName} | Delete view URL
[**getPermission**](ViewsApi.md#getPermission) | **GET** /views/{viewName}/versions/{version}/permissions/{permissionId} | Get single view permission
[**getPermissions**](ViewsApi.md#getPermissions) | **GET** /views/{viewName}/versions/{version}/permissions | Get all permissions for a view
[**getPrivilege**](ViewsApi.md#getPrivilege) | **GET** /views/{viewName}/versions/{version}/instances/{instanceName}/privileges/{privilegeId} | Get single view instance privilege
[**getPrivileges**](ViewsApi.md#getPrivileges) | **GET** /views/{viewName}/versions/{version}/instances/{instanceName}/privileges | Get all view instance privileges
[**getService**](ViewsApi.md#getService) | **GET** /views/{viewName}/versions/{version}/instances/{instanceName} | Get single view instance
[**getServices**](ViewsApi.md#getServices) | **GET** /views/{viewName}/versions/{version}/instances | Get all view instances
[**getVersion**](ViewsApi.md#getVersion) | **GET** /views/{viewName}/versions/{version} | Get single view version
[**getVersions**](ViewsApi.md#getVersions) | **GET** /views/{viewName}/versions | Get all versions for a view
[**getView**](ViewsApi.md#getView) | **GET** /views/{viewName} | Get single view
[**getViewUrl**](ViewsApi.md#getViewUrl) | **GET** /view/urls/{urlName} | Get single view URL
[**getViewUrls**](ViewsApi.md#getViewUrls) | **GET** /view/urls | Get all view URLs
[**getViews**](ViewsApi.md#getViews) | **GET** /views | Get all views
[**migrateData**](ViewsApi.md#migrateData) | **PUT** /views/{viewName}/versions/{version}/instances/{instanceName}/migrate/{originVersion}/{originInstanceName} | Migrate view instance data
[**updatePrivileges**](ViewsApi.md#updatePrivileges) | **PUT** /views/{viewName}/versions/{version}/instances/{instanceName}/privileges | Update view instance privilege
[**updateService**](ViewsApi.md#updateService) | **PUT** /views/{viewName}/versions/{version}/instances/{instanceName} | Update view instance detail
[**updateServices**](ViewsApi.md#updateServices) | **PUT** /views/{viewName}/versions/{version}/instances | Update multiple view instance detail
[**updateUrl**](ViewsApi.md#updateUrl) | **PUT** /view/urls/{urlName} | Update view URL


<a name="createPrivilege"></a>
# **createPrivilege**
> createPrivilege(viewName, version, instanceName, body)

Create view instance privilege



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ViewsApi;


ViewsApi apiInstance = new ViewsApi();
String viewName = "viewName_example"; // String | view name
String version = "version_example"; // String | view version
String instanceName = "instanceName_example"; // String | instance name
ViewPrivilegeService body = new ViewPrivilegeService(); // ViewPrivilegeService | 
try {
    apiInstance.createPrivilege(viewName, version, instanceName, body);
} catch (ApiException e) {
    System.err.println("Exception when calling ViewsApi#createPrivilege");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **viewName** | **String**| view name |
 **version** | **String**| view version |
 **instanceName** | **String**| instance name |
 **body** | [**ViewPrivilegeService**](ViewPrivilegeService.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="createService"></a>
# **createService**
> createService(viewName, version, instanceName, body)

Create view instance



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ViewsApi;


ViewsApi apiInstance = new ViewsApi();
String viewName = "viewName_example"; // String | view name
String version = "version_example"; // String | 
String instanceName = "instanceName_example"; // String | instance name
ViewInstanceResponse body = new ViewInstanceResponse(); // ViewInstanceResponse | 
try {
    apiInstance.createService(viewName, version, instanceName, body);
} catch (ApiException e) {
    System.err.println("Exception when calling ViewsApi#createService");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **viewName** | **String**| view name |
 **version** | **String**|  |
 **instanceName** | **String**| instance name |
 **body** | [**ViewInstanceResponse**](ViewInstanceResponse.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="createServices"></a>
# **createServices**
> createServices(viewName, version, body)

Create view instances



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ViewsApi;


ViewsApi apiInstance = new ViewsApi();
String viewName = "viewName_example"; // String | 
String version = "version_example"; // String | 
ViewInstanceResponse body = new ViewInstanceResponse(); // ViewInstanceResponse | 
try {
    apiInstance.createServices(viewName, version, body);
} catch (ApiException e) {
    System.err.println("Exception when calling ViewsApi#createServices");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **viewName** | **String**|  |
 **version** | **String**|  |
 **body** | [**ViewInstanceResponse**](ViewInstanceResponse.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="createUrl"></a>
# **createUrl**
> createUrl(urlName, body)

Create view URL



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ViewsApi;


ViewsApi apiInstance = new ViewsApi();
String urlName = "urlName_example"; // String | 
ViewUrlResponseSwagger body = new ViewUrlResponseSwagger(); // ViewUrlResponseSwagger | 
try {
    apiInstance.createUrl(urlName, body);
} catch (ApiException e) {
    System.err.println("Exception when calling ViewsApi#createUrl");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **urlName** | **String**|  |
 **body** | [**ViewUrlResponseSwagger**](ViewUrlResponseSwagger.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="deletePrivilege"></a>
# **deletePrivilege**
> deletePrivilege(viewName, version, instanceName, privilegeId)

Delete privileges



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ViewsApi;


ViewsApi apiInstance = new ViewsApi();
String viewName = "viewName_example"; // String | view name
String version = "version_example"; // String | view version
String instanceName = "instanceName_example"; // String | instance name
String privilegeId = "privilegeId_example"; // String | privilege id
try {
    apiInstance.deletePrivilege(viewName, version, instanceName, privilegeId);
} catch (ApiException e) {
    System.err.println("Exception when calling ViewsApi#deletePrivilege");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **viewName** | **String**| view name |
 **version** | **String**| view version |
 **instanceName** | **String**| instance name |
 **privilegeId** | **String**| privilege id |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="deletePrivileges"></a>
# **deletePrivileges**
> deletePrivileges(viewName, viewVersion, instanceName)

Delete view instance privileges



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ViewsApi;


ViewsApi apiInstance = new ViewsApi();
String viewName = "viewName_example"; // String | view name
String viewVersion = "viewVersion_example"; // String | view version
String instanceName = "instanceName_example"; // String | instance name
try {
    apiInstance.deletePrivileges(viewName, viewVersion, instanceName);
} catch (ApiException e) {
    System.err.println("Exception when calling ViewsApi#deletePrivileges");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **viewName** | **String**| view name |
 **viewVersion** | **String**| view version |
 **instanceName** | **String**| instance name |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="deleteService"></a>
# **deleteService**
> deleteService(viewName, version, instanceName)

Delete view instance



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ViewsApi;


ViewsApi apiInstance = new ViewsApi();
String viewName = "viewName_example"; // String | view name
String version = "version_example"; // String | 
String instanceName = "instanceName_example"; // String | instance name
try {
    apiInstance.deleteService(viewName, version, instanceName);
} catch (ApiException e) {
    System.err.println("Exception when calling ViewsApi#deleteService");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **viewName** | **String**| view name |
 **version** | **String**|  |
 **instanceName** | **String**| instance name |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="deleteUrl"></a>
# **deleteUrl**
> deleteUrl(urlName)

Delete view URL



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ViewsApi;


ViewsApi apiInstance = new ViewsApi();
String urlName = "urlName_example"; // String | 
try {
    apiInstance.deleteUrl(urlName);
} catch (ApiException e) {
    System.err.println("Exception when calling ViewsApi#deleteUrl");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **urlName** | **String**|  |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getPermission"></a>
# **getPermission**
> ViewPermissionResponse getPermission(viewName, version, permissionId, fields)

Get single view permission



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ViewsApi;


ViewsApi apiInstance = new ViewsApi();
String viewName = "viewName_example"; // String | view name
String version = "version_example"; // String | view version
String permissionId = "permissionId_example"; // String | permission id
String fields = "PermissionInfo/_*"; // String | Filter fields in the response (identifier fields are mandatory)
try {
    ViewPermissionResponse result = apiInstance.getPermission(viewName, version, permissionId, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ViewsApi#getPermission");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **viewName** | **String**| view name |
 **version** | **String**| view version |
 **permissionId** | **String**| permission id |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to PermissionInfo/*]

### Return type

[**ViewPermissionResponse**](ViewPermissionResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getPermissions"></a>
# **getPermissions**
> List&lt;ViewPermissionResponse&gt; getPermissions(viewName, version, fields, sortBy, pageSize, from, to)

Get all permissions for a view



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ViewsApi;


ViewsApi apiInstance = new ViewsApi();
String viewName = "viewName_example"; // String | view name
String version = "version_example"; // String | view version
String fields = "PermissionInfo/_*"; // String | Filter fields in the response (identifier fields are mandatory)
String sortBy = "sortBy_example"; // String | Sort resources in result by (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
Integer from = 0; // Integer | The starting page resource (inclusive).  \"start\" is also accepted.
Integer to = 56; // Integer | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    List<ViewPermissionResponse> result = apiInstance.getPermissions(viewName, version, fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ViewsApi#getPermissions");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **viewName** | **String**| view name |
 **version** | **String**| view version |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to PermissionInfo/*]
 **sortBy** | **String**| Sort resources in result by (asc | desc) | [optional]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **Integer**| The starting page resource (inclusive).  \&quot;start\&quot; is also accepted. | [optional] [default to 0]
 **to** | **Integer**| The ending page resource (inclusive).  \&quot;end\&quot; is also accepted. | [optional]

### Return type

[**List&lt;ViewPermissionResponse&gt;**](ViewPermissionResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getPrivilege"></a>
# **getPrivilege**
> ViewPrivilegeResponseWrapper getPrivilege(viewName, version, instanceName, privilegeId, fields)

Get single view instance privilege



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ViewsApi;


ViewsApi apiInstance = new ViewsApi();
String viewName = "viewName_example"; // String | view name
String version = "version_example"; // String | view version
String instanceName = "instanceName_example"; // String | instance name
String privilegeId = "privilegeId_example"; // String | privilege id
String fields = "PrivilegeInfo/_*"; // String | Filter fields in the response (identifier fields are mandatory)
try {
    ViewPrivilegeResponseWrapper result = apiInstance.getPrivilege(viewName, version, instanceName, privilegeId, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ViewsApi#getPrivilege");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **viewName** | **String**| view name |
 **version** | **String**| view version |
 **instanceName** | **String**| instance name |
 **privilegeId** | **String**| privilege id |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to PrivilegeInfo/*]

### Return type

[**ViewPrivilegeResponseWrapper**](ViewPrivilegeResponseWrapper.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getPrivileges"></a>
# **getPrivileges**
> List&lt;ViewPrivilegeResponseWrapper&gt; getPrivileges(viewName, version, instanceName, fields, sortBy, pageSize, from, to)

Get all view instance privileges



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ViewsApi;


ViewsApi apiInstance = new ViewsApi();
String viewName = "viewName_example"; // String | view name
String version = "version_example"; // String | view version
String instanceName = "instanceName_example"; // String | instance name
String fields = "PrivilegeInfo/_*"; // String | Filter fields in the response (identifier fields are mandatory)
String sortBy = "sortBy_example"; // String | Sort resources in result by (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
Integer from = 0; // Integer | The starting page resource (inclusive).  \"start\" is also accepted.
Integer to = 56; // Integer | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    List<ViewPrivilegeResponseWrapper> result = apiInstance.getPrivileges(viewName, version, instanceName, fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ViewsApi#getPrivileges");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **viewName** | **String**| view name |
 **version** | **String**| view version |
 **instanceName** | **String**| instance name |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to PrivilegeInfo/*]
 **sortBy** | **String**| Sort resources in result by (asc | desc) | [optional]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **Integer**| The starting page resource (inclusive).  \&quot;start\&quot; is also accepted. | [optional] [default to 0]
 **to** | **Integer**| The ending page resource (inclusive).  \&quot;end\&quot; is also accepted. | [optional]

### Return type

[**List&lt;ViewPrivilegeResponseWrapper&gt;**](ViewPrivilegeResponseWrapper.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getService"></a>
# **getService**
> ViewInstanceResponse getService(viewName, version, instanceName, fields)

Get single view instance



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ViewsApi;


ViewsApi apiInstance = new ViewsApi();
String viewName = "viewName_example"; // String | view name
String version = "version_example"; // String | 
String instanceName = "instanceName_example"; // String | instance name
String fields = "ViewInstanceInfo/_*"; // String | Filter fields in the response (identifier fields are mandatory)
try {
    ViewInstanceResponse result = apiInstance.getService(viewName, version, instanceName, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ViewsApi#getService");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **viewName** | **String**| view name |
 **version** | **String**|  |
 **instanceName** | **String**| instance name |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to ViewInstanceInfo/*]

### Return type

[**ViewInstanceResponse**](ViewInstanceResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getServices"></a>
# **getServices**
> List&lt;ViewInstanceResponse&gt; getServices(viewName, version, fields, sortBy, pageSize, from, to)

Get all view instances



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ViewsApi;


ViewsApi apiInstance = new ViewsApi();
String viewName = "viewName_example"; // String | 
String version = "version_example"; // String | 
String fields = "ViewInstanceInfo/_*"; // String | Filter fields in the response (identifier fields are mandatory)
String sortBy = "sortBy_example"; // String | Sort resources in result by (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
Integer from = 0; // Integer | The starting page resource (inclusive).  \"start\" is also accepted.
Integer to = 56; // Integer | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    List<ViewInstanceResponse> result = apiInstance.getServices(viewName, version, fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ViewsApi#getServices");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **viewName** | **String**|  |
 **version** | **String**|  |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to ViewInstanceInfo/*]
 **sortBy** | **String**| Sort resources in result by (asc | desc) | [optional]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **Integer**| The starting page resource (inclusive).  \&quot;start\&quot; is also accepted. | [optional] [default to 0]
 **to** | **Integer**| The ending page resource (inclusive).  \&quot;end\&quot; is also accepted. | [optional]

### Return type

[**List&lt;ViewInstanceResponse&gt;**](ViewInstanceResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getVersion"></a>
# **getVersion**
> ViewVersionResponse getVersion(viewName, version, fields)

Get single view version



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ViewsApi;


ViewsApi apiInstance = new ViewsApi();
String viewName = "viewName_example"; // String | view name
String version = "version_example"; // String | 
String fields = "ViewVersionInfo/_*"; // String | Filter fields in the response (identifier fields are mandatory)
try {
    ViewVersionResponse result = apiInstance.getVersion(viewName, version, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ViewsApi#getVersion");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **viewName** | **String**| view name |
 **version** | **String**|  |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to ViewVersionInfo/*]

### Return type

[**ViewVersionResponse**](ViewVersionResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getVersions"></a>
# **getVersions**
> List&lt;ViewVersionResponse&gt; getVersions(viewName, fields, sortBy, pageSize, from, to)

Get all versions for a view



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ViewsApi;


ViewsApi apiInstance = new ViewsApi();
String viewName = "viewName_example"; // String | view name
String fields = "ViewVersionInfo/_*"; // String | Filter fields in the response (identifier fields are mandatory)
String sortBy = "sortBy_example"; // String | Sort resources in result by (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
Integer from = 0; // Integer | The starting page resource (inclusive).  \"start\" is also accepted.
Integer to = 56; // Integer | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    List<ViewVersionResponse> result = apiInstance.getVersions(viewName, fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ViewsApi#getVersions");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **viewName** | **String**| view name |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to ViewVersionInfo/*]
 **sortBy** | **String**| Sort resources in result by (asc | desc) | [optional]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **Integer**| The starting page resource (inclusive).  \&quot;start\&quot; is also accepted. | [optional] [default to 0]
 **to** | **Integer**| The ending page resource (inclusive).  \&quot;end\&quot; is also accepted. | [optional]

### Return type

[**List&lt;ViewVersionResponse&gt;**](ViewVersionResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getView"></a>
# **getView**
> ViewResponse getView(viewName, fields)

Get single view



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ViewsApi;


ViewsApi apiInstance = new ViewsApi();
String viewName = "viewName_example"; // String | view name
String fields = "Views/_*"; // String | Filter fields in the response (identifier fields are mandatory)
try {
    ViewResponse result = apiInstance.getView(viewName, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ViewsApi#getView");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **viewName** | **String**| view name |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to Views/*]

### Return type

[**ViewResponse**](ViewResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getViewUrl"></a>
# **getViewUrl**
> ViewUrlResponseSwagger getViewUrl(urlName, fields)

Get single view URL



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ViewsApi;


ViewsApi apiInstance = new ViewsApi();
String urlName = "urlName_example"; // String | 
String fields = "ViewUrlInfo/_*"; // String | Filter fields in the response (identifier fields are mandatory)
try {
    ViewUrlResponseSwagger result = apiInstance.getViewUrl(urlName, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ViewsApi#getViewUrl");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **urlName** | **String**|  |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to ViewUrlInfo/*]

### Return type

[**ViewUrlResponseSwagger**](ViewUrlResponseSwagger.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getViewUrls"></a>
# **getViewUrls**
> List&lt;ViewUrlResponseSwagger&gt; getViewUrls(fields, sortBy, pageSize, from, to)

Get all view URLs



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ViewsApi;


ViewsApi apiInstance = new ViewsApi();
String fields = "ViewUrlInfo/_*"; // String | Filter fields in the response (identifier fields are mandatory)
String sortBy = "sortBy_example"; // String | Sort resources in result by (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
Integer from = 0; // Integer | The starting page resource (inclusive).  \"start\" is also accepted.
Integer to = 56; // Integer | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    List<ViewUrlResponseSwagger> result = apiInstance.getViewUrls(fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ViewsApi#getViewUrls");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to ViewUrlInfo/*]
 **sortBy** | **String**| Sort resources in result by (asc | desc) | [optional]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **Integer**| The starting page resource (inclusive).  \&quot;start\&quot; is also accepted. | [optional] [default to 0]
 **to** | **Integer**| The ending page resource (inclusive).  \&quot;end\&quot; is also accepted. | [optional]

### Return type

[**List&lt;ViewUrlResponseSwagger&gt;**](ViewUrlResponseSwagger.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getViews"></a>
# **getViews**
> List&lt;ViewResponse&gt; getViews(fields, sortBy, pageSize, from, to)

Get all views

Returns details of all views.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ViewsApi;


ViewsApi apiInstance = new ViewsApi();
String fields = "Views/_*"; // String | Filter fields in the response (identifier fields are mandatory)
String sortBy = "Views/view_name.asc"; // String | Sort resources in result by (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
Integer from = 0; // Integer | The starting page resource (inclusive).  \"start\" is also accepted.
Integer to = 56; // Integer | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    List<ViewResponse> result = apiInstance.getViews(fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ViewsApi#getViews");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to Views/*]
 **sortBy** | **String**| Sort resources in result by (asc | desc) | [optional] [default to Views/view_name.asc]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **Integer**| The starting page resource (inclusive).  \&quot;start\&quot; is also accepted. | [optional] [default to 0]
 **to** | **Integer**| The ending page resource (inclusive).  \&quot;end\&quot; is also accepted. | [optional]

### Return type

[**List&lt;ViewResponse&gt;**](ViewResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="migrateData"></a>
# **migrateData**
> migrateData(viewName, version, instanceName, originVersion, originInstanceName)

Migrate view instance data

Migrates view instance persistence data from origin view instance specified in the path params.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ViewsApi;


ViewsApi apiInstance = new ViewsApi();
String viewName = "viewName_example"; // String | view name
String version = "version_example"; // String | view version
String instanceName = "instanceName_example"; // String | instance name
String originVersion = "originVersion_example"; // String | origin version
String originInstanceName = "originInstanceName_example"; // String | origin instance name
try {
    apiInstance.migrateData(viewName, version, instanceName, originVersion, originInstanceName);
} catch (ApiException e) {
    System.err.println("Exception when calling ViewsApi#migrateData");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **viewName** | **String**| view name |
 **version** | **String**| view version |
 **instanceName** | **String**| instance name |
 **originVersion** | **String**| origin version |
 **originInstanceName** | **String**| origin instance name |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="updatePrivileges"></a>
# **updatePrivileges**
> updatePrivileges(viewName, version, instanceName, body)

Update view instance privilege



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ViewsApi;


ViewsApi apiInstance = new ViewsApi();
String viewName = "viewName_example"; // String | view name
String version = "version_example"; // String | view version
String instanceName = "instanceName_example"; // String | instance name
ViewPrivilegeService body = new ViewPrivilegeService(); // ViewPrivilegeService | 
try {
    apiInstance.updatePrivileges(viewName, version, instanceName, body);
} catch (ApiException e) {
    System.err.println("Exception when calling ViewsApi#updatePrivileges");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **viewName** | **String**| view name |
 **version** | **String**| view version |
 **instanceName** | **String**| instance name |
 **body** | [**ViewPrivilegeService**](ViewPrivilegeService.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="updateService"></a>
# **updateService**
> updateService(viewName, version, instanceName, body)

Update view instance detail



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ViewsApi;


ViewsApi apiInstance = new ViewsApi();
String viewName = "viewName_example"; // String | view name
String version = "version_example"; // String | 
String instanceName = "instanceName_example"; // String | instance name
ViewInstanceResponse body = new ViewInstanceResponse(); // ViewInstanceResponse | 
try {
    apiInstance.updateService(viewName, version, instanceName, body);
} catch (ApiException e) {
    System.err.println("Exception when calling ViewsApi#updateService");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **viewName** | **String**| view name |
 **version** | **String**|  |
 **instanceName** | **String**| instance name |
 **body** | [**ViewInstanceResponse**](ViewInstanceResponse.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="updateServices"></a>
# **updateServices**
> updateServices(viewName, version, body)

Update multiple view instance detail



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ViewsApi;


ViewsApi apiInstance = new ViewsApi();
String viewName = "viewName_example"; // String | 
String version = "version_example"; // String | 
ViewInstanceResponse body = new ViewInstanceResponse(); // ViewInstanceResponse | 
try {
    apiInstance.updateServices(viewName, version, body);
} catch (ApiException e) {
    System.err.println("Exception when calling ViewsApi#updateServices");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **viewName** | **String**|  |
 **version** | **String**|  |
 **body** | [**ViewInstanceResponse**](ViewInstanceResponse.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="updateUrl"></a>
# **updateUrl**
> updateUrl(urlName, body)

Update view URL



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ViewsApi;


ViewsApi apiInstance = new ViewsApi();
String urlName = "urlName_example"; // String | 
ViewUrlResponseSwagger body = new ViewUrlResponseSwagger(); // ViewUrlResponseSwagger | 
try {
    apiInstance.updateUrl(urlName, body);
} catch (ApiException e) {
    System.err.println("Exception when calling ViewsApi#updateUrl");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **urlName** | **String**|  |
 **body** | [**ViewUrlResponseSwagger**](ViewUrlResponseSwagger.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

