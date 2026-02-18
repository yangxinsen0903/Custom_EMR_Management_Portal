INSERT INTO biz_config (id, category, name, cfg_key, cfg_value, remark, sort_no, state, create_time, update_time)
select 101, 'sdp路由配置', '是否开启路由', 'sdpRouter.isEnableRouter', 'false', '是否开启路由:true Or false',1, 'VALID', '2024-08-16 00:57:11', '2024-08-16 00:57:11'
    where not exists (select 1 from biz_config where id=101);

INSERT INTO biz_config (id, category, name, cfg_key, cfg_value, remark, sort_no, state, create_time, update_time)
select 102, 'sdp路由配置', 'SDP一期URL', 'sdpRouter.sdp1Url', 'http://localhost:8080', 'SDP一期URL:格式 http://ip:port',2, 'VALID', '2024-08-16 00:57:11', '2024-08-16 00:57:11'
    where not exists (select 1 from biz_config where id=102);

INSERT INTO biz_config (id, category, name, cfg_key, cfg_value, remark, sort_no, state, create_time, update_time)
select 103, 'sdp路由配置', 'SDP二期集群前缀', 'sdpRouter.clusterNamePrefix', 'sdp_', 'SDP二期集群前缀:多个使用逗号分隔',3, 'VALID', '2024-08-16 00:57:11', '2024-08-16 00:57:11'
    where not exists (select 1 from biz_config where id=103);