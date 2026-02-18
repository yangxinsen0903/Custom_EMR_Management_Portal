# BlueprintsApi

All URIs are relative to *https://localhost/api/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**blueprintServiceCreateBlueprint**](BlueprintsApi.md#blueprintServiceCreateBlueprint) | **POST** /blueprints/{blueprintName} | Creates a blueprint
[**blueprintServiceDeleteBlueprint**](BlueprintsApi.md#blueprintServiceDeleteBlueprint) | **DELETE** /blueprints/{blueprintName} | Deletes a blueprint
[**blueprintServiceDeleteBlueprints**](BlueprintsApi.md#blueprintServiceDeleteBlueprints) | **DELETE** /blueprints | Deletes multiple blueprints that match the predicate. Omitting the predicate will delete all blueprints.
[**blueprintServiceGetBlueprint**](BlueprintsApi.md#blueprintServiceGetBlueprint) | **GET** /blueprints/{blueprintName} | Get the details of a blueprint
[**blueprintServiceGetBlueprints**](BlueprintsApi.md#blueprintServiceGetBlueprints) | **GET** /blueprints | Get all blueprints


<a name="blueprintServiceCreateBlueprint"></a>
# **blueprintServiceCreateBlueprint**
> blueprintServiceCreateBlueprint(blueprintName, body)

Creates a blueprint



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.BlueprintsApi;


BlueprintsApi apiInstance = new BlueprintsApi();
String blueprintName = "blueprintName_example"; // String | 
BlueprintSwagger body = new BlueprintSwagger(); // BlueprintSwagger | 
try {
    apiInstance.blueprintServiceCreateBlueprint(blueprintName, body);
} catch (ApiException e) {
    System.err.println("Exception when calling BlueprintsApi#blueprintServiceCreateBlueprint");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **blueprintName** | **String**|  |
 **body** | [**BlueprintSwagger**](BlueprintSwagger.md)|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="blueprintServiceDeleteBlueprint"></a>
# **blueprintServiceDeleteBlueprint**
> blueprintServiceDeleteBlueprint(blueprintName)

Deletes a blueprint



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.BlueprintsApi;


BlueprintsApi apiInstance = new BlueprintsApi();
String blueprintName = "blueprintName_example"; // String | 
try {
    apiInstance.blueprintServiceDeleteBlueprint(blueprintName);
} catch (ApiException e) {
    System.err.println("Exception when calling BlueprintsApi#blueprintServiceDeleteBlueprint");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **blueprintName** | **String**|  |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="blueprintServiceDeleteBlueprints"></a>
# **blueprintServiceDeleteBlueprints**
> blueprintServiceDeleteBlueprints()

Deletes multiple blueprints that match the predicate. Omitting the predicate will delete all blueprints.



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.BlueprintsApi;


BlueprintsApi apiInstance = new BlueprintsApi();
try {
    apiInstance.blueprintServiceDeleteBlueprints();
} catch (ApiException e) {
    System.err.println("Exception when calling BlueprintsApi#blueprintServiceDeleteBlueprints");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="blueprintServiceGetBlueprint"></a>
# **blueprintServiceGetBlueprint**
> List&lt;BlueprintSwagger&gt; blueprintServiceGetBlueprint(blueprintName, fields)

Get the details of a blueprint



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.BlueprintsApi;


BlueprintsApi apiInstance = new BlueprintsApi();
String blueprintName = "blueprintName_example"; // String | 
String fields = "Blueprints/_*"; // String | Filter fields in the response (identifier fields are mandatory)
try {
    List<BlueprintSwagger> result = apiInstance.blueprintServiceGetBlueprint(blueprintName, fields);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling BlueprintsApi#blueprintServiceGetBlueprint");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **blueprintName** | **String**|  |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to Blueprints/*]

### Return type

[**List&lt;BlueprintSwagger&gt;**](BlueprintSwagger.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="blueprintServiceGetBlueprints"></a>
# **blueprintServiceGetBlueprints**
> List&lt;BlueprintSwagger&gt; blueprintServiceGetBlueprints(fields, sortBy, pageSize, from, to)

Get all blueprints



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.BlueprintsApi;


BlueprintsApi apiInstance = new BlueprintsApi();
String fields = "Blueprints/blueprint_name"; // String | Filter fields in the response (identifier fields are mandatory)
String sortBy = "Blueprints/blueprint_name.asc"; // String | Sort resources in result by (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
String from = "0"; // String | The starting page resource (inclusive).  \"start\" is also accepted.
String to = "to_example"; // String | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    List<BlueprintSwagger> result = apiInstance.blueprintServiceGetBlueprints(fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling BlueprintsApi#blueprintServiceGetBlueprints");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to Blueprints/blueprint_name]
 **sortBy** | **String**| Sort resources in result by (asc | desc) | [optional] [default to Blueprints/blueprint_name.asc]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **String**| The starting page resource (inclusive).  \&quot;start\&quot; is also accepted. | [optional] [default to 0]
 **to** | **String**| The ending page resource (inclusive).  \&quot;end\&quot; is also accepted. | [optional]

### Return type

[**List&lt;BlueprintSwagger&gt;**](BlueprintSwagger.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

