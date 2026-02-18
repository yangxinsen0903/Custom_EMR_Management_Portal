
update biz_config set category = '集群销毁限流配置'  where cfg_key='cluster.destroy.limit.time';
update biz_config set  category = '集群销毁限流配置' ,sort_no = 2  where cfg_key='cluster.destroy.limit.count';

update biz_config set category = 'shein请求接口配置' ,name ='CMDB URL' ,remark='访问Shein CMDB的URL，格式： http://ip:port' where cfg_key='shein.cmdb.request.url';
update biz_config set category = 'shein请求接口配置', sort_no = 2  ,name ='CMDB Token' ,remark='访问Shein CMDB的Token' where cfg_key='shein.cmdb.request.xtoken';
