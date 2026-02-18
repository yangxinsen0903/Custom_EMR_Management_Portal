-- 中央站 Region配置更新
update conf_cluster set region = 'uswest5' where 1 = 1;

INSERT INTO config_detail (akey, avalue, application, profile, label, mwtype) VALUES ('sdp.region', 'uswest5', 'sdp-admin', 'test', 'master', 'db');

