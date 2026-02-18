delete from config_detail where akey='scale.active.check.expire.time';
INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'scale.active.check.expire.time','*/30 * * * * ?','sdp-scale','test','master','db'
where not exists (select 1 from config_detail where akey='scale.active.check.expire.time' and application='sdp-scale')
limit 1;

delete from config_detail where akey='metric.collect.task.time';
INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'metric.collect.task.time','*/5 * * * * ?','sdp-scale','test','master','db'
where not exists (select 1 from config_detail where akey='metric.collect.task.time' and application='sdp-scale')
limit 1;

delete from config_detail where akey='metric.expire.time';
INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'metric.expire.time','300','sdp-scale','test','master','db'
where not exists (select 1 from config_detail where akey='metric.expire.time' and application='sdp-scale')
limit 1;

delete from config_detail where akey='metric.active.check.expire.time';
INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'metric.active.check.expire.time','*/5 * * * * ?','sdp-scale','test','master','db'
where not exists (select 1 from config_detail where akey='metric.active.check.expire.time' and application='sdp-scale')
limit 1;

delete from config_detail where akey='scale.task.thread.pool.size';
INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'scale.task.thread.pool.size','50','sdp-scale','test','master','db'
where not exists (select 1 from config_detail where akey='scale.task.thread.pool.size' and application='sdp-scale')
limit 1;

delete from config_detail where akey='metric.collect.server.expire.time';
INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'metric.collect.server.expire.time','300','sdp-scale','test','master','db'
where not exists (select 1 from config_detail where akey='metric.collect.server.expire.time' and application='sdp-scale')
limit 1;

delete from config_detail where akey='hadoop.jmx.api.port';
INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'hadoop.jmx.api.port','8088','sdp-scale','test','master','db'
where not exists (select 1 from config_detail where akey='hadoop.jmx.api.port' and application='sdp-scale')
limit 1;

delete from config_detail where akey='metric.collect.wait.time';
INSERT INTO config_detail (akey,avalue,application,profile,label,mwtype)
select 'metric.collect.wait.time','60','sdp-scale','test','master','db'
where not exists (select 1 from config_detail where akey='metric.collect.wait.time' and application='sdp-scale')
limit 1;