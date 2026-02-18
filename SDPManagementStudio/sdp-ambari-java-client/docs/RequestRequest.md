
# RequestRequest

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**clusterName** | **String** |  |  [optional]
**id** | **Long** |  |  [optional]
**requestStatus** | [**RequestStatusEnum**](#RequestStatusEnum) |  |  [optional]
**abortReason** | **String** |  |  [optional]
**removePendingHostRequests** | **Boolean** |  |  [optional]


<a name="RequestStatusEnum"></a>
## Enum: RequestStatusEnum
Name | Value
---- | -----
PENDING | &quot;PENDING&quot;
QUEUED | &quot;QUEUED&quot;
IN_PROGRESS | &quot;IN_PROGRESS&quot;
HOLDING | &quot;HOLDING&quot;
COMPLETED | &quot;COMPLETED&quot;
FAILED | &quot;FAILED&quot;
HOLDING_FAILED | &quot;HOLDING_FAILED&quot;
TIMEDOUT | &quot;TIMEDOUT&quot;
HOLDING_TIMEDOUT | &quot;HOLDING_TIMEDOUT&quot;
ABORTED | &quot;ABORTED&quot;
SKIPPED_FAILED | &quot;SKIPPED_FAILED&quot;



