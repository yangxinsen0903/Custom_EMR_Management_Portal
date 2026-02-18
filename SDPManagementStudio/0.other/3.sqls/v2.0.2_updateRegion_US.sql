-- Dev环境/SIT环境/美国站的 Region配置更新
update conf_cluster set region = 'uswest3' where 1 = 1;

INSERT INTO config_detail (akey, avalue, application, profile, label, mwtype) VALUES ('sdp.region', 'uswest3', 'sdp-admin', 'test', 'master', 'db');
