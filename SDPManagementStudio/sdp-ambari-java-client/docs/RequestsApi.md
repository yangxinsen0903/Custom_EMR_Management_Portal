# RequestsApi

All URIs are relative to *https://localhost/api/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**requestServiceCreateRequests**](RequestsApi.md#requestServiceCreateRequests) | **POST** /requests | Creates one or more Requests
[**requestServiceGetRequest**](RequestsApi.md#requestServiceGetRequest) | **GET** /requests/{requestId} | Get the details of a request
[**requestServiceGetRequests**](RequestsApi.md#requestServiceGetRequests) | **GET** /requests | Get all requests. A predicate can be given to filter results.
[**requestServiceUpdateRequests**](RequestsApi.md#requestServiceUpdateRequests) | **PUT** /requests/{requestId} | Updates a request, usually used to cancel running requests.


<a name="requestServiceCreateRequests"></a>
# **requestServiceCreateRequests**
> requestServiceCreateRequests(body)

Creates one or more Requests



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.RequestsApi;


RequestsApi apiInstance = new RequestsApi();
RequestPostRequest body = new RequestPostRequest(); // RequestPostRequest | 
try {
    apiInstance.requestServiceCreateRequests(body);
} catch (ApiException e) {
    System.err.println("Exception when calling RequestsApi#requestServiceCreateRequests");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**RequestPostRequest**](RequestPostRequest.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="requestServiceGetRequest"></a>
# **requestServiceGetRequest**
> RequestResponse requestServiceGetRequest(requestId, fields)

Get the details of a request



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.RequestsApi;


RequestsApi apiInstance = new RequestsApi();
String requestId = "requestId_example"; // String | 
String fields = "Requests/_*"; // String | Filter fields in the response (identifier fields are mandatory)
try {
    RequestResponse result = apiInstance.requestServiceGetRequest(requestId, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling RequestsApi#requestServiceGetRequest");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **requestId** | **String**|  |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to Requests/*]

### Return type

[**RequestResponse**](RequestResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="requestServiceGetRequests"></a>
# **requestServiceGetRequests**
> List&lt;RequestResponse&gt; requestServiceGetRequests(fields, sortBy, pageSize, from, to)

Get all requests. A predicate can be given to filter results.



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.RequestsApi;


RequestsApi apiInstance = new RequestsApi();
String fields = "Requests/id"; // String | Filter fields in the response (identifier fields are mandatory)
String sortBy = "Requests/id.asc"; // String | Sort resources in result by (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
String from = "0"; // String | The starting page resource (inclusive).  \"start\" is also accepted.
String to = "to_example"; // String | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    List<RequestResponse> result = apiInstance.requestServiceGetRequests(fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling RequestsApi#requestServiceGetRequests");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to Requests/id]
 **sortBy** | **String**| Sort resources in result by (asc | desc) | [optional] [default to Requests/id.asc]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **String**| The starting page resource (inclusive).  \&quot;start\&quot; is also accepted. | [optional] [default to 0]
 **to** | **String**| The ending page resource (inclusive).  \&quot;end\&quot; is also accepted. | [optional]

### Return type

[**List&lt;RequestResponse&gt;**](RequestResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="requestServiceUpdateRequests"></a>
# **requestServiceUpdateRequests**
> requestServiceUpdateRequests(requestId, body)

Updates a request, usually used to cancel running requests.

Changes the state of an existing request. Usually used to cancel running requests.

### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.RequestsApi;


RequestsApi apiInstance = new RequestsApi();
String requestId = "requestId_example"; // String | 
RequestPutRequest body = new RequestPutRequest(); // RequestPutRequest | 
try {
    apiInstance.requestServiceUpdateRequests(requestId, body);
} catch (ApiException e) {
    System.err.println("Exception when calling RequestsApi#requestServiceUpdateRequests");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **requestId** | **String**|  |
 **body** | [**RequestPutRequest**](RequestPutRequest.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

