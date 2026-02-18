# 任务中心接口调整

## 1【新增】获取任务查询参数字典值

调用接口获取任务中心查询接口需要的参数字典值，并在 **/admin/api/getjoblist** 时使用

### 1.1 接口地址

```text
/admin/api/getJobQueryParamDict
```

### 1.2 请求方式

```text
GET
```

### 1.3 请求参数

无

### 1.4 返回结果

界面显示value的内容，在调用 **/admin/api/getjoblist** 接口时传key对应的内容。

```json
{
  "jobNames": [{
    "key": "create_cluster",
    "value": "创建集群"
  },{
    "key": "destroy_cluster",
    "value": "销毁集群"
  },
  ...],
  "jobStates": [{
    "key": "0",
    "value": "已创建"
  },{
    "key": "1",
    "value": "执行中"
  },
  ...]
}

```

## 2【修改】任务中心查询接口


### 1.1 接口地址

```text
/admin/api/getjoblist
```

### 1.2 请求方式

```text
GET
```

### 1.3 请求参数

新增参数jobState

```json
{
  "jobName":"",
  "jobState": "1", // 新增条件
  "clusterName":"",
  "time":"",
  "begTime":"",
  "endTime":"",
  "pageIndex":1,
  "pageSize":20
}
```

### 1.4 返回结果

无变化
