
# 需手动执行SQL
```sql
-- 默认上传日志的Blob所属数据中心
INSERT INTO config_core (akey,avalue,application,profile,label,mwtype) VALUES
('sdp.blob.default.region','westus2','core','test','master','db');

-- 处理补足驱逐Vm的执行频率
INSERT INTO config_core (akey,avalue,application,profile,label,mwtype) VALUES
('cron.handleEvictVm.expr','0 * * * * ?','core','test','master','db');
```


# 增加接收 Azure fleet自动补会被驱逐的VM事件
config_detail表修改key=azure.servicebus.configs

增加配置如下:

connectionString根据实际情况修改
```json
    {
      "name": "evictVmHandler",
      "type": "consumer",
      "connectionString": "此处填写ServiceBus的EndPoint",
      "topicName": "vm_info_from_rm_to_sdp",
      "subName": "sdp-consumer",
      "callback": "AutoCreatedEvictVmService@handleEvictVmEvent"
    }
```
