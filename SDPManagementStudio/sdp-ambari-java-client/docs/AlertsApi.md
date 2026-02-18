# AlertsApi

All URIs are relative to *https://localhost/api/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createTarget**](AlertsApi.md#createTarget) | **POST** /alert_targets | Creates an alert target
[**deleteTarget**](AlertsApi.md#deleteTarget) | **DELETE** /alert_targets/{targetId} | Deletes an alert target
[**getTarget**](AlertsApi.md#getTarget) | **GET** /alert_targets/{targetId} | Returns a single alert target
[**getTargets**](AlertsApi.md#getTargets) | **GET** /alert_targets | Returns all alert targets
[**updateTarget**](AlertsApi.md#updateTarget) | **PUT** /alert_targets/{targetId} | Updates an alert target


<a name="createTarget"></a>
# **createTarget**
> createTarget(body, validateConfig, overwriteExisting)

Creates an alert target



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.AlertsApi;


AlertsApi apiInstance = new AlertsApi();
AlertTargetSwagger body = new AlertTargetSwagger(); // AlertTargetSwagger | 
String validateConfig = "validateConfig_example"; // String | 
String overwriteExisting = "overwriteExisting_example"; // String | 
try {
    apiInstance.createTarget(body, validateConfig, overwriteExisting);
} catch (ApiException e) {
    System.err.println("Exception when calling AlertsApi#createTarget");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**AlertTargetSwagger**](AlertTargetSwagger.md)|  | [optional]
 **validateConfig** | **String**|  | [optional]
 **overwriteExisting** | **String**|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="deleteTarget"></a>
# **deleteTarget**
> deleteTarget(targetId)

Deletes an alert target



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.AlertsApi;


AlertsApi apiInstance = new AlertsApi();
Long targetId = 789L; // Long | 
try {
    apiInstance.deleteTarget(targetId);
} catch (ApiException e) {
    System.err.println("Exception when calling AlertsApi#deleteTarget");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **targetId** | **Long**|  |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getTarget"></a>
# **getTarget**
> AlertTargetSwagger getTarget(targetId, fields, sortBy, pageSize, from, to)

Returns a single alert target



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.AlertsApi;


AlertsApi apiInstance = new AlertsApi();
Long targetId = 789L; // Long | alert target id
String fields = "AlertTarget/_*"; // String | Filter fields in the response (identifier fields are mandatory)
String sortBy = "sortBy_example"; // String | Sort resources in result by (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
Integer from = 0; // Integer | The starting page resource (inclusive).  \"start\" is also accepted.
Integer to = 56; // Integer | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    AlertTargetSwagger result = apiInstance.getTarget(targetId, fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling AlertsApi#getTarget");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **targetId** | **Long**| alert target id |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to AlertTarget/*]
 **sortBy** | **String**| Sort resources in result by (asc | desc) | [optional]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **Integer**| The starting page resource (inclusive).  \&quot;start\&quot; is also accepted. | [optional] [default to 0]
 **to** | **Integer**| The ending page resource (inclusive).  \&quot;end\&quot; is also accepted. | [optional]

### Return type

[**AlertTargetSwagger**](AlertTargetSwagger.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getTargets"></a>
# **getTargets**
> List&lt;AlertTargetSwagger&gt; getTargets(fields, sortBy, pageSize, from, to)

Returns all alert targets



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.AlertsApi;


AlertsApi apiInstance = new AlertsApi();
String fields = "AlertTarget/_*"; // String | Filter fields in the response (identifier fields are mandatory)
String sortBy = "sortBy_example"; // String | Sort resources in result by (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
Integer from = 0; // Integer | The starting page resource (inclusive).  \"start\" is also accepted.
Integer to = 56; // Integer | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    List<AlertTargetSwagger> result = apiInstance.getTargets(fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling AlertsApi#getTargets");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to AlertTarget/*]
 **sortBy** | **String**| Sort resources in result by (asc | desc) | [optional]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **Integer**| The starting page resource (inclusive).  \&quot;start\&quot; is also accepted. | [optional] [default to 0]
 **to** | **Integer**| The ending page resource (inclusive).  \&quot;end\&quot; is also accepted. | [optional]

### Return type

[**List&lt;AlertTargetSwagger&gt;**](AlertTargetSwagger.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="updateTarget"></a>
# **updateTarget**
> updateTarget(targetId, body)

Updates an alert target



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.AlertsApi;


AlertsApi apiInstance = new AlertsApi();
Long targetId = 789L; // Long | 
AlertTargetSwagger body = new AlertTargetSwagger(); // AlertTargetSwagger | 
try {
    apiInstance.updateTarget(targetId, body);
} catch (ApiException e) {
    System.err.println("Exception when calling AlertsApi#updateTarget");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **targetId** | **Long**|  |
 **body** | [**AlertTargetSwagger**](AlertTargetSwagger.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

