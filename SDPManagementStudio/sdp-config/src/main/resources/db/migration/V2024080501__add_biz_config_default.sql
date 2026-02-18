-- 默认值,id:雪花id,IdUtil.getSnowflakeNextId()

REPLACE INTO biz_config (id, category, name, cfg_key, cfg_value, remark, sort_no, state, create_time, update_time) values
(1820703815163932673, '限流时间限制','限流时间限制','cluster.destroy.limit.time','60','限流时间限制',1 ,'VALID', now(),now());

REPLACE INTO biz_config (id, category, name, cfg_key, cfg_value, remark, sort_no, state, create_time, update_time) values
(1820703815163932674, '限流数量限制','限流数量限制','cluster.destroy.limit.count','1','限流数量限制',1 ,'VALID', now(),now());

REPLACE INTO biz_config (id, category, name, cfg_key, cfg_value, remark, sort_no, state, create_time, update_time) values
(1820703815163932675, 'sheinurl','sheinurl','shein.cmdb.request.url','','sheinurl',1 ,'VALID', now(),now());

REPLACE INTO biz_config (id, category, name, cfg_key, cfg_value, remark, sort_no, state, create_time, update_time) values
(1820703815163932676, 'sheintoken','sheintoken','shein.cmdb.request.xtoken','','sheintoken',1 ,'VALID', now(),now());











