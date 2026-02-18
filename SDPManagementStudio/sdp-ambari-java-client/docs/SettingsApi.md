# SettingsApi

All URIs are relative to *https://localhost/api/v1*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createSetting**](SettingsApi.md#createSetting) | **POST** /settings | Creates a setting
[**deleteSetting**](SettingsApi.md#deleteSetting) | **DELETE** /settings/{settingName} | Deletes a setting
[**getSetting**](SettingsApi.md#getSetting) | **GET** /settings/{settingName} | Returns a specific setting
[**getSettings**](SettingsApi.md#getSettings) | **GET** /settings | Returns all settings
[**updateSetting**](SettingsApi.md#updateSetting) | **PUT** /settings/{settingName} | Updates a setting


<a name="createSetting"></a>
# **createSetting**
> createSetting(body)

Creates a setting



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.SettingsApi;


SettingsApi apiInstance = new SettingsApi();
SettingRequestSwagger body = new SettingRequestSwagger(); // SettingRequestSwagger | 
try {
    apiInstance.createSetting(body);
} catch (ApiException e) {
    System.err.println("Exception when calling SettingsApi#createSetting");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**SettingRequestSwagger**](SettingRequestSwagger.md)|  |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="deleteSetting"></a>
# **deleteSetting**
> deleteSetting(settingName)

Deletes a setting



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.SettingsApi;


SettingsApi apiInstance = new SettingsApi();
String settingName = "settingName_example"; // String | setting name
try {
    apiInstance.deleteSetting(settingName);
} catch (ApiException e) {
    System.err.println("Exception when calling SettingsApi#deleteSetting");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **settingName** | **String**| setting name |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getSetting"></a>
# **getSetting**
> SettingResponseWrapper getSetting(settingName, fields, sortBy, pageSize, from, to)

Returns a specific setting



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.SettingsApi;


SettingsApi apiInstance = new SettingsApi();
String settingName = "settingName_example"; // String | setting name
String fields = "Settings/_*"; // String | Filter fields in the response (identifier fields are mandatory)
String sortBy = "sortBy_example"; // String | Sort resources in result by (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
Integer from = 0; // Integer | The starting page resource (inclusive).  \"start\" is also accepted.
Integer to = 56; // Integer | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    SettingResponseWrapper result = apiInstance.getSetting(settingName, fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SettingsApi#getSetting");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **settingName** | **String**| setting name |
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to Settings/*]
 **sortBy** | **String**| Sort resources in result by (asc | desc) | [optional]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **Integer**| The starting page resource (inclusive).  \&quot;start\&quot; is also accepted. | [optional] [default to 0]
 **to** | **Integer**| The ending page resource (inclusive).  \&quot;end\&quot; is also accepted. | [optional]

### Return type

[**SettingResponseWrapper**](SettingResponseWrapper.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="getSettings"></a>
# **getSettings**
> List&lt;SettingResponseWrapper&gt; getSettings(fields, sortBy, pageSize, from, to)

Returns all settings



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.SettingsApi;


SettingsApi apiInstance = new SettingsApi();
String fields = "Settings/name"; // String | Filter fields in the response (identifier fields are mandatory)
String sortBy = "sortBy_example"; // String | Sort resources in result by (asc | desc)
Integer pageSize = 10; // Integer | The number of resources to be returned for the paged response.
Integer from = 0; // Integer | The starting page resource (inclusive).  \"start\" is also accepted.
Integer to = 56; // Integer | The ending page resource (inclusive).  \"end\" is also accepted.
try {
    List<SettingResponseWrapper> result = apiInstance.getSettings(fields, sortBy, pageSize, from, to);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SettingsApi#getSettings");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **fields** | **String**| Filter fields in the response (identifier fields are mandatory) | [optional] [default to Settings/name]
 **sortBy** | **String**| Sort resources in result by (asc | desc) | [optional]
 **pageSize** | **Integer**| The number of resources to be returned for the paged response. | [optional] [default to 10]
 **from** | **Integer**| The starting page resource (inclusive).  \&quot;start\&quot; is also accepted. | [optional] [default to 0]
 **to** | **Integer**| The ending page resource (inclusive).  \&quot;end\&quot; is also accepted. | [optional]

### Return type

[**List&lt;SettingResponseWrapper&gt;**](SettingResponseWrapper.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

<a name="updateSetting"></a>
# **updateSetting**
> updateSetting(settingName, body)

Updates a setting



### Example
```java
// Import classes:
//import sunbox.sdp.ambari.client.ApiException;
//import sunbox.sdp.ambari.client.api.SettingsApi;


SettingsApi apiInstance = new SettingsApi();
String settingName = "settingName_example"; // String | setting name
SettingRequestSwagger body = new SettingRequestSwagger(); // SettingRequestSwagger | 
try {
    apiInstance.updateSetting(settingName, body);
} catch (ApiException e) {
    System.err.println("Exception when calling SettingsApi#updateSetting");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **settingName** | **String**| setting name |
 **body** | [**SettingRequestSwagger**](SettingRequestSwagger.md)|  |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: text/plain

