# RequestSchedulesApi

All URIs are relative to *https://localhost/api/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createRequestSchedule**](RequestSchedulesApi.md#createRequestSchedule) | **POST** /clusters/{clusterName}/request_schedules | Create new request schedule
[**deleteRequestSchedule**](RequestSchedulesApi.md#deleteRequestSchedule) | **DELETE** /clusters/{clusterName}/request_schedules/{requestScheduleId} | Delete a request schedule
[**getRequestSchedule**](RequestSchedulesApi.md#getRequestSchedule) | **GET** /clusters/{clusterName}/request_schedules/{requestScheduleId} | Get request schedule
[**getRequestSchedules**](RequestSchedulesApi.md#getRequestSchedules) | **GET** /clusters/{clusterName}/request_schedules | Get all request schedules


<a name="createRequestSchedule"></a>
# **createRequestSchedule**
> createRequestSchedule(clusterName, body)

Create new request schedule



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.RequestSchedulesApi;


RequestSchedulesApi apiInstance = new RequestSchedulesApi();
String clusterName = "clusterName_example"; // String | 
RequestScheduleRequestSwagger body = new RequestScheduleRequestSwagger(); // RequestScheduleRequestSwagger | 
try {
    apiInstance.createRequestSchedule(clusterName, body);
} catch (ApiException e) {
    System.err.println("Exception when calling RequestSchedulesApi#createRequestSchedule");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **clusterName** | **String**|  |
 **body** | [**RequestScheduleRequestSwagger**](RequestScheduleRequestSwagger.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="deleteRequestSchedule"></a>
# **deleteRequestSchedule**
> deleteRequestSchedule(requestScheduleId, clusterName)

Delete a request schedule

Changes status from COMPLETED to DISABLED

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.RequestSchedulesApi;


RequestSchedulesApi apiInstance = new RequestSchedulesApi();
String requestScheduleId = "requestScheduleId_example"; // String | 
String clusterName = "clusterName_example"; // String | 
try {
    apiInstance.deleteRequestSchedule(requestScheduleId, clusterName);
} catch (ApiException e) {
    System.err.println("Exception when calling RequestSchedulesApi#deleteRequestSchedule");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **requestScheduleId** | **String**|  |
 **clusterName** | **String**|  |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getRequestSchedule"></a>
# **getRequestSchedule**
> RequestScheduleResponseSwagger getRequestSchedule(requestScheduleId, clusterName, fields)

Get request schedule



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.RequestSchedulesApi;


RequestSchedulesApi apiInstance = new RequestSchedulesApi();
String requestScheduleId = "requestScheduleId_example"; // String | 
String clusterName = "clusterName_example"; // String | 
String fields = "RequestSchedule/_*"; // String | Filter fields in the response (identifier fields are mandatory)
try {
    RequestScheduleResponseSwagger result = apiInstance.getRequestSchedule(requestScheduleId, clusterName, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling RequestSchedulesApi#getRequestSchedule");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **requestScheduleId** | **String**|  |
 **clusterName** | **String**|  |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to RequestSchedule/*]

### Return type

[**RequestScheduleResponseSwagger**](RequestScheduleResponseSwagger.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getRequestSchedules"></a>
# **getRequestSchedules**
> List&lt;RequestScheduleResponseSwagger&gt; getRequestSchedules(clusterName, fields)

Get all request schedules



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.RequestSchedulesApi;


RequestSchedulesApi apiInstance = new RequestSchedulesApi();
String clusterName = "clusterName_example"; // String | 
String fields = "RequestSchedule/_*"; // String | Filter fields in the response (identifier fields are mandatory)
try {
    List<RequestScheduleResponseSwagger> result = apiInstance.getRequestSchedules(clusterName, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling RequestSchedulesApi#getRequestSchedules");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **clusterName** | **String**|  |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to RequestSchedule/*]

### Return type

[**List&lt;RequestScheduleResponseSwagger&gt;**](RequestScheduleResponseSwagger.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

