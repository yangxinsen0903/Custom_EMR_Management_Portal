-- 删除配置项, 移动到biz_config
delete from config_detail where akey='cluster.destroy.limit.time';
delete from config_detail where akey='cluster.destroy.limit.count';

delete from config_detail where akey='shein.request.url';
delete from config_detail where akey='shein.request.xtoken';

delete from config_detail where akey='shein.cmdb.request.url';
delete from config_detail where akey='shein.cmdb.request.xtoken';


