# ActionsApi

All URIs are relative to *https://localhost/api/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**actionServiceCreateActionDefinition**](ActionsApi.md#actionServiceCreateActionDefinition) | **POST** /actions/{actionName} | Creates an action definition - Currently Not Supported
[**actionServiceDeleteActionDefinition**](ActionsApi.md#actionServiceDeleteActionDefinition) | **DELETE** /actions/{actionName} | Deletes an action definition - Currently Not Supported
[**actionServiceGetActionDefinition**](ActionsApi.md#actionServiceGetActionDefinition) | **GET** /actions/{actionName} | Get the details of an action definition
[**actionServiceGetActionDefinitions**](ActionsApi.md#actionServiceGetActionDefinitions) | **GET** /actions | Get all action definitions
[**actionServiceUpdateActionDefinition**](ActionsApi.md#actionServiceUpdateActionDefinition) | **PUT** /actions/{actionName} | Updates an action definition - Currently Not Supported


<a name="actionServiceCreateActionDefinition"></a>
# **actionServiceCreateActionDefinition**
> actionServiceCreateActionDefinition(actionName, body)

Creates an action definition - Currently Not Supported



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ActionsApi;


ActionsApi apiInstance = new ActionsApi();
String actionName = "actionName_example"; // String | 
ActionRequestSwagger body = new ActionRequestSwagger(); // ActionRequestSwagger | 
try {
    apiInstance.actionServiceCreateActionDefinition(actionName, body);
} catch (ApiException e) {
    System.err.println("Exception when calling ActionsApi#actionServiceCreateActionDefinition");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **actionName** | **String**|  |
 **body** | [**ActionRequestSwagger**](ActionRequestSwagger.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="actionServiceDeleteActionDefinition"></a>
# **actionServiceDeleteActionDefinition**
> actionServiceDeleteActionDefinition(actionName)

Deletes an action definition - Currently Not Supported



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ActionsApi;


ActionsApi apiInstance = new ActionsApi();
String actionName = "actionName_example"; // String | 
try {
    apiInstance.actionServiceDeleteActionDefinition(actionName);
} catch (ApiException e) {
    System.err.println("Exception when calling ActionsApi#actionServiceDeleteActionDefinition");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **actionName** | **String**|  |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="actionServiceGetActionDefinition"></a>
# **actionServiceGetActionDefinition**
> ActionResponseSwagger actionServiceGetActionDefinition(actionName, fields)

Get the details of an action definition



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ActionsApi;


ActionsApi apiInstance = new ActionsApi();
String actionName = "actionName_example"; // String | 
String fields = "Actions/_*"; // String | Filter fields in the response (identifier fields are mandatory)
try {
    ActionResponseSwagger result = apiInstance.actionServiceGetActionDefinition(actionName, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ActionsApi#actionServiceGetActionDefinition");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **actionName** | **String**|  |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to Actions/*]

### Return type

[**ActionResponseSwagger**](ActionResponseSwagger.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="actionServiceGetActionDefinitions"></a>
# **actionServiceGetActionDefinitions**
> List&lt;ActionResponseSwagger&gt; actionServiceGetActionDefinitions(fields, sortBy, pageSize, from, to)

Get all action definitions



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ActionsApi;


ActionsApi apiInstance = new ActionsApi();
String fields = "Actions/action_name"; // String | Filter fields in the response (identifier fields are mandatory)
String sortBy = "Actions/action_name.asc"; // String | Sort resources in result by (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
String from = "0"; // String | The starting page resource (inclusive).  \"start\" is also accepted.
String to = "to_example"; // String | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    List<ActionResponseSwagger> result = apiInstance.actionServiceGetActionDefinitions(fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ActionsApi#actionServiceGetActionDefinitions");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to Actions/action_name]
 **sortBy** | **String**| Sort resources in result by (asc | desc) | [optional] [default to Actions/action_name.asc]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **String**| The starting page resource (inclusive).  \&quot;start\&quot; is also accepted. | [optional] [default to 0]
 **to** | **String**| The ending page resource (inclusive).  \&quot;end\&quot; is also accepted. | [optional]

### Return type

[**List&lt;ActionResponseSwagger&gt;**](ActionResponseSwagger.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="actionServiceUpdateActionDefinition"></a>
# **actionServiceUpdateActionDefinition**
> actionServiceUpdateActionDefinition(actionName, body)

Updates an action definition - Currently Not Supported



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.ActionsApi;


ActionsApi apiInstance = new ActionsApi();
String actionName = "actionName_example"; // String | 
ActionRequestSwagger body = new ActionRequestSwagger(); // ActionRequestSwagger | 
try {
    apiInstance.actionServiceUpdateActionDefinition(actionName, body);
} catch (ApiException e) {
    System.err.println("Exception when calling ActionsApi#actionServiceUpdateActionDefinition");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **actionName** | **String**|  |
 **body** | [**ActionRequestSwagger**](ActionRequestSwagger.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

