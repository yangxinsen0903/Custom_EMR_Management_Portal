# AuthApi

All URIs are relative to *https://localhost/api/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**getUsersViaPost**](AuthApi.md#getUsersViaPost) | **POST** /auth | User authorization request


<a name="getUsersViaPost"></a>
# **getUsersViaPost**
> getUsersViaPost(body)

User authorization request



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.AuthApi;


AuthApi apiInstance = new AuthApi();
AuthRequestCreateAuthSwagger body = new AuthRequestCreateAuthSwagger(); // AuthRequestCreateAuthSwagger | 
try {
    apiInstance.getUsersViaPost(body);
} catch (ApiException e) {
    System.err.println("Exception when calling AuthApi#getUsersViaPost");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**AuthRequestCreateAuthSwagger**](AuthRequestCreateAuthSwagger.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

