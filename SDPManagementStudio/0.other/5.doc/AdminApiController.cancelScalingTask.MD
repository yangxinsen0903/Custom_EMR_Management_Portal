# 1 手动删除队列中的任务

调用接口删除在队列中的任务。

# 1.1 接口地址

```text
/api/cluster/cancelScalingTask
```

# 1.2 请求方式

```text
POST
```

# 1.3 请求参数

```java
// 集群id
@NotEmpty(message = "集群id不能为空")
private String clusterId;

// 实例类型
@NotEmpty(message = "任务id")
private String taskId;

// 实例组名称
@NotEmpty(message = "实例组名称不能为空")
private String groupName;

```

# 1.4 返回结果

同其它接口