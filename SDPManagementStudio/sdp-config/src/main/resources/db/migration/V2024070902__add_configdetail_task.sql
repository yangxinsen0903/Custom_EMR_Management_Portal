
INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'cluster.destroy.limit.time','60','sdp-admin','test','master','db' from config_detail
where not exists (select 1 from config_detail where akey='cluster.destroy.limit.time' and application='sdp-admin') limit 1;

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'cluster.destroy.limit.count','3','sdp-admin','test','master','db' from config_detail
where not exists (select 1 from config_detail where akey='cluster.destroy.limit.count' and application='sdp-admin') limit 1;

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'cluster.destroy.task.time','1/10 * * * * ?','sdp-admin','test','master','db' from config_detail
where not exists (select 1 from config_detail where akey='cluster.destroy.task.time' and application='sdp-admin') limit 1;

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'cluster.destroy.listentask.time','1/5 * * * * ?','sdp-admin','test','master','db' from config_detail
where not exists (select 1 from config_detail where akey='cluster.destroy.listentask.time' and application='sdp-admin') limit 1;
-- shein相关配置
INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'shein.request.url','','sdp-admin','test','master','db' from config_detail
where not exists (select 1 from config_detail where akey='shein.request.url' and application='sdp-admin') limit 1;

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'shein.request.xtoken','','sdp-admin','test','master','db' from config_detail
where not exists (select 1 from config_detail where akey='shein.request.xtoken' and application='sdp-admin') limit 1;

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'shein.cmdb.request.url','','sdp-admin','test','master','db' from config_detail
where not exists (select 1 from config_detail where akey='shein.cmdb.request.url' and application='sdp-admin') limit 1;

INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'shein.cmdb.request.xtoken','','sdp-admin','test','master','db' from config_detail
where not exists (select 1 from config_detail where akey='shein.cmdb.request.xtoken' and application='sdp-admin') limit 1;

